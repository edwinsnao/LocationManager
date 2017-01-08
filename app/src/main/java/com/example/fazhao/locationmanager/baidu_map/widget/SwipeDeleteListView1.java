package com.example.fazhao.locationmanager.baidu_map.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Scroller;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.activity.ScrollLinearLayout;

import static android.R.attr.scrollbarTrackVertical;

/**
 * Created by fazhao on 2016/12/30.
 */

public class SwipeDeleteListView1 extends ListView {
    private final static String TAG = "Mms/SwipeListView";
    public final static boolean DBG = false;

    private int mDeleteButtonWidth = 80; //original value of delete button width, will get real value from xml
    private Boolean mIsHorizontal; //true, if is moving horizontal; false if is moving vertical
    private View mCurrentItemView; //current item which display delete button
    private ScrollLinearLayout mCurrentScrollView; //the current scroll item
    //    private View mSwipeLeftBgView; //the swipe left view
//    private View mReadStatusView; //read status button to mark read / unread
    private View mDeleteView; //delete button
    private float mFirstX; //the x position when touch down
    private float mFirstY; //the y position when touch down
    private boolean mIsShown; //true is display delete button
    private boolean mSupportSwipeDelete = true; //true is support swipe delete
    private boolean mSupportQuickMark = false; //true is support mark read/unread
    private boolean mIsStatusMarked = true; //true if marked the read status, else if not marked the read status
    private OnStatusChangeListener mStatusListener = null; //when read status changed, will call this to notify others
    //private boolean mIsReadStatusChanged = false; //true is the read status changed
    //private READ_STATUS mItemReadStatue = READ_STATUS.READ; //item read status
    private boolean mIsGiveupTouchEvent = false;
    private STATUS mStatus = STATUS.IDLE;
    public static final int MAX_Y_OVERSCROLL_DISTANCE = 100;
    private int mMaxYOverscrollDistance;
    private ViewGroup mParentView;
    private Scroller mScroller;
    private enum STATUS{
        DRAGGING,
        SHOW,
        IDLE
    };
//    private long mLastHideTime = 0;

    public SwipeDeleteListView1(Context context) {
        super(context);
        //get delete button width, current the read status button and the delete button is same width
        mDeleteButtonWidth = context.getResources().getDimensionPixelSize(
                R.dimen.swipe_delete_button_width);
        initBounceListView(context);
    }

    public SwipeDeleteListView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        //get delete button width, current the read status button and the delete button is same width
        mDeleteButtonWidth = context.getResources().getDimensionPixelSize(
                R.dimen.swipe_delete_button_width);
        initBounceListView(context);
    }

    public SwipeDeleteListView1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //get delete button width, current the read status button and the delete button is same width
        mDeleteButtonWidth = context.getResources().getDimensionPixelSize(
                R.dimen.swipe_delete_button_width);
        initBounceListView(context);
    }


    private void initBounceListView(Context mContext){
        //get the density of the screen and do some maths with it on the max overscroll distance
        //variable so that you get similar behaviors no matter what the screen size

        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        final float density = metrics.density;
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(50);
        mScroller = new Scroller(mContext);
        mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            // 获取SildingFinishLayout所在布局的父布局
            mParentView = (ViewGroup) this.getParent();
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if (mScroller.computeScrollOffset()) {
            mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * return true, touch event deliver to listView. return false, touch event deliver to child. if
     * move, return true
     */
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//        case MotionEvent.ACTION_DOWN:
//            log(Log.DEBUG, TAG, "onInterceptTouchEvent ACTION_DOWN");
//            break;
//
//        case MotionEvent.ACTION_MOVE:
//            log(Log.DEBUG, TAG, "onInterceptTouchEvent ACTION_MOVE return super");
//            break;
//
//        case MotionEvent.ACTION_UP:
//        case MotionEvent.ACTION_CANCEL:
//            log(Log.DEBUG, TAG, "onInterceptTouchEvent ACTION_UP return super");
//            break;
//        }
//
//        return super.onInterceptTouchEvent(ev);
//    }

    /**
     * @param dx
     * @param dy
     * @return judge if can judge scroll direction
     */
    private boolean judgeScrollDirection(float dx, float dy) {
        boolean canJudge = true;
        if (Math.abs(dx) > 30){
            if (Math.abs(dx) > 2 * Math.abs(dy)){
                mIsHorizontal = true;
            } else{
                mIsHorizontal = false;
            }
        } else{
            canJudge = false;
        }

        log(Log.DEBUG, TAG, "judgeScrollDirection, mIsHorizontal="+mIsHorizontal);
        return canJudge;
    }

    /**
     * return false, can't move any direction. return true, cant't move
     * vertical, can move horizontal. return super.onTouchEvent(ev), can move
     * both.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mSupportSwipeDelete) {
            return super.onTouchEvent(ev);
        }

        float lastX = ev.getX();
        float lastY = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsStatusMarked = false;
                //mIsReadStatusChanged = false;

                mFirstX = lastX;
                mFirstY = lastY;
                mIsHorizontal = null;

                int motionPosition2 = pointToPosition((int) lastX, (int) lastY);

                log(Log.DEBUG, TAG, "onTouchEvent, ACTION_DOWN, motionPosition=" + motionPosition2);

                if (motionPosition2 >= 0) {
                    View currentItemView = getChildAt(motionPosition2
                           - getFirstVisiblePosition());
                    if (mIsShown){
                        if (currentItemView != mCurrentItemView){
                            mIsGiveupTouchEvent = true;
                            hiddenDeleteButton(false, true);
                            return true;
                        } else {
                            currentItemView.setPressed(false);
                        }
                    } else {
                        mCurrentItemView = currentItemView;
                        mCurrentScrollView = (ScrollLinearLayout) currentItemView.findViewById(R.id.scroll);
                        mDeleteView = currentItemView.findViewById(R.id.delete);
                    }
                } else{
                    if (mIsShown){
                        hiddenDeleteButton(false, false);
                    }
                }

                mIsGiveupTouchEvent = false;
                log(Log.DEBUG, TAG, "onTouchEvent, ACTION_DOWN" );
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsGiveupTouchEvent){
                    return true;
                }
                if (mCurrentItemView == null) {
                    break;
                }
                float dx = lastX - mFirstX;
                float dy = lastY - mFirstY;

                //如果没有方向，必须获取方向
                if (mIsHorizontal == null) {
                    if (!judgeScrollDirection(dx, dy)) {
                        if (mIsShown){
                            log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE but break, mIsHorizontal=" + mIsHorizontal + " return true");
                            return true;
                        } else{
                            log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE but break, mIsHorizontal=" + mIsHorizontal + " return false");
                            break;
                        }
                    }
                }

                log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE mIsHorizontal=" + mIsHorizontal);

                if (mIsHorizontal) {
                    setLongClickable(false);

                    setPressed(false);
                    if(mCurrentItemView != null)
                    mCurrentItemView.setPressed(false);

                    //如果是横向移动的话，做下列处理
                    if (dx <= 0 && mIsStatusMarked){
                        mIsStatusMarked = false;
                    }

                    if (dx < 0 || mIsShown) {
                        //这里是处理左滑动删除的事情
                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, dx=" + dx);
                        if (mIsShown){
                            dx = dx - mDeleteButtonWidth;
                            log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, dx=" + dx);
                        }

                        if (dx < 0){
//                      mReadStatusView.setVisibility(View.INVISIBLE);
                            mDeleteView.setVisibility(View.VISIBLE);

                            mCurrentScrollView.scrollTo((int) (-dx), 0);
                            mStatus = STATUS.DRAGGING;
                        } else {
//                      mReadStatusView.setVisibility(View.VISIBLE);
                            mDeleteView.setVisibility(View.INVISIBLE);

                            if (mSupportQuickMark){
                                //这里item会处于选中状态，需要重置这个状态
                                mCurrentScrollView.scrollTo((int) (-dx), 0);
                                mStatus = STATUS.DRAGGING;
                            } else{
                                hiddenDeleteButton(false, false);
                            }
                        }

                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE");
                    } else if (dx > 0 && mSupportQuickMark){
                        //这里是处理右滑动标记的问题
                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, dx=" + dx);
                        mDeleteView.setVisibility(View.INVISIBLE);
//                  mReadStatusView.setVisibility(View.VISIBLE);
                        if (dx > mDeleteButtonWidth && !mIsStatusMarked){
//                      mIsReadStatusChanged = true;
//                      log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, mIsReadStatusChanged");

//                      TextView tv = (TextView)mReadStatusView;
//                      int motionPosition = pointToPosition((int) mFirstX, (int) mFirstY);
//
//                      if (tv.getText().equals(getResources().getString(R.string.conversation_status_unread))){
//                          mItemReadStatue = READ_STATUS.READ;
//                          tv.setBackground(getResources().getDrawable(R.color.conversation_status_read));
//                          tv.setText(R.string.conversation_status_read);
//                          if (mStatusListener != null){
//                              log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, mStatusListener=" +mStatusListener + ",MOVING");
//                              mStatusListener.onStatusChange(mCurrentItemView, motionPosition, mItemReadStatue, DRAG_STATUS.MOVING);
//                          }
//                      } else{
//                          mItemReadStatue = READ_STATUS.UNREAD;
//                          tv.setBackground(getResources().getDrawable(R.color.conversation_status_unread));
//                          tv.setText(R.string.conversation_status_unread);
//                          if (mStatusListener != null){
//                              log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, mStatusListener=" +mStatusListener + ",MOVING");
//                              mStatusListener.onStatusChange(mCurrentItemView, motionPosition, mItemReadStatue, DRAG_STATUS.MOVING);
//                          }
//                      }
                            mIsStatusMarked = true;
                        }

                        mCurrentScrollView.stopScroll();
                        mCurrentScrollView.scrollTo((int) (-dx), 0);
                        mStatus = STATUS.DRAGGING;
                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, " + "scroll to " + (-dx));
                    }
                    //return true means, list will not scroll in Y direction
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                    return true;

                } else {
                    //如果是竖向移动的话，如果已经显示删除，需要取消掉
                    if (mIsShown) {
                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_MOVE, hiddenDeleteButton");
                        hiddenDeleteButton(false, false);

                        //如果没有这个则list继续接受move事件，导致list 上下滑动
                        mIsGiveupTouchEvent = true;
                        //return true means, list will not scroll in Y direction
                        MotionEvent cancelEvent = MotionEvent.obtain(ev);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                        super.onTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                        return true;
                    }
                    mCurrentItemView = null;
//                    scrollBy(0,(int)(dy));
//                    smoothScrollBy((int) (-dy),100);
//                    log(Log.DEBUG, "、haha", "mCurrentItemView="+null);
                }
                break;

            case MotionEvent.ACTION_UP:
                log(Log.DEBUG, TAG, "onTouchEvent, ACTION_UP, mIsHorizontal="+mIsHorizontal);
                if (mIsGiveupTouchEvent){
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                    return true;
                }

                if(mCurrentItemView == null)
                    break;

                if (mIsHorizontal != null && mIsHorizontal) {
//              if (mIsReadStatusChanged){
//                  if (mStatusListener != null){
//                      log(Log.DEBUG, TAG, "onTouchEvent, ACTION_UP, mStatusListener=" +mStatusListener + ",mItemReadStatue=" + mItemReadStatue + ",FINISH");
//                      mStatusListener.onStatusChange(mCurrentItemView, -1, mItemReadStatue, DRAG_STATUS.FINISH);
//                  }
//              }

                    float moveDistance = mFirstX - lastX;
                    if (mIsShown){
                        moveDistance += mDeleteButtonWidth;
                    }
                    if (moveDistance > mDeleteButtonWidth / 2) {
                        // 如果移动的距离超过delete button的一半，则显示delete button
                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_UP, showDeleteButton");
                        //这样子做体验不好，除非加上速度检查器，所以还是隐藏吧
                        showDeleteButton(mCurrentItemView);
                    } else {
                        // 如果移动的距离超过delete button的一半，则显示delete button
                        log(Log.DEBUG, TAG, "onTouchEvent, ACTION_UP, hiddenDeleteButton");
                        hiddenDeleteButton(false, true);
                    }
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                log(Log.DEBUG, TAG, "onTouchEvent, ACTION_CANCEL");
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void showDeleteButton(View outview) {
        if (mCurrentScrollView == null) {
            log(Log.DEBUG, TAG, "showDeleteButton nothing");
            return;
        }

        mCurrentScrollView.smoothScrollTo(mCurrentScrollView.getScrollX(), 0, mDeleteButtonWidth, 0);
        mIsShown = true;
        mStatus = STATUS.SHOW;
        mDeleteView.setClickable(true);

        log(Log.DEBUG, TAG, "showDeleteButton viewl");
    }

    public void clearState(){
        mCurrentItemView = null;
    }

    public void hiddenDeleteButton(boolean quick, boolean current) {
        if (mCurrentScrollView == null) {
            log(Log.DEBUG, TAG, "hiddenDeleteButton viewl=nothing");
            return;
        }

//        log(Log.DEBUG, TAG, "hiddenDeleteButton quick="+quick+",current="+current);

        mCurrentScrollView.stopScroll();
        if (quick) {
            mCurrentScrollView.scrollTo(0, 0);
        } else {
            mCurrentScrollView.smoothScrollTo(mCurrentScrollView.getScrollX(), 0, 0, 0);
        }
        mIsShown = false;
        mStatus = STATUS.IDLE;

        mIsStatusMarked = false;
        mCurrentItemView = null;
        mDeleteView.setClickable(false);

        setLongClickable(true);
//        mLastHideTime = System.currentTimeMillis();

//        log(Log.DEBUG, "haha", "mCurrentItemView="+null);
    }

    public int getRightViewWidth() {
        return mDeleteButtonWidth;
    }

    public void setRightViewWidth(int deleteButtonWidth) {
        mDeleteButtonWidth = deleteButtonWidth;
    }

    public void deleteItem(View v) {
        hiddenDeleteButton(true, true);
    }

    public void setSupportSwipeDelete(boolean supportSwipeDel) {
        mSupportSwipeDelete = supportSwipeDel;
    }

    public boolean deleteButtonShown() {
        return mIsShown || (mStatus != STATUS.IDLE);
    }

    /* Begin add for RCS */
    public boolean isShowDelBtn() {
        return mStatus == STATUS.SHOW;
    }
    /* End add for RCS */

    public enum READ_STATUS{
        UNREAD,
        READ
    }

    public enum DRAG_STATUS{
        MOVING,
        FINISH
    }

    public interface OnStatusChangeListener{
        public void onStatusChange(View view, int position, READ_STATUS read_status, DRAG_STATUS drag_status);
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener){
        mStatusListener = listener;
    }

    private void log(int type, String tag, String event){
        if (DBG){
            switch (type){
                case Log.INFO:
                    Log.i(tag, event);
                    break;

                case Log.ERROR:
                    Log.e(tag, event);
                    break;

                case Log.DEBUG:
                    Log.d(tag, event);
            }
        }
    }
}