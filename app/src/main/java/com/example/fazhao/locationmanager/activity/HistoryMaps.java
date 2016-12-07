package com.example.fazhao.locationmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.mapsdk.raster.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.map.MapActivity;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Kings on 2016/2/12.
 */
public class HistoryMaps extends MapActivity {
	MapView mapView;
	LatLng latLng1;
	Marker myLocation;
	TencentMap tencentMap;
	private TraceDao mTraceDao;
	List<LatLng> historyFromLoad = new ArrayList<LatLng>();
	Button detail;
	TextView showTime;
	Crypto crypto = BaseApplication.getmCrypto();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.history_maps);
		initView();
		try {
			initData();
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

	private void operation() {
		myLocation = tencentMap.addMarker(new MarkerOptions().
				position(latLng1).
				icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)).
				anchor(0.5f, 0.5f));
		tencentMap.animateTo(latLng1);
		tencentMap.setZoom(15);
	}

	private void initData() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		if (historyFromLoad.size() != 0)
			historyFromLoad.clear();
		mTraceDao = BaseApplication.getmTaceDao();
		int choice = getIntent().getIntExtra("choice", 0);
		//TODO 线程
		List<TraceItem> traceItems = mTraceDao.searchData(choice);
		latLng1 = new LatLng(traceItems.get(0).getLatitude(), traceItems.get(0).getLongitude());
		for (int i = 0; i < traceItems.size(); i++) {
			LatLng latLng = new LatLng(traceItems.get(i).getLatitude(), traceItems.get(i).getLongitude());
			historyFromLoad.add(latLng);
			drawSolidLine1(historyFromLoad);
		}
		computeDistance();
		showTime.setText("时间相差：" + dateDiff(crypto.armorDecrypt(traceItems.get(0).getDate()), crypto.armorDecrypt(traceItems.get(traceItems.size() - 1).getDate()), "yyyy-MM-dd-HH:mm:ss", "m")
				+ "分钟"+"上次步数:"+mTraceDao.getLastStep().getStep());
	}

	private void initView() {
		showTime = (TextView) findViewById(R.id.show_time);
		mapView = (MapView) findViewById(R.id.tencentMapView);
		tencentMap = mapView.getMap();
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

	}

	protected void drawSolidLine1(List<LatLng> latLngs) {
		tencentMap.addPolyline(new PolylineOptions().
				addAll(latLngs).
				color(0xff2200ff));
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

	private void computeDistance() {
		int temp1 = 0;
		if (historyFromLoad.size() != 0) {
			int size = historyFromLoad.size();
			for (int i = 0; i < size - 1; i++)
				temp1 += getDistance(historyFromLoad.get(i), historyFromLoad.get(i + 1));
			ToastUtil.showShortToast(HistoryMaps.this, "距离出发点:" + String.valueOf(temp1));
		}
	}

	public Long dateDiff(String startTime, String endTime, String format, String str) {
		// 按照传入的格式生成一个simpledateformate对象
		SimpleDateFormat sd = new SimpleDateFormat(format);
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		long nm = 1000 * 60;// 一分钟的毫秒数
		long ns = 1000;// 一秒钟的毫秒数
		long diff;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		// 获得两个时间的毫秒时间差异
		try {
			diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
			day = diff / nd;// 计算差多少天
			hour = diff % nd / nh + day * 24;// 计算差多少小时
			min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
			sec = diff % nd % nh % nm / ns;// 计算差多少秒
			// 输出结果
			ToastUtil.showShortToast(HistoryMaps.this, "时间相差：" + day + "天" + (hour - day * 24) + "小时"
					+ (min - day * 24 * 60) + "分钟" + sec + "秒。");
			if (str.equalsIgnoreCase("h")) {
				return hour;
			} else {
				return min;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (str.equalsIgnoreCase("h")) {
			return hour;
		} else {
			return min;
		}
	}
}
