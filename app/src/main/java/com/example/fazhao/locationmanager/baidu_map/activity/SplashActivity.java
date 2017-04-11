package com.example.fazhao.locationmanager.baidu_map.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.encrypt.Crypto;

import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.baidu.location.h.j.C;
import static com.baidu.location.h.j.m;
import static java.lang.reflect.Array.getInt;

/**
 * Created by fazhao on 2016/12/15.
 */

public class SplashActivity extends Activity {
    private TraceDao mTraceDao;
    private List<TraceItem> traceItems;
    private List<String> time;
    private TraceItem distance;
    private List<String> route;
    private int lastStep;
    private boolean user_first;

    //    private LatLng latLng1;
//    private List<LatLng> historyFromLoad = BaseApplication.getHistory();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
        setContentView(R.layout.baidu_splash_activity);
        FrameLayout layout = (FrameLayout) findViewById(R.id.main);
        InputStream is;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inSampleSize = 2;
        is = getResources().openRawResource(R.drawable.splash3);
        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
        layout.setBackgroundDrawable(bd);
        initData();
        loadData();
        setData();
        /**
         * 判断是否首次进入app
         * */
        SharedPreferences setting = getSharedPreferences("location_first_in", Context.MODE_PRIVATE);
        user_first = setting.getBoolean("FIRST", true);
        if (user_first != false) {
            SharedPreferences.Editor editor = setting.edit();//获取编辑器
            editor.putBoolean("FIRST", false);
            editor.commit();
        }
        Handler handler = new Handler();
        /**
         * 需要延时才可以看到，否则太快看不到
         * */
        handler.postDelayed(switchRunnable, 500);
    }

    private void switchActivity(Class<?> cls) {
        Intent it = new Intent();
        it.setClass(SplashActivity.this, cls);
        startActivity(it);
        finish();
    }

    private Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            if (user_first)
                switchActivity(CustomPreferenceActivity.class);
            else
                switchActivity(IndoorLocationActivity.class);

        }
    };

    private void setData() {
//        BaseApplication.setHistory(historyFromLoad);
        if (distance != null)
            BaseApplication.setDistance(distance);
        if (time.size() != 0)
            BaseApplication.setTime(time);
        BaseApplication.setLastStep(lastStep);
        if (route.size() != 0)
            BaseApplication.setRoute(route);
    }


    private void loadData() {
        /**
         * 上次数据
         * */
        if (traceItems.size() != 0) {
//            latLng1 = new LatLng(traceItems.get(0).getLatitude(), traceItems.get(0).getLongitude());
//            for (int i = 0; i < traceItems.size(); i++) {
//                LatLng latLng = new LatLng(traceItems.get(i).getLatitude(), traceItems.get(i).getLongitude());
//                historyFromLoad.add(latLng);
//            }
            /**
             * 有history数据
             * */
            BaseApplication.setHasHistory(true);
        } else
            BaseApplication.setHasHistory(false);
        time = mTraceDao.getLastTime();
        route = mTraceDao.getLastRoute();
        distance = mTraceDao.getLastDistance();
        TraceItem tmp = mTraceDao.getLastStep();
        if (tmp != null)
            lastStep = tmp.getStep();

        SharedPreferences setting = getSharedPreferences("location_first_in", Context.MODE_PRIVATE);
        Crypto mCrypto = BaseApplication.getmCrypto();
        String mSubject = setting.getString("Subject", null);
        String mPwd = setting.getString("Pwd", null);
        String mServer = setting.getString("Server", null);
        int mLoc = setting.getInt("Loc", 0);
        String mFrom = setting.getString("From", null);
        String mTo = setting.getString("To", null);

        try {
            if (mSubject != null) {
                BaseApplication.setmSubject(mCrypto.armorDecrypt(mSubject));
            }
            if (mServer != null) {
                BaseApplication.setmServer(mCrypto.armorDecrypt(mServer));
            }
            if (mPwd != null) {
                BaseApplication.setmPwd(mCrypto.armorDecrypt(mPwd));
            }
            if (mFrom != null) {
                BaseApplication.setFrom(mCrypto.armorDecrypt(mFrom));
            }
            if (mTo != null) {
                BaseApplication.setTo(mCrypto.armorDecrypt(mTo));
            }
            if (mLoc != 0) {
                BaseApplication.setLocGap(mLoc);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

//        if(mSubject != null && mServer != null  && mFrom != null
//                && mPwd != null  && mTo != null  && mLoc != 0) {
//            Log.e("watch ", mSubject);
//            Log.e("watch ", mServer);
//            Log.e("watch ", String.valueOf(mLoc));
//            Log.e("watch ", mFrom);
//            Log.e("watch ", mTo);
//            Log.e("watch ", mPwd);
//        }
//        BaseApplication.setHistory(historyFromLoad);
    }

    private void initData() {
        mTraceDao = BaseApplication.getmTaceDao();
        traceItems = mTraceDao.searchData(mTraceDao.maxTag());
    }

}
