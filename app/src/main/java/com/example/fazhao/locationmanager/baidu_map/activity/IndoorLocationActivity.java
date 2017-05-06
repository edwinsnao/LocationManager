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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.baidu_map.mail.Mail;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.service.StepService;
import com.example.fazhao.locationmanager.baidu_map.widget.HistoryDialog;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.baidu_map.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.widget.StripListView;
import com.example.fazhao.locationmanager.baidu_map.adapter.BaseStripAdapter;
import com.example.fazhao.locationmanager.baidu_map.interfaces.TransferListener;
import com.example.fazhao.locationmanager.baidu_map.util.BaiduUtils;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * 此demo用来展示如何结合定位SDK实现室内定位，并使用MyLocationOverlay绘制定位位置
 */
public class IndoorLocationActivity extends Activity implements TransferListener {

    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    /**
     * 获取位置距离常量
     */
    private int constant = 0;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private StripListView stripListView;
    private BaseStripAdapter mFloorListAdapter;
    private View mFooterView;

    private Button compute, save, load;
    private CheckBox traffice, satelite, scale, scaleBtn;
    private ImageButton requestLocButton;
    public boolean isFirstLoc = true; // 是否首次定位

    protected MapStatusUpdate msUpdate = null;
    /**
     * 覆盖物
     */
    protected OverlayOptions overlay, StartOverlay, EndOverlay;
    /**
     * 路线覆盖物
     */
    private PolylineOptions polyline = null;
    private LatLng ll;
    private List<BDLocation> history = new ArrayList<>();
    private List<String> history_time = new ArrayList<>();
    private List<LatLng> pointList = new ArrayList<LatLng>();
    private int tmp;
    private TraceDao mTraceDao;
    private TraceItem mTraceItem;
    private int tag, maxTag;
    private HistoryDialog historyDialog;
    private int mStep = 0;
    private List<Integer> history_step = new ArrayList<>();
    private Bundle bundle = new Bundle();
    private HistoryAdapter mAdapter;
    private Crypto crypto;
    private KeyManager km;
    private List<TraceItem> mDatas, mDatas1;
    private TextView step, info, historyTitle;
    private BaiduReceiver mReceiver;
    private LocationClient mLocClient = BaseApplication.getmLocClient();
    private LocationClientOption mOption = BaseApplication.getOption();
    private Handler handler;
    private Intent mStepService;
    private MapBaseIndoorMapInfo mMapBaseIndoorMapInfo;
    private Intent tmpIntent = new Intent();
    public static Handler mHandler;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    public static String reverseAddress;
    public static GeoCoder geoCoder;
    private Timer mTimer;
    private String from = BaseApplication.getFrom();
    private String to = BaseApplication.getTo();
    private String server = BaseApplication.getmServer();
    private String subject = BaseApplication.getmSubject();
    private String pwd = BaseApplication.getmPwd();

    private BDLocationListener mListener = new BDLocationListener() {

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
            if (isFirstLoc) {
                /**
                 * 如果是第一次定位就发送位置
                 * */
                if (isConnected()) {
                    try {
                        String content = "位置:" + location.getAddress().address + " 时间:" + location.getTime();
                        Mail mail = new Mail(server, from, pwd);
                        mail.create(from, to, subject);
                        mail.addContent(content);
                        mail.send();
                    Log.e("Send OK!", location.getAddress().address);
//                    Log.e("Send OK!", String.valueOf(location.getAddress()));
                        Toast.makeText(IndoorLocationActivity.this, "Send OK1!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isFirstLoc = false;
                drawRealtimePoint(ll);
            } else {
                showRealtimeTrack(location);
            }
            Calendar now = Calendar.getInstance();
            String time = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH)
                    + "-" + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
            history_time.add(time);
            history_step.add(mStep);
            if (history.size() >= 2) {
                save.setClickable(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    save.setBackground(getResources().getDrawable(R.drawable.button_style));
                }
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    };


    private Runnable updateTitle = new Runnable() {
        @Override
        public void run() {
            historyTitle.setText("历史记录有" + (maxTag) + "数据");
        }
    };

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        Intent explicitIntent = new Intent(implicitIntent);

        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void startStepService() {
        mStepService = new Intent(IndoorLocationActivity.this, StepService.class);
        startService(mStepService);
    }

    public void stopStepService() {
        stopService(mStepService);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
//            super.openOptionsMenu();
            Intent intent = new Intent();
            intent.setClass(IndoorLocationActivity.this,CustomPreferenceActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void openOptionsMenu() {
        super.openOptionsMenu();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.file:
                Intent intent = new Intent();
                intent.setClass(IndoorLocationActivity.this,CustomPreferenceActivity.class);
                startActivity(intent);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private final static String FROMPATH = "/data/data/com.example.fazhao.locationmanager/databases/trace.db";
    private final static String TOPATH = "/storage/sdcard0/trace.db";

    public static boolean copyFile(String source, String dest) {
        try {
            File f1 = new File(source);
            File f2 = new File(dest);
            InputStream in = new FileInputStream(f1);

            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.setmHasLaunch(true);
        this.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startStepService();
//        copyFile(FROMPATH,TOPATH);
        RelativeLayout layout = new RelativeLayout(this);
        mLocClient.setLocOption(mOption);
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
                if (b) {
                    mBaiduMap.setTrafficEnabled(true);
                } else {
                    mBaiduMap.setTrafficEnabled(false);
                }
            }
        });
        satelite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if (b) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                } else {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
            }
        });
        scale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if (b) {
                    mMapView.showScaleControl(true);
                } else {
                    mMapView.showScaleControl(false);
                }
            }
        });
        scaleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if (b) {
                    mMapView.showZoomControls(true);
                } else {
                    mMapView.showZoomControls(false);
                }
            }
        });
        mCurrentMode = LocationMode.COMPASS;
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        Toast.makeText(IndoorLocationActivity.this, "跟随模式", Toast.LENGTH_SHORT).show();
                        break;
                    case COMPASS:
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        Toast.makeText(IndoorLocationActivity.this, "普通模式", Toast.LENGTH_SHORT).show();
                        break;
                    case FOLLOWING:
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        Toast.makeText(IndoorLocationActivity.this, "罗盘模式", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        mMapView = (MapView) mainview.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setIndoorEnable(true);
        BaseApplication.registerListener(mListener);
        stripListView = new StripListView(this);
        layout.addView(stripListView);
        setContentView(layout);
        step = (TextView) findViewById(R.id.steps);
        mTraceDao = BaseApplication.getmTaceDao();
        initData();
        info = (TextView) findViewById(R.id.et_streetView);
        maxTag = mTraceDao.maxTag();
        if (BaseApplication.isHasHistory()) {
//            List<String> time = BaseApplication.getTime();
//            double distance = BaseApplication.getDistance();
            double distance = mTraceDao.getDistance(maxTag);
            long time = mTraceDao.getTime(maxTag);
            List<String> route = BaseApplication.getRoute();
            try {
                info.setText("上次定位出发地:" + route.get(0) + ",目的地:" + route.get(1) + ",距离:" +
                        String.valueOf(distance) +" 米" + ",时长：" + String.valueOf(time)
                        + "秒" + ",步数:" + BaseApplication.getLastStep());
            } catch (Exception e) {
                e.printStackTrace();
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
                HandlerThread thread = new HandlerThread("MyThread");
                thread.start();
                final Handler handler = new Handler(thread.getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:
                                maxTag = mTraceDao.maxTag();
                                if (historyDialog == null) {
                                    historyDialog = new HistoryDialog(IndoorLocationActivity.this);
                                }
                                mAdapter = new HistoryAdapter(IndoorLocationActivity.this, mDatas, mDatas1, historyDialog.lv);
                                historyDialog.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> view, View view1, int pos, long l) {
                                        switch (pos) {
                                            case 0:
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDatas.clear();
                                                        mDatas1.clear();
                                                        mDatas.addAll(mTraceDao.searchDistinctDataStart());
                                                        mDatas1.addAll(mTraceDao.searchDistinctDataDestination());
//                                                        mDatas = mTraceDao.searchDistinctDataStart();
//                                                        mDatas1 = mTraceDao.searchDistinctDataDestination();

                                                        mAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                break;
                                            case 1:
                                                historyDialog.getSpinner().setSelection(pos,false);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDatas.clear();
                                                        mDatas1.clear();
                                                        mDatas.addAll(mTraceDao.searchDistinctDataStartForTime());
                                                        mDatas1.addAll(mTraceDao.searchDistinctDataDestinationForTime());
//                                                        mDatas = mTraceDao.searchDistinctDataStartForTime();
//                                                        mDatas1 = mTraceDao.searchDistinctDataDestinationForTime();
                                                        mAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                break;
                                            case 2:
                                                historyDialog.getSpinner().setSelection(pos,false);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDatas.clear();
                                                        mDatas1.clear();
                                                        mDatas.addAll(mTraceDao.searchDistinctDataStartForDistance());
                                                        mDatas1.addAll(mTraceDao.searchDistinctDataDestinationForDistance());
//                                                        mDatas = mTraceDao.searchDistinctDataStartForDistance();
//                                                        mDatas1 = mTraceDao.searchDistinctDataDestinationForDistance();
                                                        mAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                break;
                                            case 3:
                                                historyDialog.getSpinner().setSelection(pos,false);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDatas.clear();
                                                        mDatas1.clear();
                                                        mDatas.addAll(mTraceDao.searchDistinctDataStartForStep());
                                                        mDatas1.addAll(mTraceDao.searchDistinctDataDestinationForStep());
//                                                        mDatas = mTraceDao.searchDistinctDataStartForStep();
//                                                        mDatas1 = mTraceDao.searchDistinctDataDestinationForStep();
                                                        mAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                break;
                                            default:
                                                ;
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> view) {

                                    }

                        });
                                historyDialog.lv.setAdapter(mAdapter);
                                historyDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface anInterface) {
                                        historyDialog.dismiss();
                                        historyDialog = null;
                                    }
                                });
                                historyDialog.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        /**
                                         * listview是从0开始，但是我的tag是从1开始，所以position+1
                                         * */
                                        bundle.putInt("choice", position + 1);
                                        bundle.putDouble("latitude", pointList.get(pointList.size() - 1).latitude);
                                        bundle.putDouble("longitude", pointList.get(pointList.size() - 1).longitude);
                                        Intent it = new Intent();
                                        it.setClass(IndoorLocationActivity.this, HistoryMaps.class);
                                        it.putExtras(bundle);
                                        startActivity(it);
                                        finish();
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
                                historyDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                historyDialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
                                /**
                                 * 显示历史的dialog泄露了内存，因为我显示历史后会点击listview的item
                                 * 然后就会finish这个activity而进入historyActivity
                                 * 但是这个dialog在activity退出之前没有进行dismiss所以泄漏了
                                 * */
                                historyDialog.show();
                                historyTitle = ((TextView) (historyDialog.findViewById(R.id.history_num)));
                                historyTitle.setText("历史记录有" + maxTag + "数据");
                                break;
                        }
                    }
                };
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
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
                /**
                 * 不是取首尾的距离，是每次定位的距离之和
                 * */
                if (pointList.size() >= 2) {
                    for (int i = 1; i < pointList.size() - 1; i++) {
                        tmp += (int) DistanceUtil.getDistance(pointList.get(i), pointList.get(i - 1));
                    }
                    Toast.makeText(IndoorLocationActivity.this, "pointList:" + pointList.get(constant) + "ll:" + ll + "距离:" + tmp, Toast.LENGTH_SHORT).show();
                }
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
        Intent serviceIt = new Intent(createExplicitFromImplicitIntent(this, tmpIntent));
        startService(serviceIt);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                StringBuilder mStepCount = new StringBuilder("步数:");
                mStepCount.append(msg.obj);
                mStep = (int) msg.obj;
                step.setText(mStepCount);
            }
        };
        powerManager = (PowerManager) this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        geoCoder = GeoCoder.newInstance();
        //
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            // 反地理编码查询结果回调函数
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                    Toast.makeText(IndoorLocationActivity.this, "抱歉，未能找到结果",
                            Toast.LENGTH_LONG).show();
                }
                Toast.makeText(IndoorLocationActivity.this,
                        "位置：" + result.getAddress(), Toast.LENGTH_LONG)
                        .show();
                reverseAddress = result.getAddress();
            }

            // 地理编码查询结果回调函数
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                }
            }
        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isConnected())
                    try {
                        StringBuilder content = new StringBuilder("");
                        for (int i = 0; i < history.size(); i++) {
                            content.append("位置:" + history.get(i).getAddress().address);
                            content.append(" 时间:" + history_time.get(i) + "\n");
                        }
                        Mail mail = new Mail(server, from, pwd);
                        mail.create(from, to, subject);
                        mail.addContent(content.toString());
                        mail.send();
                        Toast.makeText(IndoorLocationActivity.this, "Send OK!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        };
        mTimer = new Timer();
        mTimer.schedule(task, 1000, 30000);
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
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
            if (mTraceDao.searchAllData() != null) {
                tag = mTraceDao.maxTag() + 1;
                maxTag = tag - 1;
            }
            /**
             * 插入数据到多表
             * */
            try {
                int history_size = history.size() - 1;
                mTraceDao.addTime(BaiduUtils.dateDiffForSecond(IndoorLocationActivity.this,
                        history_time.get(0),history_time.get(history_size), "yyyy-MM-dd-HH:mm:ss")
                        , crypto.armorEncrypt(history_time.get(0).getBytes())
                        , crypto.armorEncrypt(history_time.get(history_size).getBytes()), tag);
                if (history.get(0).getAddress().address != null
                        && history.get(history_size).getAddress().address != null) {
                    mTraceDao.addRoute(crypto.armorEncrypt(history.get(0).getAddress().address.getBytes())
                            , crypto.armorEncrypt(history.get(history_size).getAddress().address.getBytes()), tag);
                } else {
                    mTraceDao.addRoute(crypto.armorEncrypt("没有联网下定位导致无法获取地址名称".getBytes())
                            , crypto.armorEncrypt("没有联网下定位导致无法获取地址名称".getBytes()), tag);
                }
                for (int i = 1; i < pointList.size() - 1; i++) {
                    tmp += (int) DistanceUtil.getDistance(pointList.get(i), pointList.get(i - 1));
                }
                mTraceDao.addDistance(tmp, tag);
                for (int i = 0; i < history.size(); i++) {
                    mTraceItem = new TraceItem();
                    if (history.get(i).getAddress().address != null) {
                        mTraceItem.setAddress(crypto.armorEncrypt(history.get(i).getAddress().address.getBytes()));
                    } else {
                        mTraceItem.setAddress(crypto.armorEncrypt("没有联网下定位导致无法获取地址名称".getBytes()));
                    }
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
        }
    };

    /**
     * 显示实时轨迹
     */
    protected void showRealtimeTrack(BDLocation location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            Toast.makeText(IndoorLocationActivity.this, "当前无轨迹点", Toast.LENGTH_SHORT).show();
        } else if (DistanceUtil.getDistance(pointList.get(pointList.size() - 1), ll) > 100) {
//            5秒走50米是不可能的，所以该定位点舍弃
            Toast.makeText(IndoorLocationActivity.this, "discard", Toast.LENGTH_SHORT).show();
        } else {
            latLng = new LatLng(latitude, longitude);
            /**
             * 因为drawRealTime里面已经有一个pointList.add
             * 所以下面会导致重复的
             * */
//            pointList.add(latLng);
            // 绘制实时点
            drawRealtimePoint(latLng);
        }

    }

    @Override
    public void click() {
        if (handler == null)
            handler = new Handler();
        handler.post(updateTitle);
    }


    public class BaiduReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(IndoorLocationActivity.this, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(IndoorLocationActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 绘制实时点
     *
     * @param point
     */
    private void drawRealtimePoint(LatLng point) {

        /**
         * 这一句会导致没有东西
         * */
//        mBaiduMap.clear();
        polyline = null;
        MapStatus mMapStatus = new MapStatus.Builder().target(point)
                .zoom(20)
                .build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        if (pointList.size() >= 2 && pointList.size() <= 100000) {
            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }
        /**
         * 不要忘记加入pointList
         * */
        pointList.add(point);
        mBaiduMap.animateMapStatus(msUpdate);
        addMarker();

    }

    /**
     * 添加地图覆盖物
     */
    protected void addMarker() {
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
        if (historyDialog != null) {
            historyDialog.dismiss();
            historyDialog = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        wakeLock.acquire();
    }

    @Override
    protected void onDestroy() {
        BaseApplication.setmHasLaunch(false);
        mLocClient.unRegisterLocationListener(mListener);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        unregisterReceiver(mReceiver);
        tmpIntent.setAction("com.fazhao.locationservice");
        Intent serviceIt = new Intent(createExplicitFromImplicitIntent(this, tmpIntent));
        stopService(serviceIt);
        /**
         * 在这里关闭db
         * */
//        BaseApplication.getDbHelper().close();
        stopStepService();
        wakeLock.release();
        mTimer.cancel();
        super.onDestroy();
    }
}

