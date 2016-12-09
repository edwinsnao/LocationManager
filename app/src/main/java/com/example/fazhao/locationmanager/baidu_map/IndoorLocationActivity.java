package com.example.fazhao.locationmanager.baidu_map;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.fazhao.locationmanager.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 此demo用来展示如何结合定位SDK实现室内定位，并使用MyLocationOverlay绘制定位位置
 */
public class IndoorLocationActivity extends Activity {

    // 定位相关
    LocationClient mLocClient;
//    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    /**
     * 获取位置距离常量
     */
    private int constant=0;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private StripListView stripListView;
    private BaseStripAdapter mFloorListAdapter;
    private MapBaseIndoorMapInfo mMapBaseIndoorMapInfo = null;
    // UI相关

    private Button requestLocButton,compute;
    boolean isFirstLoc = true; // 是否首次定位

    protected MapStatusUpdate msUpdate = null;
    /**
     *  覆盖物
     */
    protected OverlayOptions overlay,StartOverlay,EndOverlay;
    /**
     *  路线覆盖物
     */
    private PolylineOptions polyline = null;
    /**
     * 手机加速度感应器服务注册
     */
    private Acc acc=new Acc();
    private LatLng latLng,ll;
    private List<LatLng> pointList = new ArrayList<LatLng>();
    /**
     * 最小距离单位(米)
     */
    private final Double MinDistance=2.0;
    /**
     * 因距离太大丢失的点数
     */
    private int LostLoc=0;
    /**
     * 最大距离单位(米)
     */
    private final Double MaxDistance=90.00;
    /**
     * 第一次定位丢失的点数
     */
    private int FLostLoc=0;
    private int tmp;
    private BitmapDescriptor mBitmap;
    private int locTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout layout = new RelativeLayout(this);

        mBitmap = BitmapDescriptorFactory.fromResource(R.mipmap.map_d);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainview = inflater.inflate(R.layout.activity_location_baidu, null);
        layout.addView(mainview);

        requestLocButton = (Button) mainview.findViewById(R.id.button1);
        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) mainview.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启室内图
        mBaiduMap.setIndoorEnable(true);
        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext());
//        mLocClient.registerLocationListener(myListener);
        mLocClient.registerLocationListener(new BDLocationListener(){

            @Override
            public void onReceiveLocation(BDLocation location) {
                Log.e("locClient", String.valueOf(isFirstLoc));
                Log.e("locTime", String.valueOf(locTime++));
                // TODO Auto-generated method stub
                //				locData = new MyLocationData.Builder()
                //				.accuracy(0)
                //				// 此处设置开发者获取到的方向信息，顺时针0-360
                //				.direction(0).latitude(location.getLatitude())
                //				.longitude(location.getLongitude()).build();
                //				// 设置定位数据
                //				BaiDuMap.setMyLocationData(locData);
                ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                if(isFirstLoc){
                    isFirstLoc=false;
//                    if(constant<pointList.size()){
//                        if(DistanceUtil.getDistance(pointList.get(constant),ll)>DistanceUtil.getDistance(pointList.get(constant+1),ll)){
//                            save("距离: "+DistanceUtil.getDistance(pointList.get(constant+1),ll)+" 时间: "+getStringDate()+" 点数: "+constant);
//                            if(DistanceUtil.getDistance(pointList.get(constant+1),ll)>100&&isGetNewRoute){
//                                IsGetNewRoute();
//                            }
//                            constant++;
//                        }else{
//                            save("距离: "+DistanceUtil.getDistance(pointList.get(constant),ll)+" 时间: "+getStringDate()+" 点数: "+constant);
//                            if(DistanceUtil.getDistance(pointList.get(constant),ll)>100&&isGetNewRoute){
//                                IsGetNewRoute();
//                            }
//                        }
//                    }

                    drawRealtimePoint(ll);
                }else{
                    showRealtimeTrack(location);
                }
                Log.e("address",String.valueOf(location.getAddress().address));
                Log.e("time",String.valueOf(location.getTime()));
                Log.e("latitude",String.valueOf(location.getLatitude()));
                Log.e("lontitude",String.valueOf(location.getLongitude()));

            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        option.disableCache(false);
        option.setNeedDeviceDirect(true);
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);
        mLocClient.start();

        stripListView = new StripListView(this);
        layout.addView(stripListView);
        setContentView(layout);
        compute = (Button) findViewById(R.id.btn_compute);
        compute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tmp = (int) DistanceUtil.getDistance(pointList.get(constant),ll);
                Toast.makeText(IndoorLocationActivity.this,"距离:"+tmp,Toast.LENGTH_SHORT).show();
            }
        });
        mFloorListAdapter = new BaseStripAdapter(IndoorLocationActivity.this);

        mBaiduMap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b == false || mapBaseIndoorMapInfo == null) {
                    stripListView.setVisibility(View.INVISIBLE);

                    return;
                }

                mFloorListAdapter.setmFloorList(mapBaseIndoorMapInfo.getFloors());
                stripListView.setVisibility(View.VISIBLE);
                stripListView.setStripAdapter(mFloorListAdapter);
                mMapBaseIndoorMapInfo = mapBaseIndoorMapInfo;
            }
        });
    }


    /*
	 * 显示实时轨迹
	 *
	 * @param realtimeTrack
	 */
    protected void showRealtimeTrack(BDLocation location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            Toast.makeText(this, "当前无轨迹点", Toast.LENGTH_SHORT).show();
        } else {
            latLng = new LatLng(latitude, longitude);
            if (IsMove(latLng,location)) {
                // 绘制实时点
                drawRealtimePoint(latLng);
            }
        }

    }

    /*
     * 绘制实时点
     *
     * @param points
     */
    private void drawRealtimePoint(LatLng point) {

        mBaiduMap.clear();
        polyline=null;
        MapStatus mMapStatus = new MapStatus.Builder().target(point).build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);


        overlay = new MarkerOptions().position(point)
                .icon(mBitmap).zIndex(9).draggable(true);

        if (pointList.size() >=2 && pointList.size() <= 100000) {
            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }

        addMarker();

    }

    /*
	 * 判断手机是否在运动
	 */
    private boolean IsMove(LatLng latLng,BDLocation location){
//        Log.e("locTime", String.valueOf(locTime++));
        if(pointList.size()>=1){
            Double dis= DistanceUtil.getDistance(pointList.get(pointList.size()-1),latLng);
            //判断手机是否静止,如果静止,判定采集点无效,直接抛弃
            if(!acc.is_Acc&&acc.IsRun){
                acc.IsRun=false;
                return false;
            }
            //判断是否是第一次定位置,如果是第一次定位并且因为第一次抛弃的位置数量小于10个则判断两点间距离大小
            if(FLostLoc<10){
                FLostLoc=FLostLoc+1;
                if(dis>10&&FLostLoc<6){//距离大于十米,而且被抛弃数量少于5个则说明有可能是获取位置失败
                    pointList.clear();
                    pointList.add(latLng);//更新位置
                    return false;
                }
                if(dis>0&&dis<10&&FLostLoc>=6)//如果距离在10米内,则表示客户正在运动,直接跳出
                    FLostLoc=11;
            }
            //根据两点间距离判断是否发生定位漂移,如果漂移距离小于MinDistance则抛弃,如果漂移距离大于MaxDistance则取两点的中间点.
            if(dis<=MinDistance){
                if((dis<=MinDistance||dis>=MaxDistance)){
                    return false;
                }

                if(LostLoc>=4){
                    Double newlatitude=(latLng.latitude+pointList.get(pointList.size()-1).latitude)/2;
                    Double newlongitude=(latLng.longitude+pointList.get(pointList.size()-1).longitude)/2;
                    latLng = new LatLng(newlatitude, newlongitude);
                }else{
                    LostLoc=LostLoc+1;
                    return false;
                }

            }
            LostLoc=0;//重置丢失点的个数
            //			pointList.add(latLng);
            acc.is_Acc=false;
        }
        pointList.add(latLng);
        return true;
    }

    /*
	 * 添加地图覆盖物
	 */
    protected  void addMarker() {

        if (null != msUpdate) {
            mBaiduMap.setMapStatus(msUpdate);
        }

        // 路线覆盖物
        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }

        // 实时点覆盖物
        if (null != overlay) {
            mBaiduMap.addOverlay(overlay);
        }

        //起点覆盖物
        if (null != StartOverlay) {
            mBaiduMap.addOverlay(StartOverlay);
        }
        // 终点覆盖物
        if (null != EndOverlay) {
            mBaiduMap.addOverlay(EndOverlay);
        }
    }

    /**
     * 定位SDK监听函数
     */
//    public class MyLocationListenner implements BDLocationListener {
//
//        private String lastFloor = null;
//
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            // map view 销毁后不在处理新接收的位置
//            if (location == null || mMapView == null) {
//                return;
//            }
//            String bid = location.getBuildingID();
//            if (bid != null && mMapBaseIndoorMapInfo != null) {
//                Log.i("indoor", "bid = " + bid + " mid = " + mMapBaseIndoorMapInfo.getID());
//                if (bid.equals(mMapBaseIndoorMapInfo.getID())) {// 校验是否满足室内定位模式开启条件
//                    // Log.i("indoor","bid = mMapBaseIndoorMapInfo.getID()");
//                    String floor = location.getFloor().toUpperCase();// 楼层
//                    Log.i("indoor", "floor = " + floor + " position = " + mFloorListAdapter.getPosition(floor));
//                    Log.i("indoor", "radius = " + location.getRadius() + " type = " + location.getNetworkLocationType());
//
//                    boolean needUpdateFloor = true;
//                    if (lastFloor == null) {
//                        lastFloor = floor;
//                    } else {
//                        if (lastFloor.equals(floor)) {
//                            needUpdateFloor = false;
//                        } else {
//                            lastFloor = floor;
//                        }
//                    }
//                    if (needUpdateFloor) {// 切换楼层
//                        mBaiduMap.switchBaseIndoorMapFloor(floor, mMapBaseIndoorMapInfo.getID());
//                        mFloorListAdapter.setSelectedPostion(mFloorListAdapter.getPosition(floor));
//                        mFloorListAdapter.notifyDataSetInvalidated();
//                    }
//
//                    if (!location.isIndoorLocMode()) {
//                        mLocClient.startIndoorMode();// 开启室内定位模式，只有支持室内定位功能的定位SDK版本才能调用该接口
//                        Log.i("indoor", "start indoormod");
//                    }
//                }
//            }
//
//            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
//            // 此处设置开发者获取到的方向信息，顺时针0-360
//                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
//            mBaiduMap.setMyLocationData(locData);
//            if (isFirstLoc) {
//                isFirstLoc = false;
//                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
//                MapStatus.Builder builder = new MapStatus.Builder();
//                builder.target(ll).zoom(18.0f);
//                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//            }
//        }
//
//        public void onReceivePoi(BDLocation poiLocation) {
//        }
//    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

}
