package com.example.fazhao.locationmanager.activity;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.baidu_map.activity.IndoorLocationActivity;
import com.example.fazhao.locationmanager.baidu_map.util.BaiduUtils;
import com.example.fazhao.locationmanager.baidu_map.widget.HistoryDialog;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.tencent.mapsdk.raster.model.Marker;
//import com.tencent.tencentmap.mapsdk.map.MapActivity;
//import com.tencent.tencentmap.mapsdk.map.MapView;
//import com.tencent.tencentmap.mapsdk.map.TencentMap;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.R.attr.tag;

/**
 * Created by Kings on 2016/2/12.
 */
public class HistoryMaps extends Activity{
	private MapView mapView;
	private LatLng latLng1;
	private Marker myLocation;
	private BaiduMap mBaiduMap;
	private TraceDao mTraceDao;
	private List<TraceItem> mDatas,mDatas1,traceItems;
	private CheckBox traffice,satelite,scale,scaleBtn;
	private com.baidu.mapapi.map.PolylineOptions polyline = null;
	protected MapStatusUpdate msUpdate = null;
	private List<LatLng> historyFromLoad = new ArrayList<LatLng>();
	private Button detail,load;
	private TextView showTime,historyTitle;
	private int tag;
//	private Crypto crypto = Crypto.getsInstance();
	private Crypto crypto = BaseApplication.getmCrypto();
	private HistoryAdapter mAdapter;
	private HistoryDialog historyDialog;
//	private Handler mHandler;
	private Handler handler;
//	private Crypto crypto = new Crypto(this);

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		this.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
		setContentView(R.layout.history_maps);
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
		if(historyDialog!=null)
			historyDialog.dismiss();
		mapView.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mBaiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		if(handler!=null)
			handler.removeCallbacksAndMessages(null);
		mapView = null;
		/**
		 * 在这里关闭db
		 * */
		BaseApplication.getDbHelper().close();
		super.onDestroy();
	}

	private void operation() {
//		myLocation = tencentMap.addMarker(new MarkerOptions().
//				position(latLng1).
//				icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)).
//				anchor(0.5f, 0.5f));
//		tencentMap.animateTo(latLng1);
//		tencentMap.setZoom(15);
		MapStatus mMapStatus= new MapStatus.Builder().target(latLng1)
				.zoom(20)
				.build();

		msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		mBaiduMap.animateMapStatus(msUpdate);
		showTime.setText("时间相差：" + BaiduUtils.dateDiff(this,traceItems.get(0).getDate(), traceItems.get(traceItems.size() - 1).getDate(), "yyyy-MM-dd-HH:mm:ss", "m")
				+ "分钟"+"上次步数:"+mTraceDao.getLastStep().getStep());
	}

	private void initData(int choice) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		if (historyFromLoad.size() != 0)
			historyFromLoad.clear();
		mTraceDao = BaseApplication.getmTaceDao();
		//TODO 线程
		traceItems = mTraceDao.searchData(choice);
		Log.e("choice", String.valueOf(choice));
		Log.e("choice1", String.valueOf(traceItems.size()));
		latLng1 = new LatLng(traceItems.get(0).getLatitude(), traceItems.get(0).getLongitude());
		for (int i = 0; i < traceItems.size(); i++) {
			LatLng latLng = new LatLng(traceItems.get(i).getLatitude(), traceItems.get(i).getLongitude());
			historyFromLoad.add(latLng);
		}
//		drawSolidLine1(historyFromLoad);
		drawSolidLine1();
//		computeDistance();
		ToastUtil.showShortToast(HistoryMaps.this, "距离出发点:" + String.valueOf(DistanceUtil.getDistance(historyFromLoad.get(0),historyFromLoad.get(historyFromLoad.size()-1))));
//		showTime.setText("时间相差：" + BaiduUtils.dateDiff(this,traceItems.get(0).getDate(), traceItems.get(traceItems.size() - 1).getDate(), "yyyy-MM-dd-HH:mm:ss", "m")
//				+ "分钟"+"上次步数:"+mTraceDao.getLastStep().getStep());
//		showTime.setText("时间相差：" + BaiduUtils.dateDiff(this,crypto.armorDecrypt(traceItems.get(0).getDate()), crypto.armorDecrypt(traceItems.get(traceItems.size() - 1).getDate()), "yyyy-MM-dd-HH:mm:ss", "m")
//				+ "分钟"+"上次步数:"+mTraceDao.getLastStep().getStep());
	}


	private void initView() {
		showTime = (TextView) findViewById(R.id.show_time);
		mapView = (MapView) findViewById(R.id.historymap);
//		mapView = (MapView) findViewById(R.id.tencentMapView);
//		tencentMap = mapView.getMap();
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
					mapView.showScaleControl(true);
				}else{
					mapView.showScaleControl(false);
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
					mapView.showZoomControls(true);
				}else{
					mapView.showZoomControls(false);
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
//								if(historyDialog == null)
									historyDialog = new HistoryDialog(HistoryMaps.this);
								mAdapter = new HistoryAdapter(HistoryMaps.this, mDatas, mDatas1, historyDialog.lv);
								historyDialog.lv.setAdapter(mAdapter);
								historyDialog.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
										/**
										 * listview是从0开始，但是我的tag是从1开始，所以position+1
										 * */

//											if(mHandler == null)
//												mHandler = new Handler();
//											Runnable runnable = new Runnable() {
//												@Override
//												public void run() {
//													try{
//														initData(position+1);
//														operation();
//														detail.setOnClickListener(new View.OnClickListener() {
//															@Override
//															public void onClick(View v) {
//																Intent it = new Intent(HistoryMaps.this, HistoryDetail.class);
//																Bundle bundle = new Bundle();
//																bundle.putInt("choice", position+1);
//																it.putExtras(bundle);
//																startActivity(it);
//															}
//														});
//													} catch (NoSuchPaddingException e) {
//														e.printStackTrace();
//													} catch (InvalidAlgorithmParameterException e) {
//														e.printStackTrace();
//													} catch (NoSuchAlgorithmException e) {
//														e.printStackTrace();
//													} catch (IllegalBlockSizeException e) {
//														e.printStackTrace();
//													} catch (BadPaddingException e) {
//														e.printStackTrace();
//													} catch (InvalidKeyException e) {
//														e.printStackTrace();
//													}
//												}
//											};
//										mHandler.post(runnable);
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												try{
													initData(position+1);
													operation();
													detail.setOnClickListener(new View.OnClickListener() {
														@Override
														public void onClick(View v) {
															Intent it = new Intent(HistoryMaps.this, HistoryDetail.class);
															Bundle bundle = new Bundle();
															bundle.putInt("choice", position+1);
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
		if(handler == null)
			handler = new Handler();
		handler.post(updateTitle);
	}

	private Runnable updateTitle = new Runnable() {
		@Override
		public void run() {
			historyTitle = ((TextView)(historyDialog.findViewById(R.id.history_num)));
			historyTitle.setText("历史记录有" + (tag-1) + "数据");
		}
	};
//	protected void drawSolidLine1(List<LatLng> latLngs) {
//		tencentMap.addPolyline(new PolylineOptions().
//				addAll(latLngs).
//				color(0xff2200ff));
//	}

//	/**
//	 * 计算两点之间距离
//	 *
//	 * @param start
//	 * @param end
//	 * @return 米
//	 */
//	public double getDistance(LatLng start, LatLng end) {
//		double lat1 = (Math.PI / 180) * start.getLatitude();
//		double lat2 = (Math.PI / 180) * end.getLatitude();
//
//		double lon1 = (Math.PI / 180) * start.getLongitude();
//		double lon2 = (Math.PI / 180) * end.getLongitude();
//
//		//地球半径
//		double R = 6371;
//
//		//两点间距离 km，如果想要米的话，结果*1000就可以了
//		double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
//
//		return d * 1000;
//	}
//
//	private void computeDistance() {
//		int temp1 = 0;
//		if (historyFromLoad.size() != 0) {
//			int size = historyFromLoad.size();
//			for (int i = 0; i < size - 1; i++)
//				temp1 += getDistance(historyFromLoad.get(i), historyFromLoad.get(i + 1));
//			ToastUtil.showShortToast(HistoryMaps.this, "距离出发点:" + String.valueOf(temp1));
//		}
//	}

//	public Long dateDiff(String startTime, String endTime, String format, String str) {
//		// 按照传入的格式生成一个simpledateformate对象
//		SimpleDateFormat sd = new SimpleDateFormat(format);
//		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
//		long nh = 1000 * 60 * 60;// 一小时的毫秒数
//		long nm = 1000 * 60;// 一分钟的毫秒数
//		long ns = 1000;// 一秒钟的毫秒数
//		long diff;
//		long day = 0;
//		long hour = 0;
//		long min = 0;
//		long sec = 0;
//		// 获得两个时间的毫秒时间差异
//		try {
//			diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
//			day = diff / nd;// 计算差多少天
//			hour = diff % nd / nh + day * 24;// 计算差多少小时
//			min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
//			sec = diff % nd % nh % nm / ns;// 计算差多少秒
//			// 输出结果
//			ToastUtil.showShortToast(HistoryMaps.this, "时间相差：" + day + "天" + (hour - day * 24) + "小时"
//					+ (min - day * 24 * 60) + "分钟" + sec + "秒。");
//			if (str.equalsIgnoreCase("h")) {
//				return hour;
//			} else {
//				return min;
//			}
//
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (str.equalsIgnoreCase("h")) {
//			return hour;
//		} else {
//			return min;
//		}
//	}
}
