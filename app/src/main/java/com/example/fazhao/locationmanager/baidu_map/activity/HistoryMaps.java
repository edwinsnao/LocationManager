package com.example.fazhao.locationmanager.baidu_map.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.baidu_map.util.BaiduUtils;
import com.example.fazhao.locationmanager.baidu_map.util.ToastUtil;
import com.example.fazhao.locationmanager.baidu_map.widget.HistoryDialog;
import com.example.fazhao.locationmanager.application.BaseApplication;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


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
        MapStatus mMapStatus = new MapStatus.Builder().target(latLng1)
                .zoom(20)
                .build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.animateMapStatus(msUpdate);
        showTime.setText("时间相差：" + BaiduUtils.dateDiff(this, traceItems.get(0).getDate(), traceItems.get(traceItems.size() - 1).getDate(), "yyyy-MM-dd-HH:mm:ss", "m")
                + "分钟" + "上次步数:" + mTraceDao.getLastStep().getStep());
    }

    private void initData(int choice) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (historyFromLoad.size() != 0)
            historyFromLoad.clear();
        mTraceDao = BaseApplication.getmTaceDao();
        //TODO 线程
        traceItems = mTraceDao.searchData(choice);
        latLng1 = new LatLng(traceItems.get(0).getLatitude(), traceItems.get(0).getLongitude());
        for (int i = 0; i < traceItems.size(); i++) {
            LatLng latLng = new LatLng(traceItems.get(i).getLatitude(), traceItems.get(i).getLongitude());
            historyFromLoad.add(latLng);
        }
        drawSolidLine1();
        ToastUtil.showShortToast(HistoryMaps.this, "距离出发点:" + String.valueOf(DistanceUtil.getDistance(historyFromLoad.get(0), historyFromLoad.get(historyFromLoad.size() - 1))));
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
                                if (historyDialog == null)
                                    historyDialog = new HistoryDialog(HistoryMaps.this);
                                mAdapter = new HistoryAdapter(HistoryMaps.this, mDatas, mDatas1, historyDialog.lv);
                                historyDialog.lv.setAdapter(mAdapter);
                                historyDialog.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                        /**
                                         * listview是从0开始，但是我的tag是从1开始，所以position+1
                                         * */

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    initData(position + 1);
                                                    operation();
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
