package com.example.fazhao.locationmanager.baidu_map;

/**
 * Created by fazhao on 2016/12/20.
 */


        import com.baidu.location.LocationClient;

        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.os.PowerManager;

class ScreenLockLocation {

    private AlarmManager mAlarmManger = null;
    private TimerReceiver mLister = null;
    private PendingIntent pi = null;
    private Context mContext = null;
    private String actionStr = "com.baidu.locSDK.test.timer1";
    private PowerManager.WakeLock wl = null;

    public ScreenLockLocation(Context mC) {
        mContext = mC.getApplicationContext();
        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);

        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationWackLock");
        wl.setReferenceCounted(false);
    }

    public void start() {
        mAlarmManger = (AlarmManager) mContext
                .getSystemService(Context.ALARM_SERVICE);
        mLister = new TimerReceiver();
        mContext.registerReceiver(mLister, new IntentFilter(actionStr));
        pi = PendingIntent.getBroadcast(mContext, 0, new Intent(actionStr),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManger.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
                30000, pi);
    }

    public void stop(){
        mContext.unregisterReceiver(mLister);
        mAlarmManger.cancel(pi);
    }

    public void releaseWackLock(){
        if(wl != null && wl.isHeld())
            wl.release();
    }

    public class TimerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

//            LocationClient mLocClient = Location.getInstance().mLocationClient;
//            if (mLocClient != null && mLocClient.isStarted()){
//                wl.acquire();
//                mLocClient.requestLocation();
            }
        }

    }

