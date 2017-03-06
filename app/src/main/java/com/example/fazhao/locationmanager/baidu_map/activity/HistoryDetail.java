package com.example.fazhao.locationmanager.baidu_map.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.adapter.HistoryAdapter;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.baidu_map.widget.SlideLayout;

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
	SlideLayout slide;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_history);
		initUI();
		initData();
	}

	private void initUI() {
		slide = (SlideLayout) findViewById(R.id.slide);
		slide.setOnSildingFinishListener(new SlideLayout.OnSildingFinishListener() {
			@Override
			public void onSildingFinish() {
				finish();
			}
		});
		slide.setTouchView(slide);
	}

	private void initData() {
		tag = getIntent().getIntExtra("choice", 1);
		mTraceDao = BaseApplication.getmTaceDao();

		lv = (ListView) findViewById(R.id.detail_lv);
		mAdapter = new HistoryAdapter(HistoryDetail.this, mDatas,lv);
		mFooterView = LayoutInflater.from(HistoryDetail.this).inflate(R.layout.maps_list_footer, null);
		lv.setAdapter(mAdapter);
		lv.addFooterView(mFooterView);

		/**
		* wrong
		 * Only the original thread that created a view hierarchy can touch its views.
		* */
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case 0:
						mAdapter.notifyDataSetChanged();
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
}
