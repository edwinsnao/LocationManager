package com.example.fazhao.locationmanager.baidu_map.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.activity.TraceDao;
import com.example.fazhao.locationmanager.activity.TraceItem;
import com.example.fazhao.locationmanager.application.BaseApplication;

import java.util.List;

/**
 * Created by fazhao on 2016/12/15.
 */

public class SplashActivity extends Activity {
    private TraceDao mTraceDao;
    private List<TraceItem> traceItems;
    private List<String> time;
    private List<TraceItem> distance;
    private List<String> route;
    private int lastStep;
//    private LatLng latLng1;
//    private List<LatLng> historyFromLoad = BaseApplication.getHistory();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
        setContentView(R.layout.baidu_splash_activity);
        initData();
        loadData();
        setData();
        Handler handler = new Handler();
        /**
        * 需要延时才可以看到，否则太快看不到
        * */
        handler.postDelayed(switchRunnable,500);

    }

    private void switchActivity() {
        Intent it = new Intent();
        it.setClass(SplashActivity.this,IndoorLocationActivity.class);
        startActivity(it);
        finish();
    }

    private Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            switchActivity();
        }
    };

    private void setData() {
//        BaseApplication.setHistory(historyFromLoad);
        BaseApplication.setRoute(route);
        BaseApplication.setDistance(distance);
        BaseApplication.setTime(time);
        BaseApplication.setLastStep(lastStep);
    }


    private void loadData() {
        /**
         * 上次数据
         * */
        if(traceItems.size()!=0) {
//            latLng1 = new LatLng(traceItems.get(0).getLatitude(), traceItems.get(0).getLongitude());
//            for (int i = 0; i < traceItems.size(); i++) {
//                LatLng latLng = new LatLng(traceItems.get(i).getLatitude(), traceItems.get(i).getLongitude());
//                historyFromLoad.add(latLng);
//            }
            /**
            * 有history数据
            * */
            BaseApplication.setHasHistory(true);
        }
        else
            BaseApplication.setHasHistory(false);
        time = mTraceDao.getLastTime();
        route = mTraceDao.getLastRoute();
        distance = mTraceDao.getLastDistance();
        lastStep = mTraceDao.getLastStep().getStep();
//        Log.e("hasHistory?",String.valueOf(BaseApplication.isHasHistory()));
//        BaseApplication.setHistory(historyFromLoad);
//        Log.e("traceItemSize?",String.valueOf(traceItems.size()));
    }

    private void initData() {
        mTraceDao = BaseApplication.getmTaceDao();
        traceItems = mTraceDao.searchData(mTraceDao.maxTag());
    }

}
