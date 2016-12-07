package com.example.fazhao.locationmanager.activity;

/**
 * Created by Kings on 2016/2/3.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.encrypt.KeyManager;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.param.Address2GeoParam;
import com.tencent.lbssearch.object.result.Address2GeoResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.Circle;
import com.tencent.mapsdk.raster.model.CircleOptions;
import com.tencent.mapsdk.raster.model.GeoPoint;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.mapsdk.raster.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.map.ItemizedOverlay;
import com.tencent.tencentmap.mapsdk.map.MapActivity;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.OverlayItem;
import com.tencent.tencentmap.mapsdk.map.Projection;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import org.apache.http.Header;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TencentMaps extends MapActivity implements
        TencentLocationListener {
    private ImageButton btnShowLocation;
    private SensorManager sensorManager;
    private Sensor oritationSensor;
    private SensorManager mSensorManager;
    private int stepSensor;
    private boolean hasRecord = false;
    private int hasStepCount = 0;
    private int prviousStepCount = 0;
    private int CURRENT_SETP;
    private Sensor mStepSensor;
    private Sensor detectorSensor;
    private Sensor countSensor;
    private CheckBox cbTraffic;
    private CheckBox cbSatellite;
    private CheckBox cbScale;
    private LatLng mZhongGuanCun;
    private TextView tvMonitor;
    private EditText etSteetView;
    private SensorEventListener mSensorEventListener;
    private SensorEventListener mapOrientation;

    private TencentLocationManager locationManager;
    private TencentLocationRequest locationRequest;
    private Marker myLocation;
    private Circle accuracy;
    private MapView mapView;
    private TencentMap tencentMap;
    private ViewGroup custInfowindow;
    private int mStep;
//	private Button btnAnimate;

    private Double param0 = null;
    private Double param1 = null;
    private Button goHere;
    private Button search;
    private Button compute;
    private Button save;
    private Button load;
    private TextView steps;
    private TraceDao mTraceDao;
    private TraceItem mTraceItem;
    List<TencentLocation> history = new ArrayList<>();
    List<LatLng> historyFromLoad = new ArrayList<LatLng>();
    List<String> historyDate = new ArrayList<String>();
    HistoryAdapter mAdapter;
    List<TraceItem> mDatas = new ArrayList<TraceItem>();
    List<TraceItem> mDatas1 = new ArrayList<TraceItem>();
    int tag = 0;
    Bundle bundle = new Bundle();
    View mFooterView;
    private Crypto crypto;
    private KeyManager km;
    private AlertDialog historyDialog;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_show_location);
        /**
         * Release版本要注释掉，只用于调试（内存泄漏）
         * 但是不可以注释
         * 注释了就不可以定位
         * */
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()   // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
////                .detectAll()
//                .penaltyLog()
//                .penaltyDeath()
//                .build());
        initData();
        init();
        bindListener();
    }

    private void initData() {
        final String key = "12345678909876543212345678909876";
        final String iv = "1234567890987654";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                km = BaseApplication.getKm();
                /**
                 * 耗时
                 * */
                km.setIv(iv.getBytes());
                km.setId(key.getBytes());
            }
        });
        thread.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(historyDialog!=null)
            historyDialog.dismiss();
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(mapOrientation);
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    protected void init() {
        crypto = BaseApplication.getmCrypto();
        mFooterView = LayoutInflater.from(TencentMaps.this).inflate(R.layout.maps_list_footer, null);
        final Runnable saveHistory = new Runnable() {
            @Override
            public void run() {
                if (mTraceDao.searchAllData() != null)
                    tag = mTraceDao.maxTag() + 1;
                for (int i = 0; i < history.size(); i++) {
                    mTraceItem = new TraceItem();
                    try {
                        mTraceItem.setName(crypto.armorEncrypt(history.get(i).getName().getBytes()));
                        mTraceItem.setAddress(crypto.armorEncrypt(history.get(i).getAddress().getBytes()));
                        mTraceItem.setLatitude(history.get(i).getLatitude());
                        mTraceItem.setLongitude(history.get(i).getLongitude());
                        mTraceItem.setTag(tag);
                        /**
                         * 这里是导致一个tag的所有记录的时间都是相同的，所以时间差为0或很接近
                         * 应该把这里放到onlocationchanged里（history里setDate）
                         * 而这里就拿history的date
                         * */
                        mTraceItem.setDate(crypto.armorEncrypt(historyDate.get(i).getBytes()));
                        /**
                        * 在最后一个插入步数
                         * 如果是0也插入，证明不是走路（是交通工具）
                        * */
//                        if(i == history.size() - 1){
                            mTraceItem.setStep(mStep);
//                        }
                        mTraceDao.add(mTraceItem);
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
            }
        };
        mTraceDao = BaseApplication.getmTaceDao();
        steps = (TextView) findViewById(R.id.steps);
        if(mTraceDao.maxTag() !=0){
            steps.setText("上次步数:"+mTraceDao.getLastStep().getStep());
        }
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        oritationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
		mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        btnShowLocation = (ImageButton) findViewById(R.id.btn_show_location);
        locationManager = TencentLocationManager.getInstance(TencentMaps.this);
        locationRequest = TencentLocationRequest.create()
                .setInterval(1000)
                /**
                 * 连续多次定位建议开启
                 * */
                .setAllowCache(true)
                /**
                 * 省电，但是没有name和address，我调试了一晚，以为是加密出错，原来是这里错了
                 * */
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        mapView = (MapView) findViewById(R.id.tencentMapView);
        tencentMap = mapView.getMap();
        getLastKnownLocation();
        cbTraffic = (CheckBox) findViewById(R.id.cb_traffic);
        cbSatellite = (CheckBox) findViewById(R.id.cb_satelite);
        cbScale = (CheckBox) findViewById(R.id.cb_scale);
//		btnAnimate = (Button) findViewById(R.id.btn_animate);
        tvMonitor = (TextView) findViewById(R.id.tvMonitor);
        etSteetView = (EditText) findViewById(R.id.et_streetView);
        save = (Button) findViewById(R.id.btn_save);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TencentMaps.this, "saving.....", Toast.LENGTH_SHORT).show();
                save.setEnabled(false);
                Thread saveThread = new Thread(saveHistory);
                saveThread.start();
                save.setEnabled(true);
                Toast.makeText(TencentMaps.this, "saved!", Toast.LENGTH_SHORT).show();
            }
        });
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
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                final View v1 = inflater.inflate(R.layout.loading_dialog, null);
                                final SwipeDeleteListView lv = (SwipeDeleteListView) v1.findViewById(R.id.list_history);
                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        /**
                                         * listview是从0开始，但是我的tag是从1开始，所以position+1
                                         * */
                                        bundle.putInt("choice", position + 1);
                                        Intent it = new Intent();
                                        it.setClass(TencentMaps.this, HistoryMaps.class);
                                        it.putExtras(bundle);
                                        startActivity(it);
                                        finish();
                                    }
                                });
                                mAdapter = new HistoryAdapter(TencentMaps.this, mDatas, mDatas1, lv);
                                lv.setDivider(getResources().getDrawable(R.drawable.divider));
                                lv.setAdapter(mAdapter);
                                lv.setFooterDividersEnabled(true);
                                lv.setHeaderDividersEnabled(true);
                                lv.addFooterView(mFooterView);
                                historyDialog = new AlertDialog.Builder(TencentMaps.this, AlertDialog.THEME_HOLO_LIGHT)
                                        .setTitle("历史记录有" + tag + "数据").setView(v1)//在这里把写好的这个listview的布局加载dialog中
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                                int choice = 0;
                                                EditText et_choice = (EditText) v1.findViewById(R.id.et_searchData);
                                                if (TextUtils.isEmpty(et_choice.getText())) {
//                                            加入不输入任何信息，则默认取最新的数据
                                                    choice = tag;
                                                } else
                                                    choice = Integer.valueOf(et_choice.getText().toString());
                                                bundle.putInt("choice", choice);
                                                Intent it = new Intent();
                                                it.setClass(TencentMaps.this, HistoryMaps.class);
                                                it.putExtras(bundle);
                                                startActivity(it);
                                                dialog.cancel();
                                                TencentMaps.this.finish();
                                            }
                                        }).create();
                                historyDialog.setButton3("清空历史记录", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mTraceDao.deleteAll();
                                        mAdapter.notifyDataSetChanged();
                                        dialog.cancel();
                                    }
                                });
                                historyDialog.setButton2("清除指定记录", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                        final View v = inflater.inflate(R.layout.delete_history_map, null);
                                        new AlertDialog.Builder(TencentMaps.this, AlertDialog.THEME_HOLO_LIGHT)
                                                .setMessage("删除指定记录")
                                                .setView(v)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        /**
                                                         * 默认删除第一次记录(最远的记录)
                                                         * */
                                                        int choice1 = 1;
                                                        EditText et_choice = (EditText) v.findViewById(R.id.et_DelteData);
                                                        if (TextUtils.isEmpty(et_choice.getText())) {
                                                            choice1 = tag;
                                                        } else
                                                            choice1 = Integer.valueOf(et_choice.getText().toString());
                                                        final int finalChoice = choice1;
                                                        new AlertDialog.Builder(TencentMaps.this, AlertDialog.THEME_HOLO_LIGHT)
                                                                .setMessage("删除记录后不能恢复记录，是否继续？")
                                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        mTraceDao.deleteAll(finalChoice);
                                                                        /**
                                                                         * mDatas是从mTraceDao.searchData得到的
                                                                         * 因为上面一句 已经删除了
                                                                         * 所以拿到的已经没有finalchoice了所以报错
                                                                         * */
                                                                        mAdapter.notifyDataSetChanged();
                                                                        dialog.cancel();
                                                                    }
                                                                })
                                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.cancel();
                                                                    }
                                                                })
                                                                .create().show();
                                                    }
                                                })
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                })
                                                .create().show();
                                    }
                                });
                                historyDialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
                                /**
                                * 显示历史的dialog泄露了内存，因为我显示历史后会点击listview的item
                                 * 然后就会finish这个activity而进入historyActivity
                                 * 但是这个dialog在activity退出之前没有进行dismiss所以泄漏了
                                * */
                                historyDialog.show();
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
            public void onClick(View v) {
                int temp = 0;
                if (history.size() != 0) {
                    int size = history.size();
                    for (int i = 0; i < size - 1; i++)
                    /**
                     * 官方的计算距离
                     * */
                        temp += TencentLocationUtils.distanceBetween(history.get(i), history.get(i + 1));
                    ToastUtil.showShortToast(TencentMaps.this, "距离出发点:" + String.valueOf(temp));
                }
            }
        });
        search = (Button) findViewById(R.id.btn_search);
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                geocoder();
            }
        });
        goHere = (Button) findViewById(R.id.btn_streetView);
        goHere.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putDouble("param0", param0);
                bundle.putDouble("param1", param1);
                Intent it = new Intent();
                it.setClass(TencentMaps.this, StreetViewActivity.class);
                it.putExtras(bundle);
                startActivity(it);
            }
        });
        mZhongGuanCun = new LatLng(39.980484, 116.311302);//中关村

//		btnAnimate.setOnClickListener(new OnClickListener() {
//
//			CancelableCallback callback = new CancelableCallback() {
//
//				@Override
//				public void onFinish() {
//					// TODO Auto-generated method stub
//				}
//
//				@Override
//				public void onCancel() {
//					// TODO Auto-generated method stub
//				}
//			};
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				tencentMap.animateTo(mZhongGuanCun/*, 4000, callback*/);
//			}
//		});
        tencentMap.setOnMapCameraChangeListener(new TencentMap.OnMapCameraChangeListener() {

            @Override
            public void onCameraChangeFinish(CameraPosition arg0) {
                // TODO Auto-generated method stub
                param0 = arg0.getTarget().getLatitude();
                param1 = arg0.getTarget().getLongitude();
            }

            @Override
            public void onCameraChange(CameraPosition arg0) {
                // TODO Auto-generated method stub
            }
        });
        tencentMap.setOnMapClickListener(new TencentMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
            }
        });
        tencentMap.setOnMapLongClickListener(new TencentMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub
            }
        });

        tencentMap.setOnMarkerDraggedListener(new TencentMap.OnMarkerDraggedListener() {

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                tvMonitor.setText("Marker Dragging");
            }

            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                tvMonitor.setText("Marker Drag End");
            }

            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                tvMonitor.setText("Marker Drag Start");
            }
        });
        Drawable drawable = getResources().getDrawable(R.drawable.red_location);
        final int y = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0,
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        TestOverlay testOverlay = new TestOverlay(drawable, this);

        custInfowindow = setCustInfowindow();
        testOverlay.setOnTapListener(new OnTapListener() {

            TextView title = (TextView) custInfowindow.findViewById(Integer.valueOf(1));
            TextView snippet = (TextView) custInfowindow.findViewById(Integer.valueOf(2));

            @Override
            public void onTap(OverlayItem itemTap) {
                // TODO Auto-generated method stub
                if (custInfowindow == null || itemTap == null) {
                    return;
                }
                MapView.LayoutParams lp = new MapView.LayoutParams(
                        MapView.LayoutParams.WRAP_CONTENT,
                        MapView.LayoutParams.WRAP_CONTENT,
                        itemTap.getPoint(),
                        0, -y, MapView.LayoutParams.BOTTOM_CENTER);
                title.setText(itemTap.getTitle());
                snippet.setText(itemTap.getSnippet());
                if (mapView.indexOfChild(custInfowindow) == -1) {
                    mapView.addView(custInfowindow, lp);
                } else {
                    mapView.updateViewLayout(custInfowindow, lp);
                }
            }

            @Override
            public void onEmptyTap(GeoPoint pt) {
                // TODO Auto-generated method stub
                if (mapView.indexOfChild(custInfowindow) >= 0) {
                    mapView.removeView(custInfowindow);
                }
            }
        });
        mapView.addOverlay(testOverlay);
        //实时交通图开关
        cbTraffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                tencentMap.setTrafficEnabled(isChecked);
            }
        });

        //卫星图开关
        cbSatellite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                tencentMap.setSatelliteEnabled(isChecked);
            }
        });

        //内置比例尺开关
        cbScale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                mapView.getUiSettings().setScaleControlsEnabled(isChecked);
            }
        });
    }

    /**
     * 计算两点之间距离
     *
     * @param start
     * @param end
     * @return 米
     */
    public double getDistance(LatLng start, LatLng end) {
        double lat1 = (Math.PI / 180) * start.getLatitude();
        double lat2 = (Math.PI / 180) * end.getLatitude();

        double lon1 = (Math.PI / 180) * start.getLongitude();
        double lon2 = (Math.PI / 180) * end.getLongitude();

        //地球半径
        double R = 6371;

        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;

        return d * 1000;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
//        if (countSensor != null) {
//            stepSensor = 0;
//            Log.v("xf", "countSensor");
//            sensorManager.registerListener(TencentMaps.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        } else if (detectorSensor != null) {
//            stepSensor = 1;
//            Log.v("xf", "detectorSensor");
//            sensorManager.registerListener(TencentMaps.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        } else {
//            Log.v("xf", "Count sensor not available!");
////            addBasePedoListener();
//        }
        /**
        * step
        * */
        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[0] == 1.0f) {
                    mStep++;
                }
                StringBuilder builder = new StringBuilder("步数:");
                builder.append(Integer.toString(mStep));
                steps.setText(builder);
            }
        };
        /**
        * TencentMap
        * */
        mapOrientation = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (myLocation != null) {
                    myLocation.setRotation(event.values[0]);
                }
            }
        };
    }


    protected void bindListener() {
        btnShowLocation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                放到oncreate中，一开app就开始定位
                int error = locationManager.requestLocationUpdates(
                        locationRequest, TencentMaps.this);
                switch (error) {
//                    case 0:
//                        LogWrap.e("成功注册监听器");
//                        break;
//                    case 1:
//                        LogWrap.e("设备缺少使用腾讯定位服务需要的基本条件");
//                        break;
//                    case 2:
//                        LogWrap.e("manifest 中配置的 key 不正确");
//                        break;
//                    case 3:
//                        LogWrap.e("自动加载libtencentloc.so失败");
//                        break;
//
//                    default:
//                        break;
                }
                sensorManager.registerListener(mapOrientation,
                        oritationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });
        /**
         * 如果设置SENSOR_DELAY_FASTEST会浪费电的
         * */
        mSensorManager.registerListener(mSensorEventListener, mStepSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onLocationChanged(TencentLocation arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub
        if (arg1 == TencentLocation.ERROR_OK) {
            LatLng latLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
            if (myLocation == null) {
                myLocation = tencentMap.addMarker(new MarkerOptions().
                        position(latLng).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)).
                        anchor(0.5f, 0.5f));
            }
            if (accuracy == null) {
                accuracy = tencentMap.addCircle(new CircleOptions().
                        center(latLng).
                        radius((double) arg0.getAccuracy()).
                        fillColor(0x440000ff).
                        strokeWidth(0f));
            }
            myLocation.setPosition(latLng);
            accuracy.setCenter(latLng);
            accuracy.setRadius(arg0.getAccuracy());
            tencentMap.animateTo(latLng);
            tencentMap.setZoom(19);
            history.add(arg0);
            Calendar now = Calendar.getInstance();
            historyDate.add(now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH)
                    + "-" + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND));
            if (history.size() != 0) {
                drawSolidLine(history);
            }
//            if (historyFromLoad.size() != 0)
//                drawSolidLine1(historyFromLoad);
        }
    }

    @Override
    public void onStatusUpdate(String arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub

    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        // TODO Auto-generated method stub
//        if (myLocation != null) {
//            myLocation.setRotation(event.values[0]);
//        }
////        if (stepSensor == 0) {
////            int tempStep = (int) event.values[0];
////            if (!hasRecord) {
////                hasRecord = true;
////                hasStepCount = tempStep;
////            } else {
////                int thisStepCount = tempStep - hasStepCount;
////                CURRENT_SETP += (thisStepCount - prviousStepCount);
////                prviousStepCount = thisStepCount;
//////                StepDcretor.CURRENT_SETP++;
////
////            }
//////			Logger.d("tempStep" + tempStep);
////        } else if (stepSensor == 1) {
////            if (event.values[0] == 1.0) {
////                CURRENT_SETP++;
////            }
////        }
////        StringBuilder builder = new StringBuilder("步数:");
////        builder.append(Integer.toString(CURRENT_SETP));
////        steps.setText(builder);
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // TODO Auto-generated method stub
//
//    }

    protected ViewGroup setCustInfowindow() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout root = new LinearLayout(this);
        root.setBackgroundDrawable(getResources().getDrawable(R.drawable.infowindow_background));
        root.setLayoutParams(lp);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        TextView tvTitle = new TextView(this);
        tvTitle.setId(Integer.valueOf(1));
        tvTitle.setLayoutParams(lp);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(0xff000000);
        TextView tvSnipeet = new TextView(this);
        tvSnipeet.setLayoutParams(lp);
        tvSnipeet.setId(Integer.valueOf(2));
        tvSnipeet.setGravity(Gravity.CENTER);
        tvSnipeet.setTextColor(0xff000000);
        root.addView(tvTitle);
        root.addView(tvSnipeet);
        return root;
    }


    interface OnTapListener {
        public void onTap(OverlayItem itemTap);

        public void onEmptyTap(GeoPoint pt);
    }

    /**
     * 将路线以实线画到地图上
     *
     * @param locations
     */
    protected void drawSolidLine(List<TencentLocation> locations) {
        /**
        * 防止越画越卡？
        * */
        tencentMap.clearAllOverlays();
        tencentMap.addPolyline(new PolylineOptions().
                addAll(getLatLngs(locations)).
                color(0xff2200ff));
    }

    protected void getLastKnownLocation() {
        if (mTraceDao.maxTag() != 0) {
            TraceItem traceItem = mTraceDao.getLast();
            LatLng latLng = new LatLng(traceItem.getLatitude(), traceItem.getLongitude());
            myLocation = tencentMap.addMarker(new MarkerOptions().
                    position(latLng).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)).
                    anchor(0.5f, 0.5f));
            tencentMap.animateTo(latLng);
            tencentMap.setZoom(20);
        }
    }

    /**
     * 将路线以实线画到地图上
     *
     * @param latLngs
     */
    protected void drawSolidLine1(List<LatLng> latLngs) {
        tencentMap.addPolyline(new PolylineOptions().
                addAll(latLngs).
                color(0xff2200ff));
    }

    protected List<LatLng> getLatLngs(List<TencentLocation> locations) {
        List<LatLng> latLngs = new ArrayList<LatLng>();
        for (TencentLocation location : locations) {
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        return latLngs;
    }

    protected LatLng getLatLng(TencentLocation locations) {
        LatLng latLng1 = new LatLng(locations.getLatitude(), locations.getLongitude());
        return latLng1;
    }

    protected void geocoder() {
        TencentSearch tencentSearch = new TencentSearch(TencentMaps.this);
        String address = etSteetView.getText().toString();
        Address2GeoParam address2GeoParam =
                new Address2GeoParam().address(address).region("北京");
        tencentSearch.address2geo(address2GeoParam, new HttpResponseListener() {

            @Override
            public void onSuccess(int arg0, Header[] arg1, BaseObject arg2) {
                // TODO Auto-generated method stub
                if (arg2 == null) {
                    return;
                }
                Address2GeoResultObject obj = (Address2GeoResultObject) arg2;
                LatLng latLng1 = new LatLng(obj.result.location.lat, obj.result.location.lng);
                tencentMap.animateTo(latLng1);
                tencentMap.setZoom(20);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
                // TODO Auto-generated method stub
                ToastUtil.showShortToast(TencentMaps.this, "error ");
            }
        });
    }

    class TestOverlay extends ItemizedOverlay<OverlayItem> {

        private List<OverlayItem> overlayItems;
        private OnTapListener onTapListener;

        public TestOverlay(Drawable drawable, Context context) {
            // TODO Auto-generated constructor stub
            super(boundCenterBottom(drawable));
            overlayItems = new ArrayList<OverlayItem>();
            GeoPoint gp1 = new GeoPoint(39911766, 116305456);
            GeoPoint gp2 = new GeoPoint(40118165, 116170304);
            GeoPoint gp3 = new GeoPoint(39794996, 116546586);

            overlayItems.add(new OverlayItem(gp1, "39.911766, 116.305456", "snippet"));
            overlayItems.add(new OverlayItem(gp2, "40.118165, 116.170304", "snippet"));
            OverlayItem item = new OverlayItem(gp3, "39.794996, 116.546586", "可拖动");
            item.setDragable(true);
            overlayItems.add(item);
            populate();
        }

        @Override
        protected OverlayItem createItem(int arg0) {
            // TODO Auto-generated method stub
            return overlayItems.get(arg0);
        }

        @Override
        public int size() {
            // TODO Auto-generated method stub
            return overlayItems.size();
        }

        @Override
        public void draw(Canvas arg0, MapView arg1) {
            // TODO Auto-generated method stub
            super.draw(arg0, arg1);
            Projection projection = arg1.getProjection();
            Paint paint = new Paint();
            paint.setColor(0xff000000);
            paint.setTextSize(15);
            float width;
            float textHeight = paint.measureText("Yy");
            for (int i = 0; i < overlayItems.size(); i++) {
                Point point = new Point();
                projection.toPixels(overlayItems.get(i).getPoint(), point);
                width = paint.measureText(Integer.toString(i));
                arg0.drawText(Integer.toString(i),
                        point.x - width / 2, point.y + textHeight, paint);
            }

        }

        @Override
        protected boolean onTap(int arg0) {
            // TODO Auto-generated method stub
            OverlayItem overlayItem = overlayItems.get(arg0);
            setFocus(overlayItem);
            if (onTapListener != null) {
                onTapListener.onTap(overlayItem);
            }
            return true;
        }

        @Override
        public void onEmptyTap(GeoPoint arg0) {
            // TODO Auto-generated method stub
            if (onTapListener != null) {
                onTapListener.onEmptyTap(arg0);
            }
        }

        public void setOnTapListener(OnTapListener onTapListener) {
            this.onTapListener = onTapListener;
        }
    }

}
