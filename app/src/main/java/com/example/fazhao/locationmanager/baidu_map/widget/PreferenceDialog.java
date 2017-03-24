package com.example.fazhao.locationmanager.baidu_map.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;

/**
 * Created by fazhao on 2017/1/3.
 */

public class PreferenceDialog extends Dialog {
    public SwipeDeleteListView1 lv;
    private Button negativeButton, okButton;
    public EditText mEditText;
    private Context mContext;

    public PreferenceDialog(Context context) {
        super(context, R.style.customeDialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = context;
        setCustomDialog();
    }


    private void setCustomDialog() {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.preference_dialog, null);
        mEditText = (EditText) mView.findViewById(R.id.preference_input);
        negativeButton = (Button) mView.findViewById(R.id.preference_cancel);
        okButton = (Button) mView.findViewById(R.id.preference_ok);
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
    public void setOnPositiveListener(View.OnClickListener listener) {
        okButton.setOnClickListener(listener);
    }
}
