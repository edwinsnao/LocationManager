package com.example.fazhao.locationmanager.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;
import com.tencent.tencentmap.streetviewsdk.Marker;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanorama;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanoramaCamera;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanoramaView;

/**
 * Created by Kings on 2016/2/10.
 */
public class StreetViewActivity extends Activity implements
        StreetViewPanorama.OnStreetViewPanoramaChangeListener, StreetViewPanorama.OnStreetViewPanoramaFinishListner,
        StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener {
    private StreetViewPanoramaView mPanoramaView;
    private StreetViewPanorama  mPanorama;

    private RelativeLayout mControlPanel;
    private Button btnTurnUp;
    private Button btnTurnDown;
    private Button btnTurnLeft;
    private Button btnTurnRight;
    private CheckBox cbGuidance;
    private CheckBox cbGestures;
    private CheckBox cbStreet;
    private CheckBox cbZoom;
    private CheckBox cbNavigation;
    private CheckBox cbGallery;
    private CheckBox cbScenceName;
    private CheckBox cbIndoorLink;

    private Animation controlAppear;
    private Animation controlVanish;

    private Double arg0 = null;
    private Double arg1 = null;


    private boolean streetViewPanoramaState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streetview);
        init();
    }

    private void init() {
        mPanoramaView = (StreetViewPanoramaView)findViewById(R.id.panorama_view);
        mPanorama = mPanoramaView.getStreetViewPanorama();
        //初始化地点为中国技术交易大厦
        arg0 = getIntent().getDoubleExtra("param0",0);
        arg1 = getIntent().getDoubleExtra("param1",0);
//        mPanorama.setPosition(39.984122, 116.307894);
        mPanorama.setPosition(arg0, arg1);
//		mPanorama.setPosition("10011001120307113241300");
        mPanorama.setOnStreetViewPanoramaChangeListener(this);
        mPanorama.setOnStreetViewPanoramaFinishListener(this);
        mPanorama.setOnStreetViewPanoramaCameraChangeListener(this);
        mControlPanel = (RelativeLayout)findViewById(R.id.layout_orientation_panel);
        mControlPanel.setAnimationCacheEnabled(true);
        mControlPanel.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        btnTurnUp = (Button)findViewById(R.id.btn_turn_up);
        btnTurnDown = (Button)findViewById(R.id.btn_turn_down);
        btnTurnLeft = (Button)findViewById(R.id.btn_turn_left);
        btnTurnRight = (Button)findViewById(R.id.btn_turn_right);
        cbGestures = (CheckBox)findViewById(R.id.cb_gestures);
        cbGuidance = (CheckBox)findViewById(R.id.cb_guidance);
        cbStreet = (CheckBox)findViewById(R.id.cb_streetname);
        cbZoom = (CheckBox)findViewById(R.id.cb_zoom);
        cbNavigation = (CheckBox)findViewById(R.id.cb_navigation);
        cbGallery = (CheckBox)findViewById(R.id.cb_gallery);
        cbScenceName = (CheckBox)findViewById(R.id.cb_scence_name);
        cbIndoorLink = (CheckBox)findViewById(R.id.cb_indoor_link);

        cbGestures.setChecked(mPanorama.isPanningGesturesEnabled());
        cbGuidance.setChecked(mPanorama.isIndoorGuidanceEnabled());
        cbStreet.setChecked(mPanorama.isStreetNamesEnabled());
        cbZoom.setChecked(mPanorama.isZoomGesturesEnabled());
        cbNavigation.setChecked(mPanorama.isUserNavigationEnabled());
        cbGallery.setChecked(mPanorama.isStreetGalleryEnabled());
        cbScenceName.setChecked(mPanorama.isScenceNameEnabled());
        cbIndoorLink.setChecked(mPanorama.isIndoorLinkPoiEnabled());

        addMarker();
        bindPanleListener();
        bindCheckBoxListener();
        setAnimation();
    }

    private void addMarker() {

        final Marker marker1 = new Marker(39.984122, 116.307894){
            @Override
            public void onClick(float arg0, float arg1) {
                // TODO Auto-generated method stub
                mPanorama.setPosition(39.984122, 116.307894);
                ToastUtil.showShortToast(StreetViewActivity.this, "到达中国技术交易大厦");
                super.onClick(arg0, arg1);
            }
        };
        mPanorama.addMarker(marker1);

        Marker marker2 = new Marker(39.992932, 116.310211,
                convertViewToBitmap(getMarkerLayout("点我进如北京大学"))){
            @Override
            public void onClick(float arg0, float arg1) {
                // TODO Auto-generated method stub
                //使用街景id进入街景
                mPanorama.setPosition("10011009120328110539700");
                marker1.updateIcon(convertViewToBitmap(getMarkerLayout("点我到中国技术交易大厦")));
                ToastUtil.showShortToast(StreetViewActivity.this, "进入北京大学");
                super.onClick(arg0, arg1);
            }

            @Override
            public float onGetItemScale(double arg0, float arg1) {
                // TODO Auto-generated method stub
                //Log.i("marker", "distance:" + arg0 +"; angleScale:" + arg1);
                return super.onGetItemScale(0.2*arg0, arg1);
            }
        };
        mPanorama.addMarker(marker2);

        Marker marker3 = new Marker(39.984548, 116.309579);
        mPanorama.addMarker(marker3);
    }

    private void bindPanleListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                controlVanish.reset();
                controlVanish.start();
                switch (v.getId()) {
                    case R.id.btn_turn_up:
                        mPanorama.setPanoramaTilt(mPanorama.getPanoramaTilt() + 10);
                        break;
                    case R.id.btn_turn_down:
                        mPanorama.setPanoramaTilt(mPanorama.getPanoramaTilt() - 10);
                        break;
                    case R.id.btn_turn_left:
                        mPanorama.setPanoramaBearing(mPanorama.getPanoramaBearing() - 10);
                        break;
                    case R.id.btn_turn_right:
                        mPanorama.setPanoramaBearing(mPanorama.getPanoramaBearing() + 10);
                        break;

                    default:
                        break;
                }
            }
        };

        btnTurnUp.setOnClickListener(clickListener);
        btnTurnDown.setOnClickListener(clickListener);
        btnTurnRight.setOnClickListener(clickListener);
        btnTurnLeft.setOnClickListener(clickListener);
    }

    private void bindCheckBoxListener() {
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                switch (buttonView.getId()) {
                    case R.id.cb_gestures:
                        mPanorama.setPanningGesturesEnabled(isChecked);
                        break;
                    case R.id.cb_guidance:
                        mPanorama.setIndoorGuidanceEnabled(isChecked);
                        break;
                    case R.id.cb_streetname:
                        mPanorama.setStreetNamesEnabled(isChecked);
                        break;
                    case R.id.cb_zoom:
                        mPanorama.setZoomGesturesEnabled(isChecked);
                        break;
                    case R.id.cb_navigation:
                        mPanorama.setUserNavigationEnabled(isChecked);
                        break;
                    case R.id.cb_gallery:
                        mPanorama.setStreetGalleryEnabled(isChecked);
                        break;
                    case R.id.cb_scence_name:
                        mPanorama.setScenceNameEnabled(isChecked);
                        break;
                    case R.id.cb_indoor_link:
                        mPanorama.setIndoorLinkPoiEnabled(isChecked);
                        break;

                    default:
                        break;
                }
            }
        };
        cbGestures.setOnCheckedChangeListener(listener);
        cbGuidance.setOnCheckedChangeListener(listener);
        cbStreet.setOnCheckedChangeListener(listener);
        cbZoom.setOnCheckedChangeListener(listener);
        cbNavigation.setOnCheckedChangeListener(listener);
        cbGallery.setOnCheckedChangeListener(listener);
        cbScenceName.setOnCheckedChangeListener(listener);
        cbIndoorLink.setOnCheckedChangeListener(listener);
    }

    private void setAnimation() {
        controlAppear = AnimationUtils.loadAnimation(this, R.anim.control_appear);
        controlAppear.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                mControlPanel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                if (isStreetViewPanoramaFinish()) {
                    mControlPanel.setAnimation(controlVanish);
                }
            }
        });

        controlVanish = AnimationUtils.loadAnimation(StreetViewActivity.this,
                R.anim.control_vanish);
        controlVanish.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                mControlPanel.setVisibility(View.GONE);
            }
        });

    }

    private LinearLayout getMarkerLayout(String title) {
        LayoutInflater layInflater = getLayoutInflater();
        LinearLayout markerLayout = (LinearLayout)layInflater
                .inflate(R.layout.marker, null);
        TextView tvMarkerTitle = (TextView)markerLayout
                .findViewById(R.id.marker_title);

        tvMarkerTitle.setText(title);
        return markerLayout;
    }

    private Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    private void setStreetViewPanoramaState(boolean state) {
        streetViewPanoramaState = state;
    }

    private boolean isStreetViewPanoramaFinish() {
        return streetViewPanoramaState;
    }

    @Override
    public void OnStreetViewPanoramaFinish(boolean arg0) {
        // TODO Auto-generated method stub
        setStreetViewPanoramaState(true);
        mControlPanel.startAnimation(controlVanish);
    }

    @Override
    public void onStreetViewPanoramaChange(String arg0) {
        // TODO Auto-generated method stub
        setStreetViewPanoramaState(false);
        if (mControlPanel.getVisibility() != View.GONE) {
            return;
        }
        mControlPanel.startAnimation(controlAppear);
    }

    @Override
    public void onStreetViewPanoramaCameraChange(final StreetViewPanoramaCamera arg0) {
        // TODO Auto-generated method stub
//		Log.i("camera", "camera zoom:" + arg0.zoom +
//				"\ncamera tilt:" + arg0.tilt + "\ncamera bearing:" + arg0.bearing);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mControlPanel.getVisibility() == View.GONE) {
                    mControlPanel.startAnimation(controlAppear);
                } else {
                    controlVanish.reset();
                    controlVanish.start();
                }
                if (arg0.tilt > -15f && arg0.tilt < 90f) {
                    if (!btnTurnDown.isEnabled()) {
                        btnTurnDown.setEnabled(true);
                    }
                    if (!btnTurnUp.isEnabled()) {
                        btnTurnUp.setEnabled(true);
                    }
                }
                if (arg0.tilt >= 90f) {
                    btnTurnUp.setEnabled(false);

                } else if (arg0.tilt <= -15f) {
                    btnTurnDown.setEnabled(false);
                }
            }
        });
    }
}
