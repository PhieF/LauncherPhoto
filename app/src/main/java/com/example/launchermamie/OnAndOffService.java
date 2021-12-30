package com.example.launchermamie;

import  android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

public class OnAndOffService extends Service {

    public static boolean isStarted;
    private Handler mHandler;
    private boolean mIsMeantToBeOff = true;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private boolean mHasStarted;
    private  final long delay = 15*60*1000;
    private boolean mIsMeantToBePaused = false;

    public OnAndOffService() {
        super();

    }

    public void onDestroy(){
        super.onDestroy();
        isStarted = false;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startID) {
        isStarted = true;
        int ret = super.onStartCommand(intent, flags, startID);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(intent != null && intent.getAction() != null){
                    Log.d("intentdebug","action "+intent.getAction());
                    if(intent.getAction().equals("turnoff")){
                        mIsMeantToBeOff = true;
                        turnOffScreen();

                    }
                    else if(intent.getAction().equals("turnoffandrestart")){
                        turnOffScreen();
                        mIsMeantToBePaused = true;
                        Intent servIntent = new Intent(OnAndOffService.this, OnAndOffService.class);
                        servIntent.setAction("turnonnoteamviewer");
                        PendingIntent pendingIntent = PendingIntent.getService(OnAndOffService.this, 1, servIntent, 0);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay, pendingIntent);
                        else
                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay, pendingIntent);
                    }
                    else if(intent.getAction().equals("turnonnoteamviewer")){
                        if(mIsMeantToBeOff)
                            return;
                        mIsMeantToBePaused = false;
                        turnOnScreen();
                        Intent servIntent = new Intent(OnAndOffService.this, OnAndOffService.class);
                        servIntent.setAction("turnoffandrestart");
                        PendingIntent pendingIntent = PendingIntent.getService(OnAndOffService.this, 1, servIntent, 0);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay, pendingIntent);
                        else
                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay, pendingIntent);
                    }
                    else {
                        mIsMeantToBeOff = false;
                        turnOnScreen();
                        startTeamViewer();
                        Intent servIntent = new Intent(OnAndOffService.this, OnAndOffService.class);
                        servIntent.setAction("turnoffandrestart");
                        PendingIntent pendingIntent = PendingIntent.getService(OnAndOffService.this, 1, servIntent, 0);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay, pendingIntent);
                        else
                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay, pendingIntent);
                    }
                }
                else {
                    Log.d("intentdebug","no action");
                    mIsMeantToBeOff = false;
                    turnOnScreen();

                }
            }
        },2000);

        mPowerManager = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);


        Calendar calendar = Calendar.getInstance();

// if it's after or equal 9 am schedule for next day
       // if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 9) {
       //     calendar.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
       // }
        if(!mHasStarted) {


            Intent servIntent = new Intent(this, OnAndOffService.class);
            servIntent.setAction("turnoff");

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, servIntent, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 20);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Log.d("intentdebug","trigger at "+calendar.getTimeInMillis());

            Log.d("intentdebug","trigger in "+(calendar.getTimeInMillis()- System.currentTimeMillis()));


           alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                  AlarmManager.INTERVAL_DAY, pendingIntent);

            servIntent = new Intent(this, OnAndOffService.class);
            servIntent.setAction("turnon");

            pendingIntent = PendingIntent.getService(this, 1, servIntent, 0);

            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
                long wait = 0;
                public void onReceive(Context context, Intent intent) {
                    if(mIsMeantToBeOff || mIsMeantToBePaused)
                        return;
                    int rawlevel = intent.getIntExtra("level", -1);
                    int scale = intent.getIntExtra("scale", -1);
                    int level = -1;
                    if (rawlevel >= 0 && scale > 0) {
                        level = (rawlevel * 100) / scale;
                    }
                    Log.d("batterydebug", "Battery Level Remaining: " + level + "%");
                    if(level<80) {
                        wait = 0;
                       turnOffScreen();
                    }
                   else if(level>=99) {
                       if(wait == 0)
                           wait = System.currentTimeMillis();
                       if(System.currentTimeMillis()-wait >= 15*60*1000)
                            turnOnScreen();
                    }
                }
            };




            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batteryLevelReceiver, batteryLevelFilter);



        }
        mHasStarted = true;
        return ret;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void startTeamViewer(){
        Log.d("teamdebug","startingteamviewer");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.restoreAll(OnAndOffService.this);

            }
        }, 5000);
    }

    @SuppressLint("InvalidWakeLockTag")
    public void turnOnScreen(){
        // turn on screen

        Log.d("intentdebug", "ON!");

        if(mWakeLock==null || !mWakeLock.isHeld()) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
            mWakeLock.acquire();

            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
           // wifiManager.setWifiEnabled(true);

            //startTeamViewer();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.staticReload();
                }
            }, 30000);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    public void turnOffScreen(){
        // turn off screen
        Log.v("intentdebug", "OFF!"+(mWakeLock!=null));
        if(mWakeLock!=null){
            mWakeLock.release();
            mWakeLock = null;
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
           // wifiManager.setWifiEnabled(false);
        }
    }
}
