package com.example.fazhao.locationmanager.baidu_map.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.fazhao.locationmanager.application.BaseApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fazhao on 2016/12/27.
 */

public class LocationService extends Service {
    private LocationClient mLocClient = BaseApplication.getmLocClient();
    private LocationClientOption option = BaseApplication.getOption();
    private PowerManager.WakeLock m_wklk;

    public LocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mLocClient != null) {
            mLocClient.stop();
        }
        super.onDestroy();
        // 停止定时器
        if (m_wklk != null) {
            m_wklk.release();
            m_wklk = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 触发定时器
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mLocClient.setLocOption(option);
        mLocClient.start();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        m_wklk = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LocationService.class.getName());
        m_wklk.acquire();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }
}
