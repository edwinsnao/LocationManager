package com.example.fazhao.locationmanager.baidu_map.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.fazhao.locationmanager.application.BaseApplication;

/**
 * Created by fazhao on 2016/12/27.
 */

public class LocationService extends Service {
    LocationClient mLocClient = BaseApplication.getmLocClient();
    LocationClientOption option = BaseApplication.getOption();
    public LocationService(){

    }

    private PowerManager.WakeLock wakeLock = null;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
//        mLocClient = new LocationClient(this.getApplicationContext());
//            LocationClientOption option = new LocationClientOption();
//            option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//ÉèÖÃ¶¨Î»Ä£Ê½
//            option.setCoorType("bd09ll");//·µ»ØµÄ¶¨Î»½á¹ûÊÇ°Ù¶È¾­Î³¶È£¬Ä¬ÈÏÖµgcj02
//            int span=3000;
//            option.setScanSpan(span);//ÉèÖÃ·¢Æð¶¨Î»ÇëÇóµÄ¼ä¸ôÊ±¼äÎª5000ms
        mLocClient.setLocOption(option);
//        mLocClient.setLocOption(option);
//        mLocClient.registerLocationListener(mListener);
        mLocClient.start();

//            mLocationClient.registerLocationListener(new BDLocationListener() {
//
//                @Override
//                public void onReceiveLocation(BDLocation location) {
//                    // TODO Auto-generated method stub
//                    ll = new LatLng(location.getLatitude(),
//                            location.getLongitude());
////            CoordinateConverter converter = new CoordinateConverter();
////            converter.from(CoordinateConverter.CoordType.GPS);
////            // latLng 待转换坐标
////            converter.coord(ll);
////            LatLng desLatLng = converter.convert();
//                    MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
//                            // 此处设置开发者获取到的方向信息，顺时针0-360
//                            .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
//                    // 设置定位数据
//                    mBaiduMap.setMyLocationData(locData);
//                    if(isFirstLoc){
//                        isFirstLoc=false;
////                    if(constant<pointList.size()){
////                        if(DistanceUtil.getDistance(pointList.get(constant),ll)>DistanceUtil.getDistance(pointList.get(constant+1),ll)){
////                            save("距离: "+DistanceUtil.getDistance(pointList.get(constant+1),ll)+" 时间: "+getStringDate()+" 点数: "+constant);
////                            if(DistanceUtil.getDistance(pointList.get(constant+1),ll)>100&&isGetNewRoute){
////                                IsGetNewRoute();
////                            }
////                            constant++;
////                        }else{
////                            save("距离: "+DistanceUtil.getDistance(pointList.get(constant),ll)+" 时间: "+getStringDate()+" 点数: "+constant);
////                            if(DistanceUtil.getDistance(pointList.get(constant),ll)>100&&isGetNewRoute){
////                                IsGetNewRoute();
////                            }
////                        }
////                    }
//
//                        drawRealtimePoint(ll);
////                drawRealtimePoint(desLatLng);
//                    }else{
//                        showRealtimeTrack(location);
//                    }
//                    history.add(location);
//
////                StringBuffer sb = new StringBuffer(256);
////                sb.append("time : ");
////                sb.append(location.getTime());
////                sb.append("\nerror code : ");
////                sb.append(location.getLocType());
////                sb.append("\nlatitude : ");
////                sb.append(location.getLatitude());
////                sb.append("\nlontitude : ");
////                sb.append(location.getLongitude());
////                sb.append("\nradius : ");
////                sb.append(location.getRadius());
////                if (location.getLocType() == BDLocation.TypeGpsLocation){
////                    sb.append("\nspeed : ");
////                    sb.append(location.getSpeed());
////                    sb.append("\nsatellite : ");
////                    sb.append(location.getSatelliteNumber());
////                    sb.append("\ndirection : ");
////                    sb.append("\naddr : ");
////                    sb.append(location.getAddrStr());
////                    sb.append(location.getDirection());
////                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
////                    sb.append("\naddr : ");
////                    sb.append(location.getAddrStr());
////                    //ÔËÓªÉÌÐÅÏ¢
////                    sb.append("\noperationers : ");
////                    sb.append(location.getOperators());
////                }
////
////                Log.i("BaiduLocationApiDem", sb.toString());
//                }
//            });
//            mLocationClient.start();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        acquireWakeLock();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        releaseWakeLock();
    }

    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                //   Log.i(TAG, "call acquireWakeLock");
                wakeLock.acquire();
            }
        }
    }

    // ÊÍ·ÅÉè±¸µçÔ´Ëø
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            //   Log.i(TAG, "call releaseWakeLock");
            wakeLock.release();
            wakeLock = null;
        }
    }
}
