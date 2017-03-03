package com.example.fazhao.locationmanager.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.activity.HistoryMaps;
import com.example.fazhao.locationmanager.baidu_map.widget.ScrollListView;
import com.example.fazhao.locationmanager.baidu_map.widget.SwipeDeleteListView1;
import com.example.fazhao.locationmanager.activity.TraceDao;
import com.example.fazhao.locationmanager.activity.TraceItem;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.activity.IndoorLocationActivity;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;

import java.util.List;

/**
 * Created by Kings on 2016/2/13.
 */
public class HistoryAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<TraceItem> mDatas;
	private List<TraceItem> mDatas1;
//	private SwipeDeleteListView1 listView;
	private ListView listView;
	private Crypto crypto;
	private KeyManager km;
	private TraceDao mTraceDao = BaseApplication.getmTaceDao();
	private Context mContext;
//	private Crypto crypto = Crypto.getsInstance();
	private deleteClick mDeleteClick;


	public void setClickListener(deleteClick deleteClick) {
		mDeleteClick = deleteClick;
	}

	/**
	 * 使用了github开源的ImageLoad进行了数据加载
	 */

	public HistoryAdapter(Context context, List<TraceItem> datas) {
		this.mDatas = datas;
		mInflater = LayoutInflater.from(context);
		initData(context);
	}

	private void initData(Context context) {
		km = new KeyManager(context);
		String key = "12345678909876543212345678909876";
		String iv = "1234567890987654";
		km.setIv(iv.getBytes());
		km.setId(key.getBytes());
		crypto = new Crypto(context);
		mContext = context;
	}

	public HistoryAdapter(Context context, List<TraceItem> datas, ListView lv) {
		this.mDatas = datas;
		mInflater = LayoutInflater.from(context);
		listView = lv;
		initData(context);
	}

	public HistoryAdapter(Context context, List<TraceItem> datas, List<TraceItem> datas1, SwipeDeleteListView1 lv) {
		this.mDatas = datas;
		this.mDatas1 = datas1;
		mInflater = LayoutInflater.from(context);
		listView = lv;
		initData(context);
	}

	public void addAll(List<TraceItem> mDatas) {
		this.mDatas.addAll(mDatas);
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		ListView mListView = null;
		if(parent instanceof SwipeDeleteListView1)
			mListView = (SwipeDeleteListView1) parent;
		else
			mListView = (ScrollListView) parent;
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.history_maps_adapter, parent, false);
			holder = new ViewHolder();

			holder.time_start = (TextView) convertView.findViewById(R.id.time_history);
			holder.time_end = (TextView) convertView.findViewById(R.id.time_history_end);
			holder.address_start = (TextView) convertView.findViewById(R.id.address_history);
			holder.address_destination = (TextView) convertView.findViewById(R.id.address_history_destination);
			holder.tag = (TextView) convertView.findViewById(R.id.number_history);
			holder.delete = (TextView) convertView.findViewById(R.id.delete);
			holder.step = (TextView) convertView.findViewById(R.id.step);


			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mDatas1 != null) {
			TraceItem traceItem1 = mDatas1.get(position);
//			try {
				StringBuilder builder = new StringBuilder("结束时间：");
//				builder.append(crypto.armorDecrypt(traceItem1.getDate()));
				builder.append(traceItem1.getDate());
//				holder.time_end.setText("结束时间：" + crypto.armorDecrypt(traceItem1.getDate()));
				holder.time_end.setText(builder);
				StringBuilder builder1 = new StringBuilder("目的地：");
//				builder1.append(crypto.armorDecrypt(traceItem1.getName()));
//				builder1.append(crypto.armorDecrypt(traceItem1.getAddress()));
				builder1.append(traceItem1.getAddress());
//				holder.address_destination.setText("目的地：" + crypto.armorDecrypt(traceItem1.getName()));
				holder.address_destination.setText(builder1);
//			} catch (InvalidKeyException e) {
//				e.printStackTrace();
//			} catch (NoSuchAlgorithmException e) {
//				e.printStackTrace();
//			} catch (NoSuchPaddingException e) {
//				e.printStackTrace();
//			} catch (IllegalBlockSizeException e) {
//				e.printStackTrace();
//			} catch (BadPaddingException e) {
//				e.printStackTrace();
//			} catch (InvalidAlgorithmParameterException e) {
//				e.printStackTrace();
//			}
		} else {
			holder.time_end.setVisibility(View.GONE);
			holder.address_destination.setVisibility(View.GONE);
			holder.tag.setVisibility(View.GONE);
		}
		TraceItem traceItem = mDatas.get(position);
		StringBuilder builder = new StringBuilder("编号：");
		builder.append(traceItem.getTag());
//		holder.tag.setText("编号：" + traceItem.getTag());
		holder.tag.setText(builder);
//		try {
			StringBuilder builder2 = new StringBuilder("出发时间：");
//			builder2.append(crypto.armorDecrypt(traceItem.getDate()));
			builder2.append(traceItem.getDate());
//			holder.time_start.setText("出发时间：" + crypto.armorDecrypt(traceItem.getDate()));
			holder.time_start.setText(builder2);
			/**
			 * 不能用TencemtMaps.crypto，因为已经销毁，onpause()
			 * */
			StringBuilder builder3 = new StringBuilder("出发地：");
//			builder3.append(crypto.armorDecrypt(traceItem.getName()));
//			builder3.append(crypto.armorDecrypt(traceItem.getAddress()));
			builder3.append(traceItem.getAddress());
//			holder.address_start.setText("出发地：" + crypto.armorDecrypt(traceItem.getName()));
			holder.address_start.setText(builder3);
		Log.e("Detail", String.valueOf(traceItem.getTag()));
		Log.e("Detail", String.valueOf(traceItem.getStep()));
		Log.e("Detail", String.valueOf(traceItem.getAddress()));
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e) {
//			e.printStackTrace();
//		} catch (BadPaddingException e) {
//			e.printStackTrace();
//		} catch (InvalidAlgorithmParameterException e) {
//			e.printStackTrace();
//		}
		StringBuilder builder4 = new StringBuilder("步数:");
//		builder4.append(mTraceDao.getLastStep().getStep());
		builder4.append(traceItem.getStep());
		holder.step.setText(builder4);
		final ViewHolder finalHolder = holder;
		final ListView finalMListView = mListView;
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final View dismissView = (View) v.getParent();
				ValueAnimator mValueAnimator;
//				动画
				final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
				final int originalHeight = dismissView.getHeight();

				mValueAnimator = ValueAnimator.ofInt(originalHeight, 0).setDuration(400);
				mValueAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mDatas.remove(position);
						mTraceDao.deleteAll(position+1);
						notifyDataSetChanged();
						if(finalMListView instanceof SwipeDeleteListView1) {
							((SwipeDeleteListView1)finalMListView).hiddenDeleteButton(true, true);
							((SwipeDeleteListView1)finalMListView).clearState();
						}
						finalHolder.delete.setVisibility(View.GONE);
					}
				});

				mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						lp.height = (Integer) valueAnimator.getAnimatedValue();
						if (lp.height > 0) {
							dismissView.setLayoutParams(lp);
						}
					}
				});
				mValueAnimator.start();

//				listView.turnToNormal();
				if(mContext instanceof IndoorLocationActivity) {
					IndoorLocationActivity activity = (IndoorLocationActivity) mContext;
					activity.click();
				}else if(mContext instanceof HistoryMaps){
					HistoryMaps activity = (HistoryMaps) mContext;
					activity.click();
				}
			}
		});


		return convertView;
	}

	public interface deleteClick{
		public void click(int position);

	}

	private final class ViewHolder {
		TextView time_start;
		TextView time_end;
		TextView tag;
		TextView address_start;
		TextView address_destination;
		TextView delete;
		TextView step;
	}
}
