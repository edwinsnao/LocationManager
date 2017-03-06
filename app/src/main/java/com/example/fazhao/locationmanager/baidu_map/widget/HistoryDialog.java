package com.example.fazhao.locationmanager.baidu_map.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;

/**
 * Created by fazhao on 2017/1/3.
 */

public class HistoryDialog extends Dialog {
    public SwipeDeleteListView1 lv;
    private Button negativeButton, deleteAll;
    public TextView title;
    private Context mContext;

    public HistoryDialog(Context context) {
        super(context, R.style.customeDialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = context;
        setCustomDialog();
    }


    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);
        title = (TextView) mView.findViewById(R.id.history_num);
        lv = (SwipeDeleteListView1) mView.findViewById(R.id.list_history);
        lv.setDivider(mContext.getApplicationContext().getResources().getDrawable(R.drawable.divider));
        lv.setFooterDividersEnabled(true);
        lv.setHeaderDividersEnabled(true);
        negativeButton = (Button) mView.findViewById(R.id.cancel);
        deleteAll = (Button) mView.findViewById(R.id.delete_all);
        super.setContentView(mView);
    }

    @Override
    public void setContentView(int layoutResID) {
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
    }

    @Override
    public void setContentView(View view) {
    }

    /**
     * 取消键监听器
     *
     * @param listener
     */
    public void setOnNegativeListener(View.OnClickListener listener) {
        negativeButton.setOnClickListener(listener);
    }

    /**
     * 删除记录监听器
     *
     * @param listener
     */
    public void setOnDeleteAllListener(View.OnClickListener listener) {
        deleteAll.setOnClickListener(listener);
    }
}
