package com.example.fazhao.locationmanager.baidu_map.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.fazhao.locationmanager.baidu_map.activity.IndoorLocationActivity;

import static android.drm.DrmStore.Playback.STOP;
import static com.example.fazhao.locationmanager.R.id.step;

/**
 * Created by fazhao on 2017/2/21.
 */

public class StepService extends Service {
    private static SensorManager mSensorManager;
    private static Sensor mStepSensor;
    private static SensorEventListener mSensorEventListener;
    public int mStep = 0;
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
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        m_wklk = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LocationService.class.getName());
        m_wklk.acquire();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_wklk != null) {
            m_wklk.release();
            m_wklk = null;
        }
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public void sendStepBroadcast(int step) {
        bundle.putString("step", String.valueOf(step));
        intent.putExtras(bundle);
        intent.setAction(BROADCAST_ACTION);
        sendBroadcast(intent);
    }

    public static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }
            if (mSensorManager != null) {//取消监听后重写监听，以保持后台运行
                mSensorManager.unregisterListener(mSensorEventListener);
                mSensorManager
                        .registerListener(
                                mSensorEventListener,
                                mStepSensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
//                mSensorManager
//                        .registerListener(
//                                mSensorEventListener,
//                                mSensorManager
//                                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                                SensorManager.SENSOR_DELAY_NORMAL);
            }


        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
            mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorEventListener = new SensorEventListener() {
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }

                @Override
                public void onSensorChanged(SensorEvent event) {
                    mStep += (int) event.values[0];
                    Message msg = Message.obtain();
                    msg.obj = mStep;
                    IndoorLocationActivity.mHandler.sendMessage(msg);
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
