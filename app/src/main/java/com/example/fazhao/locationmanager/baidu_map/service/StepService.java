package com.example.fazhao.locationmanager.baidu_map.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.baidu.location.h.j.S;
import static com.example.fazhao.locationmanager.R.id.step;

/**
 * Created by fazhao on 2017/2/21.
 */

public class StepService extends Service {
    private SensorManager mSensorManager;
    private Sensor mStepSensor,mCountSensor;
    private Sensor mStepCounter;
    private SensorEventListener mSensorEventListener;
    private int mStep = 0;
    private PowerManager.WakeLock m_wklk;
    private Intent intent = new Intent();
    private Bundle bundle = new Bundle();
    private int stepSensor,hasStepCount,prviousStepCount;
    private boolean hasRecord;

    public static final String BROADCAST_ACTION = "com.action.step";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(m_wklk != null){
            m_wklk.release();
            m_wklk = null;
        }
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public void sendStepBroadcast(int step){
        bundle.putString("step", String.valueOf(step));
        intent.putExtras(bundle);
        intent.setAction(BROADCAST_ACTION);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(mSensorManager == null) {
            mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
            mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (mCountSensor != null) {
                stepSensor = 0;
                Log.v("xf", "countSensor");
                mSensorManager.registerListener(mSensorEventListener, mCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else if (mStepSensor != null) {
                stepSensor = 1;
                Log.v("xf", "detectorSensor");
                mSensorManager.registerListener(mSensorEventListener, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.v("xf", "Count sensor not available!");
//            addBasePedoListener();
            }
            /**
             * step
             * */
            mSensorEventListener = new SensorEventListener() {
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }

                @Override
                public void onSensorChanged(SensorEvent event) {
//                    mStep += (int) event.values[0];
//                    StringBuilder builder = new StringBuilder("步数:");
//                    builder.append(Integer.toString(mStep));

                    if (stepSensor == 0) {
                        int tempStep = (int) event.values[0];
                        if (!hasRecord) {
                            hasRecord = true;
                            hasStepCount = tempStep;
                        } else {
                            int thisStepCount = tempStep - hasStepCount;
                            mStep+=(thisStepCount-prviousStepCount);
                            prviousStepCount = thisStepCount;
//                StepDcretor.CURRENT_SETP++;

                        }
                        Log.d("tempStep" , String.valueOf(tempStep));
                    } else if (stepSensor == 1) {
                        if (event.values[0] == 1.0) {
                            mStep++;
                        }

                    }
                    sendStepBroadcast(mStep);
//                    step.setText(builder);
                }
            };
            /**
             * 如果设置SENSOR_DELAY_FASTEST会浪费电的
             * */
            mSensorManager.registerListener(mSensorEventListener, mStepSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        m_wklk = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LocationService.class.getName());
        m_wklk.acquire();
    }
}
