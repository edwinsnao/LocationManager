package com.example.fazhao.locationmanager.activity;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.application.BaseApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kings on 2016/2/16.
 */
public class HistoryDetail extends Activity {
	ListView lv;
	int tag;
	View mFooterView;
	HistoryAdapter mAdapter;
	private List<TraceItem> mDatas = new ArrayList<TraceItem>();
	TraceDao mTraceDao;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_history);
		initData();
	}

	private void initData() {
		tag = getIntent().getIntExtra("choice", 1);
		mTraceDao = BaseApplication.getmTaceDao();

		mAdapter = new HistoryAdapter(HistoryDetail.this, mDatas);
		mFooterView = LayoutInflater.from(HistoryDetail.this).inflate(R.layout.maps_list_footer, null);
		lv = (ListView) findViewById(R.id.detail_lv);
		lv.setAdapter(mAdapter);
		lv.addFooterView(mFooterView);

		/**
		* wrong
		 * Only the original thread that created a view hierarchy can touch its views.
		* */
//		HandlerThread thread = new HandlerThread("MyThread");
//		thread.start();
//		final Handler handler = new Handler(thread.getLooper()) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case 0:
						mAdapter.notifyDataSetChanged();
						Log.e("eadapter", String.valueOf(mAdapter));
						Log.e("edatas", String.valueOf(mDatas));
						break;
				}
			}
		};
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				/**
				 * 如果这样直接赋值，检测不到mDatas的数据变化
				 * 所以notifydatasetchanged并不会有listview数据的变化刷新
				 * */
				mDatas.addAll(mTraceDao.searchData(tag));
				handler.sendEmptyMessage(0);
			}
		};
		handler.post(runnable);
	}

	private void initView() {
		mFooterView = LayoutInflater.from(HistoryDetail.this).inflate(R.layout.maps_list_footer, null);
		lv = (ListView) findViewById(R.id.detail_lv);
		lv.setAdapter(mAdapter);
		lv.addFooterView(mFooterView);

	}

//	class MyIntentService extends IntentService {
//
//		public MyIntentService(String name) {
//			super(name);
//		}
//
//		@Override
//		protected void onHandleIntent(Intent intent) {
//			mDatas = mTraceDao.searchData(tag);
//			mAdapter.notifyDataSetChanged();
//		}
//	}

}
