package com.example.fazhao.locationmanager.baidu_map.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.activity.HistoryMaps;
import com.example.fazhao.locationmanager.baidu_map.widget.HistoryDialog;
import com.example.fazhao.locationmanager.baidu_map.widget.SwipeDeleteListView1;
import com.example.fazhao.locationmanager.activity.TraceDao;
import com.example.fazhao.locationmanager.activity.TraceItem;
import com.example.fazhao.locationmanager.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.Acc;
import com.example.fazhao.locationmanager.baidu_map.StripListView;
import com.example.fazhao.locationmanager.baidu_map.adapter.BaseStripAdapter;
import com.example.fazhao.locationmanager.baidu_map.interfaces.TransferListener;
import com.example.fazhao.locationmanager.baidu_map.util.BaiduUtils;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * 此demo用来展示如何结合定位SDK实现室内定位，并使用MyLocationOverlay绘制定位位置
 */
public class IndoorLocationActivity extends Activity implements TransferListener {

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

    private Button compute,save,load;
    private CheckBox traffice,satelite,scale,scaleBtn;
    private ImageButton requestLocButton;
    public boolean isFirstLoc = true; // 是否首次定位

    protected  MapStatusUpdate msUpdate = null;
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
    private List<BDLocation> history = new ArrayList<>();
    private List<String> history_time = new ArrayList<>();
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
//    private BitmapDescriptor mBitmap;
    private int locTime = 0;
    private TraceDao mTraceDao;
    private TraceItem mTraceItem;
    private  int tag;
//    private AlertDialog historyDialog;
    private HistoryDialog historyDialog;
    private int mStep = 0;
    private List<Integer> history_step = new ArrayList<>();
    private Bundle bundle = new Bundle();
    private HistoryAdapter mAdapter;
    private View mFooterView;
    private Crypto crypto;
    private KeyManager km;
    private List<TraceItem> mDatas,mDatas1;
    private SensorEventListener mSensorEventListener;
    private Sensor mStepSensor;
    private SensorManager mSensorManager;
    private TextView step,info,historyTitle;
//    private LatLng latLng1;
//    private List<LatLng> historyFromLoad = BaseApplication.getHistory();
//    private List<TraceItem> traceItems;
    private BaiduReceiver mReceiver;
    private LocationClient mLocClient = BaseApplication.getmLocClient();
//    private LocationClientOption option = BaseApplication.getOption();
    private Handler handler;


    private Intent tmpIntent = new Intent();

    private  BDLocationListener mListener = new BDLocationListener(){

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            history.add(location);
            if(isFirstLoc){
                isFirstLoc=false;
                drawRealtimePoint(ll);
            }else{
                showRealtimeTrack(location);
            }
            Calendar now = Calendar.getInstance();
            String time = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH)
                    + "-" + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
            history_time.add(time);
            history_step.add(mStep);
            if(history.size() >= 2) {
                save.setClickable(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    save.setBackground(getResources().getDrawable(R.drawable.button_style));
                }
            }
            Log.e("address",String.valueOf(location.getAddress().address));
            if(location.getAddress().address != null)
            Log.e("addressBytes",String.valueOf(location.getAddress().address.getBytes()));
//            Log.e("time",String.valueOf(location.getTime()));
            Log.e("time",time);
            Log.e("latitude",String.valueOf(location.getLatitude()));
            Log.e("lontitude",String.valueOf(location.getLongitude()));
            Log.e("size",String.valueOf(history));
//                Log.e("getSatelliteNumber",String.valueOf(location.getSatelliteNumber()));

        }
    };


    private Runnable updateTitle = new Runnable() {
        @Override
        public void run() {
            historyTitle.setText("历史记录有" + (tag-1) + "数据");
        }
    };

//    private Handler updateDialog = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
////            historyDialog.setTitle("历史记录有" + (tag-1) + "数据");
//            ((TextView)(historyDialog.findViewById(R.id.history_num))).setText("历史记录有" + (tag-1) + "数据");
//            Log.e("tag",""+(tag-1));
//        }
//    };

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout layout = new RelativeLayout(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mainview = inflater.inflate(R.layout.activity_location_baidu, null);
        layout.addView(mainview);
        mFooterView = LayoutInflater.from(IndoorLocationActivity.this).inflate(R.layout.maps_list_footer, null);
        requestLocButton = (ImageButton) mainview.findViewById(R.id.button1);
        traffice = (CheckBox) mainview.findViewById(R.id.cb_traffic);
        scale = (CheckBox) mainview.findViewById(R.id.cb_scale);
        satelite = (CheckBox) mainview.findViewById(R.id.cb_satelite);
        scaleBtn = (CheckBox) mainview.findViewById(R.id.cb_scale_btn);
        traffice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if(b){
                    mBaiduMap.setTrafficEnabled(true);
                }else{
                    mBaiduMap.setTrafficEnabled(false);
                }
            }
        });
        satelite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if(b){
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }else{
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
            }
        });
        scale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if(b){
                    mMapView.showScaleControl(true);
                }else{
                    mMapView.showScaleControl(false);
                }
            }
        });
        /**
        * 缩放按钮
        * */
        scaleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if(b){
                    mMapView.showZoomControls(true);
                }else{
                    mMapView.showZoomControls(false);
                }
            }
        });
        mCurrentMode = LocationMode.COMPASS;
//        requestLocButton.setText("普通");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
//                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        Toast.makeText(IndoorLocationActivity.this,"跟随模式",Toast.LENGTH_SHORT).show();
                        break;
                    case COMPASS:
//                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        Toast.makeText(IndoorLocationActivity.this,"普通模式",Toast.LENGTH_SHORT).show();
                        break;
                    case FOLLOWING:
//                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        Toast.makeText(IndoorLocationActivity.this,"罗盘模式",Toast.LENGTH_SHORT).show();
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
        BaseApplication.registerListener(mListener);
        stripListView = new StripListView(this);
        layout.addView(stripListView);
        setContentView(layout);
        step = (TextView) findViewById(R.id.steps);
        mTraceDao = BaseApplication.getmTaceDao();
        initData();
        info = (TextView) findViewById(R.id.et_streetView);
        if(BaseApplication.isHasHistory()){
                List<String> time = BaseApplication.getTime();
                List<TraceItem> distance = BaseApplication.getDistance();
                List<String> route = BaseApplication.getRoute();
            try {
                LatLng latLng = new LatLng(distance.get(0).getLatitude(), distance.get(0).getLongitude());
                LatLng latLng1 = new LatLng(distance.get(1).getLatitude(), distance.get(1).getLongitude());
                /**
                 * 多表查询
                 * */
//            出发地:route.get(0),目的地:route.get(1)
                info.setText("上次定位出发地:" + route.get(0) + ",目的地:" + route.get(1) + ",距离:" + String.valueOf(DistanceUtil.getDistance(latLng, latLng1)) + ",时长：" + BaiduUtils.dateDiff(this, time.get(0), time.get(1), "yyyy-MM-dd-HH:mm:ss", "m")
                        + "分钟" + ",步数:" + BaseApplication.getLastStep());
            }catch (Exception e ){

            }
        }
        save = (Button) findViewById(R.id.btn_save);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(IndoorLocationActivity.this, "saving.....", Toast.LENGTH_SHORT).show();
                save.setEnabled(false);
                Thread saveThread = new Thread(saveHistory);
                saveThread.start();
                save.setEnabled(true);
                Toast.makeText(IndoorLocationActivity.this, "saved!", Toast.LENGTH_SHORT).show();
            }
        });
        save.setClickable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            save.setBackground(getResources().getDrawable(R.drawable.button_gray));
        }
        load = (Button) findViewById(R.id.btn_load);
        load.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 查询有多少次
                 * */
                HandlerThread thread = new HandlerThread("MyThread");
                thread.start();
                final Handler handler = new Handler(thread.getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:
                                if(historyDialog == null)
                                    historyDialog = new HistoryDialog(IndoorLocationActivity.this);
                                mAdapter = new HistoryAdapter(IndoorLocationActivity.this, mDatas, mDatas1, historyDialog.lv);
                                historyDialog.lv.setAdapter(mAdapter);
                                historyDialog.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        /**
                                         * listview是从0开始，但是我的tag是从1开始，所以position+1
                                         * */
//                                        if(lv.isMoved()){
//                                            lv.setMoved(false);
//                                            lv.turnToNormal();
//                                        }else {
                                        bundle.putInt("choice", position + 1);
//                                        Log.e("choice",String.valueOf(position + 1));
                                        Intent it = new Intent();
                                        it.setClass(IndoorLocationActivity.this, HistoryMaps.class);
                                        it.putExtras(bundle);
                                        startActivity(it);
                                        finish();
//                                        }
                                    }
                                });
                                historyDialog.setOnDeleteAllListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mTraceDao.deleteAll();
                                        mAdapter.notifyDataSetChanged();
                                        historyDialog.title.setText("历史记录有0数据");
                                    }
                                });
                                historyDialog.setOnNegativeListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        historyDialog.cancel();
                                    }
                                });
//                                historyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                historyDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                historyDialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
                                /**
                                 * 显示历史的dialog泄露了内存，因为我显示历史后会点击listview的item
                                 * 然后就会finish这个activity而进入historyActivity
                                 * 但是这个dialog在activity退出之前没有进行dismiss所以泄漏了
                                 * */
                                historyDialog.show();
                                historyTitle = ((TextView)(historyDialog.findViewById(R.id.history_num)));
                                historyTitle.setText("历史记录有" + tag + "数据");
                                break;
                        }
                    }
                };
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tag = mTraceDao.maxTag();
                        mDatas = mTraceDao.searchDistinctDataStart();
                        mDatas1 = mTraceDao.searchDistinctDataDestination();
                        handler.sendEmptyMessage(0);
                    }
                };
                handler.post(runnable);
//
            }
        });
        compute = (Button) findViewById(R.id.btn_compute);
        compute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tmp = (int) DistanceUtil.getDistance(pointList.get(constant),ll);
                Toast.makeText(IndoorLocationActivity.this,"pointList:"+pointList.get(constant)+"ll:"+ll+"距离:"+tmp,Toast.LENGTH_SHORT).show();
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
        /**
        * 监听网络以及百度地图的key是否注册了
        * */
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new BaiduReceiver();
        registerReceiver(mReceiver, iFilter);
        tmpIntent.setAction("com.fazhao.locationservice");
        Intent serviceIt = new Intent(createExplicitFromImplicitIntent(this,tmpIntent));
        startService(serviceIt);
    }

    private void initData() {
        km = new KeyManager(IndoorLocationActivity.this);
        final String key = "12345678909876543212345678909876";
        final String iv = "1234567890987654";
        km.setIv(iv.getBytes());
        km.setId(key.getBytes());
        crypto = new Crypto(this);
    }

    final Runnable saveHistory = new Runnable() {
        @Override
        public void run() {
            if (mTraceDao.searchAllData() != null)
                tag = mTraceDao.maxTag() + 1;

            /**
            * 插入数据到多表
            * */
            try {
                int history_size = history.size()-1;
                mTraceDao.addTime(crypto.armorEncrypt(history_time.get(0).getBytes())
                        ,crypto.armorEncrypt(history_time.get(history_size).getBytes()),tag);
                if(history.get(0).getAddress().address != null
                        && history.get(history_size).getAddress().address != null)
                mTraceDao.addRoute(crypto.armorEncrypt(history.get(0).getAddress().address.getBytes())
                        ,crypto.armorEncrypt(history.get(history_size).getAddress().address.getBytes()),tag);
                mTraceDao.addDistance(history.get(0).getLatitude(),history.get(history_size).getLatitude()
                        ,history.get(0).getLongitude(),history.get(history_size).getLongitude(),tag);
                for (int i = 0; i < history.size(); i++) {
                    mTraceItem = new TraceItem();
                    if(history.get(i).getAddress().address != null)
                        mTraceItem.setAddress(crypto.armorEncrypt(history.get(i).getAddress().address.getBytes()));
                        mTraceItem.setLatitude(history.get(i).getLatitude());
                        mTraceItem.setLongitude(history.get(i).getLongitude());
                        mTraceItem.setTag(tag);
                        /**
                         * 这里是导致一个tag的所有记录的时间都是相同的，所以时间差为0或很接近
                         * 应该把这里放到onlocationchanged里（history里setDate）
                         * 而这里就拿history的date
                         * */
                        mTraceItem.setDate(crypto.armorEncrypt(history_time.get(i).getBytes()));
                        /**
                         * 在最后一个插入步数
                         * 如果是0也插入，证明不是走路（是交通工具）
                         * */
                        mTraceItem.setStep(history_step.get(i));
//                        }
                        mTraceDao.add(mTraceItem);
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
            BaseApplication.setHasHistory(true);
        }
    };

    /*
	 * 显示实时轨迹
	 *
	 * @param realtimeTrack
	 */
    protected void showRealtimeTrack(BDLocation location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude,longitude);
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            Toast.makeText(IndoorLocationActivity.this, "当前无轨迹点", Toast.LENGTH_SHORT).show();
        }
//        else if(DistanceUtil.getDistance(pointList.get(pointList.size() - 1),ll)>50){
//            5秒走50米是不可能的，所以该定位点舍弃
//        }
        else {
            latLng = new LatLng(latitude, longitude);
            pointList.add(latLng);
                // 绘制实时点
                drawRealtimePoint(latLng);
//            }
        }

    }

    @Override
    public void click() {
        if(handler == null)
            handler = new Handler();
        handler.post(updateTitle);
    }


    public class BaiduReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(IndoorLocationActivity.this,"key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(IndoorLocationActivity.this);
                builder.setTitle("当前没有网络")
                        .setMessage("您是否进入系统设置开启网络?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface anInterface, int i) {
                                Intent it = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(it);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface anInterface, int i) {

                            }
                        })
                        .show();
            } else if (s
                    .equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Toast.makeText(IndoorLocationActivity.this,"网络出错",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 绘制实时点
     *
     * @param point
     */
    private void drawRealtimePoint(LatLng point) {

        mBaiduMap.clear();
        polyline=null;
        MapStatus mMapStatus = new MapStatus.Builder().target(point)
                .zoom(20)
                .build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);


        if (pointList.size() >=2 && pointList.size() <= 100000) {
            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }
        mBaiduMap.animateMapStatus(msUpdate);
        addMarker();

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


    @Override
    protected void onPause() {
//        mMapView.onPause();
        if(historyDialog!=null)
            historyDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
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
                    StringBuilder builder = new StringBuilder("步数:");
                    builder.append(Integer.toString(mStep));
                    step.setText(builder);
                }
            };
            /**
             * 如果设置SENSOR_DELAY_FASTEST会浪费电的
             * */
            mSensorManager.registerListener(mSensorEventListener, mStepSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(mSensorEventListener);
        mLocClient.unRegisterLocationListener(mListener);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        unregisterReceiver(mReceiver);
        tmpIntent.setAction("com.fazhao.locationservice");
        Intent serviceIt = new Intent(createExplicitFromImplicitIntent(this,tmpIntent));
        stopService(serviceIt);
        /**
        * 在这里关闭db
        * */
        BaseApplication.getDbHelper().close();
        super.onDestroy();
    }

}

