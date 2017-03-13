package com.example.fazhao.locationmanager.application;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.example.fazhao.locationmanager.baidu_map.model.DBHelper;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;
import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fazhao on 2016/12/6.
 */

public class BaseApplication extends Application {
    private static TraceDao mTaceDao;
    private static DBHelper dbHelper;
    private static List<LatLng> historyFromLoad = new ArrayList<>();
    private static boolean hasHistory;
    private static LocationClient mLocClient;
    private static LocationClientOption option;
    private static List<String> time;
    private static List<TraceItem> distance;
    private static List<String> route;

    public static LocationClient getmLocClient() {
        return mLocClient;
    }

    public static List<String> getRoute() {
        return route;
    }

    public static void setRoute(List<String> r) {
        route = r;
    }

    public static List<String> getTime() {
        return time;
    }

    public static void setTime(List<String> t) {
        time = t;
    }

    public static List<TraceItem> getDistance() {
        return distance;
    }

    public static void setDistance(List<TraceItem> d) {
        distance = d;
    }

    public static LocationClientOption getOption() {
        return option;
    }

    public static void registerListener(BDLocationListener mListener){
        mLocClient.registerLocationListener(mListener);
    }

    public static boolean isHasHistory() {
        return hasHistory;
    }

    public static void setHasHistory(boolean hasHistory) {
        BaseApplication.hasHistory = hasHistory;
    }
        private static Crypto mCrypto;
    private static KeyManager km;


    public static List<LatLng> getHistory() {
        return historyFromLoad;
    }

    public static void setHistory(List<LatLng> historyFromLoad) {
        BaseApplication.historyFromLoad = historyFromLoad;
    }

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
        try {
            /**
             * 三星手机泄漏内存(editText)，我的手机
             * */
            if ("samsung".equalsIgnoreCase(Build.MANUFACTURER) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Class cls = Class.forName("android.sec.clipboard.ClipboardUIManager");
                Method m = cls.getDeclaredMethod("getInstance", Context.class);
                m.setAccessible(true);
                Object o = m.invoke(null, getApplicationContext());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        /**
        * 百度地图
        * */
        SDKInitializer.initialize(getApplicationContext());
        LeakCanary.install(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        km = new KeyManager(this);
        mCrypto = new Crypto(this);
        mLocClient = new LocationClient(this);
        option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setProdName("LocationManager");
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
        option.setLocationNotify(true);
        option.disableCache(false);
        option.setAddrType("all");
        option.setNeedDeviceDirect(true);
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);
        dbHelper = new DBHelper(this);
        mTaceDao = new TraceDao();

    }
}
