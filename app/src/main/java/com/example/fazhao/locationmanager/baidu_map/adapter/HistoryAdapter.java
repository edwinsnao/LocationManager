package com.example.fazhao.locationmanager.baidu_map.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.baidu_map.activity.HistoryMaps;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.widget.ScrollListView;
import com.example.fazhao.locationmanager.baidu_map.widget.SwipeDeleteListView1;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
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
    private deleteClick mDeleteClick;


    public void setClickListener(deleteClick deleteClick) {
        mDeleteClick = deleteClick;
    }

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
        if (parent instanceof SwipeDeleteListView1)
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
            StringBuilder builder = new StringBuilder("结束时间：");
            builder.append(traceItem1.getDate());
            holder.time_end.setText(builder);
            StringBuilder builder1 = new StringBuilder("目的地：");
            builder1.append(traceItem1.getAddress());
            holder.address_destination.setText(builder1);
        } else {
            holder.time_end.setVisibility(View.GONE);
            holder.address_destination.setVisibility(View.GONE);
            holder.tag.setVisibility(View.GONE);
        }
        TraceItem traceItem = mDatas.get(position);
        StringBuilder builder = new StringBuilder("编号：");
        builder.append(traceItem.getTag());
        holder.tag.setText(builder);
        StringBuilder builder2 = new StringBuilder("出发时间：");
        builder2.append(traceItem.getDate());
        holder.time_start.setText(builder2);
        StringBuilder builder3 = new StringBuilder("出发地：");
        builder3.append(traceItem.getAddress());
        holder.address_start.setText(builder3);
        if (traceItem.getStep() != -1) {
            StringBuilder builder4 = new StringBuilder("步数:");
            builder4.append(traceItem.getStep());
            holder.step.setText(builder4);
        }
        else if (traceItem.getUptime() != -1) {
            StringBuilder builder4 = new StringBuilder("时长:");
            builder4.append(traceItem.getUptime());
            holder.step.setText(builder4);
        }
        else if (traceItem.getDistance() != -1) {
            StringBuilder builder4 = new StringBuilder("距离:");
            builder4.append(traceItem.getDistance());
            holder.step.setText(builder4);
        }
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
                        mTraceDao.deleteAll(position + 1);
                        notifyDataSetChanged();
                        if (finalMListView instanceof SwipeDeleteListView1) {
                            ((SwipeDeleteListView1) finalMListView).hiddenDeleteButton(true, true);
                            ((SwipeDeleteListView1) finalMListView).clearState();
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

                if (mContext instanceof IndoorLocationActivity) {
                    IndoorLocationActivity activity = (IndoorLocationActivity) mContext;
                    activity.click();
                } else if (mContext instanceof HistoryMaps) {
                    HistoryMaps activity = (HistoryMaps) mContext;
                    activity.click();
                }
            }
        });


        return convertView;
    }

    public interface deleteClick {
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
