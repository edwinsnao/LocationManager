package com.example.fazhao.locationmanager.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.example.fazhao.locationmanager.baidu_map.widget.SwipeDeleteListView1;

/**
 * Created by fazhao on 2016/12/30.
 */

public class ScrollLinearLayout extends LinearLayout {
    private static final String TAG = "ScrollLinearLayout";
    private Scroller mScroller;

    public ScrollLinearLayout(Context context, AttributeSet attr) {
        super(context, attr);
        mScroller = new Scroller(context);
    }

    public void smoothScrollTo(int curX, int curY, int toX, int toY) {
        mScroller.startScroll(curX, curY, toX - curX, toY - curY,
                2 * Math.abs(toX - curX));
        invalidate();
    }

    public void stopScroll(){
        if (!mScroller.isFinished()){
            mScroller.abortAnimation();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (SwipeDeleteListView1.DBG) {
                Log.d(TAG, "computeScroll scrollX = " + mScroller.getCurrX());
            }
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    public boolean isScrolling() {
        return !mScroller.isFinished();
    }
}
