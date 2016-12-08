package com.example.fazhao.locationmanager.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.example.fazhao.locationmanager.activity.DBHelper;
import com.example.fazhao.locationmanager.activity.TraceDao;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;

/**
 * Created by fazhao on 2016/12/6.
 */

public class BaseApplication extends Application {
    private static TraceDao mTaceDao;
    private static DBHelper dbHelper;
    private static Crypto mCrypto;
    private static KeyManager km;

    public static TraceDao getmTaceDao() {
        return mTaceDao;
    }

    public static DBHelper getDbHelper() {
        return dbHelper;
    }

    public static Crypto getmCrypto() {
        return mCrypto;
    }

    public static KeyManager getKm() {
        return km;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /**
        * 百度地图
        * */
        SDKInitializer.initialize(getApplicationContext());
        km = new KeyManager(this);
        mCrypto = new Crypto(this);
        dbHelper = new DBHelper(this);
        mTaceDao = new TraceDao();

    }
}
