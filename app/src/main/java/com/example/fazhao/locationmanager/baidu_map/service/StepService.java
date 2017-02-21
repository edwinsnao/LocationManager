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

import static com.baidu.location.h.j.S;
import static com.example.fazhao.locationmanager.R.id.step;

/**
 * Created by fazhao on 2017/2/21.
 */

public class StepService extends Service {
    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private SensorEventListener mSensorEventListener;
    private int mStep = 0;
    private PowerManager.WakeLock m_wklk;
    private Intent intent = new Intent();
    private Bundle bundle = new Bundle();
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
            /**
             * step
             * */
            mSensorEventListener = new SensorEventListener() {
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }

                @Override
                public void onSensorChanged(SensorEvent event) {
                    mStep += (int) event.values[0];
//                    StringBuilder builder = new StringBuilder("步数:");
//                    builder.append(Integer.toString(mStep));
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
