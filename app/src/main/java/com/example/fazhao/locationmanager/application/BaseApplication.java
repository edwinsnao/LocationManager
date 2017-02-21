package com.example.fazhao.locationmanager.application;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.activity.DBHelper;
import com.example.fazhao.locationmanager.activity.TraceDao;
import com.example.fazhao.locationmanager.activity.TraceItem;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fazhao on 2016/12/6.
 */

public class BaseApplication extends Application {
    private static TraceDao mTaceDao;
    private static DBHelper dbHelper;
    private static SQLiteDatabase db;
    private static List<LatLng> historyFromLoad = new ArrayList<>();
    private static boolean hasHistory;
    private static LocationClient mLocClient;
    private static LocationClientOption option;
    private File filepath;
    private static List<String> time;
    private static List<TraceItem> distance;
    private static List<String> route;
    private static int lastStep;

    public static int getLastStep() {
        return lastStep;
    }

    public static void setLastStep(int lastStep) {
        BaseApplication.lastStep = lastStep;
    }

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

    public static final String sdpath = "/data/data/com.example.fazhao.locationmanager/databases/";//sdpath用于存放保存的路径。
//    public static final String sdpath = "/data/data/com.example.fazhao.locationmanager/files/";//sdpath用于存放保存的路径。
    public static final String filename = "trace";//filename用于保存文件名。

    public static boolean isHasHistory() {
        return hasHistory;
    }

    public static SQLiteDatabase getDb() {
        return db;
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
//        getmDb();
        SDKInitializer.initialize(getApplicationContext());
        LeakCanary.install(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
//        Crypto.init(this);
        km = new KeyManager(this);
        mCrypto = new Crypto(this);
        mLocClient = new LocationClient(this);
        option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setProdName("LocationManager");
//        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
//        option.setScanSpan(1000);
        option.setLocationNotify(true);
        option.disableCache(false);
        option.setAddrType("all");
        option.setNeedDeviceDirect(true);
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//        db = getmDb();
//        KeyManager.init(this);
        mLocClient.setLocOption(option);
        dbHelper = new DBHelper(this);
        String databasefilename = sdpath+"/"+filename;//其值等于database的路径
        filepath = new File(databasefilename);
        db = SQLiteDatabase.openDatabase(filepath.getPath(),null,SQLiteDatabase.OPEN_READWRITE);//利用openDatabase方法打开数据库。
        mTaceDao = new TraceDao();

    }

//    private void getmDb() {
//        File dir = new File(sdpath);
//        if (!dir.exists()) {
//            dir.mkdir();
//        }//如果该目录不存在，创建该目录
//        String databasefilename = sdpath+"/"+filename;//其值等于database的路径
//        filepath = new File(databasefilename);
//        /**
//        * 如果重置了，就把下面两行注释掉
//        * */
//        if(filepath.exists())
//            filepath.delete();
//        if (!filepath.exists()) {//如果文件不存在
//            try {
//                InputStream inputStream = getResources().openRawResource(R.raw.trace);//将raw中的test.db放入输入流中
//                FileOutputStream fileOutputStream = new FileOutputStream(databasefilename);//将新的文件放入输出流中
//                byte[] buff = new byte[8192];
//                int len = 0;
//                while ((len = inputStream.read(buff)) > 0) {
//                    fileOutputStream.write(buff, 0, len);
//                }
//                fileOutputStream.close();
//                inputStream.close();
//            } catch (Exception e) {
//                Log.e("info","无法复制");
//                e.printStackTrace();
//            }
//        }//写入文件结束
//        Log.e("filepath"," "+filepath);
//    }
//    private SQLiteDatabase getmDb() {
//        File dir = new File(sdpath);
//        if (!dir.exists()) {
//            dir.mkdir();
//        }//如果该目录不存在，创建该目录
//        String databasefilename = sdpath+"/"+filename;//其值等于database的路径
//        File filepath = new File(databasefilename);
//        if(filepath.exists())
//            filepath.delete();
//        if (!filepath.exists()) {//如果文件不存在
//            try {
//                InputStream inputStream = getResources().openRawResource(R.raw.trace);//将raw中的test.db放入输入流中
//                FileOutputStream fileOutputStream = new FileOutputStream(databasefilename);//将新的文件放入输出流中
//                byte[] buff = new byte[8192];
//                int len = 0;
//                while ((len = inputStream.read(buff)) > 0) {
//                    fileOutputStream.write(buff, 0, len);
//                }
//                fileOutputStream.close();
//                inputStream.close();
//            } catch (Exception e) {
//                Log.e("info","无法复制");
//                e.printStackTrace();
//            }
//        }//写入文件结束
//        Log.e("filepath"," "+filepath);
//        SQLiteDatabase database = SQLiteDatabase.openDatabase(filepath.getPath(),null,SQLiteDatabase.OPEN_READWRITE);//利用openDatabase方法打开数据库。
//        return database;
//    }
}
