package com.example.fazhao.locationmanager.baidu_map.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.baidu_map.adapter.PicAdapter;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.baidu_map.util.BaiduUtils;
import com.example.fazhao.locationmanager.baidu_map.util.ToastUtil;
import com.example.fazhao.locationmanager.baidu_map.widget.HistoryDialog;
import com.example.fazhao.locationmanager.application.BaseApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.baidu.location.h.j.V;
import static com.baidu.navisdk.adapter.PackageUtil.getSdcardDir;


/**
 * Created by Kings on 2016/2/12.
 */
public class HistoryMaps extends Activity {
    private MapView mapView;
    private LatLng latLng1;
    private Marker myLocation;
    private BaiduMap mBaiduMap;
    private TraceDao mTraceDao;
    private List<TraceItem> mDatas, mDatas1, traceItems;
    private CheckBox traffice, satelite, scale, scaleBtn;
    private com.baidu.mapapi.map.PolylineOptions polyline = null;
    protected MapStatusUpdate msUpdate = null;
    private List<LatLng> historyFromLoad = new ArrayList<LatLng>();
    private List<Double> history_latitude = new ArrayList<>();
    private List<Double> history_longitude = new ArrayList<>();
    private Button detail, load;
    private TextView showTime, historyTitle;
    private int tag;
    private HistoryAdapter mAdapter;
    private HistoryDialog historyDialog;
    private Handler handler;
    private ImageButton mNaviButton;

    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
    public static final String RESET_END_NODE = "resetEndNode";
    public static final String VOID_MODE = "voidMode";

    private static final String[] authBaseArr = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION };
    private static final String[] authComArr = { Manifest.permission.READ_PHONE_STATE };
    private static final int authBaseRequestCode = 1;
    private static final int authComRequestCode = 2;

    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    private BNRoutePlanNode.CoordinateType mCoordinateType = null;
    private double mLongitude,mLatitude;
    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "LocationManager";
    private String authinfo = null;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
        setContentView(R.layout.history_maps);
        mLongitude = getIntent().getDoubleExtra("longitude",0);
        mLatitude = getIntent().getDoubleExtra("latitude",0);
        initView();
        int choice = getIntent().getIntExtra("choice", 0);
        try {
            initData(choice);
            operation();
//            saveBitmap(choice+"record");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

    }

    public void saveBitmap(final String name) {
// 截图，在SnapshotReadyCallback中保存图片到 sd 卡
        mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
            public void onSnapshotReady(Bitmap snapshot) {
                File file = new File("/data/data/com.example.fazhao.locationmanager/files/"+name+".png");
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    if (snapshot.compress(
                            Bitmap.CompressFormat.PNG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                    Toast.makeText(HistoryMaps.this,
                            "屏幕截图成功，图片存在: " + file.toString(),
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(HistoryMaps.this, "正在截取屏幕图片...",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (historyDialog != null)
            historyDialog.dismiss();
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        mapView = null;
        /**
         * 在这里关闭db
         * */
        BaseApplication.getDbHelper().close();
        super.onDestroy();
    }

    private void operation() {
        int zoomLevel[] = {2000000,1000000,500000,200000,100000,
                50000,25000,20000,10000,5000,2000,1000,500,100,50,20,0};

        double maxlat,minlat,maxlon,minlon;
        Collections.sort(history_latitude, new Comparator<Double>() {
            @Override
            public int compare(Double aDouble, Double t1) {
                if (aDouble > t1) {
                    return 1;
                } else if (aDouble < t1) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        Collections.sort(history_longitude, new Comparator<Double>() {
            @Override
            public int compare(Double aDouble, Double t1) {
                if (aDouble > t1) {
                    return 1;
                } else if (aDouble < t1) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        maxlat = history_latitude.get(history_latitude.size() - 1);
        minlat = history_latitude.get(0);
        minlon = history_longitude.get(0);
        maxlon = history_longitude.get(history_longitude.size() - 1);
        final double midlat = (maxlat+minlat)/2;
        final double midlon = (maxlon+minlon)/2;
        LatLng latlon = new LatLng(midlat, midlon);
        int jl = (int)DistanceUtil.getDistance(new LatLng(maxlat, maxlon), new LatLng(minlat, minlon));
        int i;
        for(i=0;i<17;i++){
            if(zoomLevel[i]<jl){
                Log.e("final", String.valueOf(i));
                break;
            }
        }
        float zoom = i+6;
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latlon, zoom);
//        MapStatus mMapStatus = new MapStatus.Builder().target(latLng1)
//                .zoom(20)
//                .build();
//
//        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.animateMapStatus(u);
    }

    private void initData(int choice) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (historyFromLoad.size() != 0)
            historyFromLoad.clear();
        mTraceDao = BaseApplication.getmTaceDao();
        //TODO 线程
        traceItems = mTraceDao.searchData(choice);
        latLng1 = new LatLng(traceItems.get(0).getLatitude(), traceItems.get(0).getLongitude());
        history_latitude.clear();
        history_longitude.clear();
        historyFromLoad.clear();
        for (int i = 0; i < traceItems.size(); i++) {
            LatLng latLng = new LatLng(traceItems.get(i).getLatitude(), traceItems.get(i).getLongitude());
            historyFromLoad.add(latLng);
        }
        for (int i = 0; i < historyFromLoad.size(); i++) {
            history_latitude.add(historyFromLoad.get(i).latitude);
        }
        for (int i = 0; i < historyFromLoad.size(); i++) {
            history_longitude.add(historyFromLoad.get(i).longitude);
        }
        drawSolidLine1();
        int tmp = (int) mTraceDao.getDistance(choice);
//        for (int i = 1; i < historyFromLoad.size() -1; i++) {
//            tmp += DistanceUtil.getDistance(historyFromLoad.get(i),historyFromLoad.get(i - 1));
//        }
        ToastUtil.showShortToast(HistoryMaps.this, "距离出发点:" + String.valueOf(tmp));
        showTime.setText("时长：" + mTraceDao.getTime(choice) + "秒"
                + ", 步数:" + traceItems.get(traceItems.size() - 1).getStep()
                + ", 距离:" + tmp + "米");
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean hasBasePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
//                if (0 == status) {
//                    authinfo = "key校验成功!";
//                } else {
//                    authinfo = "key校验失败, " + msg;
//                }
//                HistoryMaps.this.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(HistoryMaps.this, authinfo, Toast.LENGTH_LONG).show();
//                    }
//                });
            }

            public void initSuccess() {
                Toast.makeText(HistoryMaps.this, "正在启动...", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
//                Toast.makeText(HistoryMaps.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(HistoryMaps.this, "启动失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9354030");
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(HistoryMaps.this, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(HistoryMaps.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
            case BD09LL: {
                TraceItem traceItem = traceItems.get(traceItems.size() - 1);
                sNode = new BNRoutePlanNode(mLongitude, mLatitude, "我的位置", null, coType);
                eNode = new BNRoutePlanNode(traceItem.getLongitude(), traceItem.getLatitude(), traceItem.getAddress(), null, coType);
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
        }
    }

    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */

            Intent intent = new Intent(HistoryMaps.this, GuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(HistoryMaps.this, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        showTime = (TextView) findViewById(R.id.show_time);
        mapView = (MapView) findViewById(R.id.historymap);
        mBaiduMap = mapView.getMap();
        detail = (Button) findViewById(R.id.look_detail);
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(HistoryMaps.this, HistoryDetail.class);
                Bundle bundle = new Bundle();
                bundle.putInt("choice", getIntent().getIntExtra("choice", 0));
                it.putExtras(bundle);
                startActivity(it);
            }
        });
        traffice = (CheckBox) findViewById(R.id.cb_traffic);
        scale = (CheckBox) findViewById(R.id.cb_scale);
        satelite = (CheckBox) findViewById(R.id.cb_satelite);
        scaleBtn = (CheckBox) findViewById(R.id.cb_scale_btn);
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
                    mapView.showScaleControl(true);
                } else {
                    mapView.showScaleControl(false);
                }
            }
        });
        scaleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if (b) {
                    mapView.showZoomControls(true);
                } else {
                    mapView.showZoomControls(false);
                }
            }
        });
        mNaviButton = (ImageButton) findViewById(R.id.button_nav);
        mNaviButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (initDirs()) {
                    initNavi();
                }
                if (BaiduNaviManager.isNaviInited()) {
                    routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
                }
            }
        });
        load = (Button) findViewById(R.id.look_history);
        load.setOnClickListener(new View.OnClickListener() {
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
                                historyDialog = new HistoryDialog(HistoryMaps.this);
                                mAdapter = new HistoryAdapter(HistoryMaps.this, mDatas, mDatas1, historyDialog.lv);
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
                                            case 4:
                                                historyDialog.getSpinner().setSelection(pos,false);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        PicAdapter adapter = new PicAdapter(HistoryMaps.this,mTraceDao.maxTag());
                                                        historyDialog.lv.setAdapter(adapter);
                                                    }
                                                });
                                                break;
                                            case 5:
                                                historyDialog.getSpinner().setSelection(pos,false);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDatas.clear();
                                                        mDatas1.clear();
                                                        mDatas.addAll(mTraceDao.searchDistinctDataStartForWalk());
                                                        mDatas1.addAll(mTraceDao.searchDistinctDataDestinationForWalk());
//                                                        mDatas = mTraceDao.searchDistinctDataStartForStep();
//                                                        mDatas1 = mTraceDao.searchDistinctDataDestinationForStep();
                                                        mAdapter.notifyDataSetChanged();
                                                        historyDialog.lv.setAdapter(mAdapter);
                                                    }
                                                });
                                                break;
                                            case 6:
                                                historyDialog.getSpinner().setSelection(pos,false);
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDatas.clear();
                                                        mDatas1.clear();
                                                        mDatas.addAll(mTraceDao.searchDistinctDataStartForBike());
                                                        mDatas1.addAll(mTraceDao.searchDistinctDataDestinationForBike());
//                                                        mDatas = mTraceDao.searchDistinctDataStartForStep();
//                                                        mDatas1 = mTraceDao.searchDistinctDataDestinationForStep();
                                                        mAdapter.notifyDataSetChanged();
                                                        historyDialog.lv.setAdapter(mAdapter);
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
                                historyDialog.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                                        /**
                                         * listview是从0开始，但是我的tag是从1开始，所以position+1
                                         * */

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    TextView tagView = (TextView)view.findViewById(R.id.number_history);
                                                    String s = tagView.getText().toString();
                                                    int index1 = s.lastIndexOf("：");
                                                    s = s.substring(index1 + 1);//截取冒号往后的内容
                                                    int tag = Integer.parseInt(s);
                                                    initData(tag);
                                                    operation();
//                                                    saveBitmap(tag +"record");
                                                    detail.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent it = new Intent(HistoryMaps.this, HistoryDetail.class);
                                                            Bundle bundle = new Bundle();
                                                            bundle.putInt("choice", position + 1);
                                                            it.putExtras(bundle);
                                                            startActivity(it);
                                                        }
                                                    });
                                                } catch (NoSuchPaddingException e) {
                                                    e.printStackTrace();
                                                } catch (InvalidAlgorithmParameterException e) {
                                                    e.printStackTrace();
                                                } catch (NoSuchAlgorithmException e) {
                                                    e.printStackTrace();
                                                } catch (IllegalBlockSizeException e) {
                                                    e.printStackTrace();
                                                } catch (BadPaddingException e) {
                                                    e.printStackTrace();
                                                } catch (InvalidKeyException e) {
                                                    e.printStackTrace();
                                                }
                                                historyDialog.dismiss();
                                            }
                                        });
                                    }
                                });
                                historyDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface anInterface) {
                                        historyDialog.dismiss();
                                        historyDialog = null;
                                    }
                                });
                                historyDialog.setOnDeleteAllListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mTraceDao.deleteAll();
                                        mAdapter.notifyDataSetChanged();
                                        historyDialog.title.setText("历史记录有0数据");
                                    }
                                });
                                historyDialog.setOnNegativeListener(new View.OnClickListener() {
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
                    }
                };
                handler.post(runnable);
                handler.sendEmptyMessage(0);
//
            }
        });
    }

    protected void drawSolidLine1() {
        polyline = new com.baidu.mapapi.map.PolylineOptions().width(10)
                .color(Color.RED).points(historyFromLoad);
        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }
    }

    public void click() {
        if (handler == null)
            handler = new Handler();
        handler.post(updateTitle);
    }

    private Runnable updateTitle = new Runnable() {
        @Override
        public void run() {
            historyTitle = ((TextView) (historyDialog.findViewById(R.id.history_num)));
            historyTitle.setText("历史记录有" + (tag - 1) + "数据");
        }
    };
}
