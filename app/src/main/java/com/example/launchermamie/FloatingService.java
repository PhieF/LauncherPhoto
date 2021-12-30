package com.example.launchermamie;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.Stack;

/**
 * Created by phoenamandre on 01/02/16.
 */
public class FloatingService extends Service implements View.OnClickListener, BubbleLayout.OnBubbleClickListener {
    private static final int LOCK_MSG = 1;
    public static final String START_MINIMIZE = "start_minimize";
    public static FloatingService sService;
    public static final String NOTE = "param1";

    private WindowManager mWindowManager;
    private boolean contains;

    private View mContent;
    private WindowManager.LayoutParams paramsF;
    private FrameLayout mFrameLayout;
    private BroadcastReceiver mReceiver;

    private boolean mHasPressedMinimize;
    private FrameLayout mfragmentContainer;
    private ViewGroup mOptionMenuContainer;
    private boolean isLocked;

    private boolean screenshotThreadIsStarted;
    private long mLastCheck;
    private String mScreenshotPath;
    private int mLastVisibility;
    private BubbleLayout mBubble;
    private BubbleManager mBubbleManager;
    private String mChannelId="";

    public void onCreate() {
        super.onCreate();
        Log.d("FloatingService", "on create");
        sService = this;
        LayoutInflater li = LayoutInflater.from(this);
        mBubble = new BubbleLayout(this);
        mBubble.setMainBubbleView(li.inflate(R.layout.my_bubble, null));
        mBubble.setOnBubbleClickListener(this);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //requestMinimize();
                if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED))
                    mBubbleManager.putNearestBoarder();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MAIN);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mReceiver, filter);
        //image.setAlpha((float) 0.8);


        try {
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mBubbleManager = new BubbleManager(mWindowManager, this, mBubble);
          //  mBubbleManager.show();
            contains= true;
        } catch (Exception e){

        }

        //startScreenshotThread();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int ret = super.onStartCommand(intent, flags, startID);
        addFloatingView();
        if (intent == null || !intent.getBooleanExtra(START_MINIMIZE, false))
            invert();
        return ret;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(){

        };
    }


    public void addFloatingView() {
        if (!contains && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("", true)) {


            Intent it = new Intent(getApplicationContext(), MainActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    it,
                    0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&mChannelId.isEmpty()) {
                mChannelId = createNotificationChannel("sync2", "Sync Service");
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, mChannelId).
                    setSmallIcon(R.mipmap.ic_launcher).
                    setContentTitle(getString(R.string.app_name)).
                    setContentText(getString(R.string.app_name));


            startForeground(3, builder.build());
            //startForeground();
            paramsF = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);

            paramsF.gravity = Gravity.TOP | Gravity.LEFT;
            paramsF.x = 0;
            paramsF.y = 0;
           // mWindowManager.addView(image, paramsF);
           // image.setVisibility(View.GONE);

            contains = true;

        }


    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
    private void invert() {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        removeView();
        sService = null;

    }


    private void removeView() {
        if (contains) {
            mBubbleManager.remove();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == LOCK_MSG)
                lock();
        }

    };


    private void lock() {

    }

    @Override
    public void onClick(View view) {

       /* if (view == mContent.findViewById(R.id.minimize)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHasPressedMinimize = true;
                    requestMinimize();
                }
            }, 200);

        } else if (view == mDim) {
            if (image.getAlpha() == 1)
                image.setAlpha((float) 0.5);
            else
                image.setAlpha(1);

        } else if (view == mContent.findViewById(R.id.close)) {
            sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 200);

        } else if (view == mShadowButton) {

            if (image.getAlpha() == 1) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSPARENT);

                params.x = 0;
                params.y = 0;
                ((ViewGroup) mTitleBar.getParent()).removeView(mTitleBar);
                mWindowManager.addView(mTitleBar, params);

                paramsF.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            } else {
                mWindowManager.removeViewImmediate(mTitleBar);
                ((ViewGroup) mContent).addView(mTitleBar, 0);
                paramsF.flags = 0;
            }
            view.setFocusable(true);

            view.setFocusableInTouchMode(true);
            view.setEnabled(true);
            view.setClickable(true);


            image.setAlpha(image.getAlpha() == 1 ? (float) 0.5 : 1);
            mWindowManager.updateViewLayout(image, paramsF);
        }*/
    }

    public void requestMinimize() {

    }

    public void requestRestore() {
        if (!mHasPressedMinimize && contains) {
            requestMaximize();
        }
    }

    public void requestMaximize() {

    }

    @Override
    public void onMainBubbleClick() {
        Log.d("bubbledebug","onclick");
        MainActivity.restoreAll(this);

    }

    @Override
    public void onSecondaryBubbleClick(View v) {

    }

    @Override
    public void onRemoveToast() {
        mScreenshotPath = null;
    }

    @Override
    public void onDisplayToast() {

    }



    public void askDestroy() {
        Log.d("askDestroydebug", "askDestroy");
        stopSelf();
    }

    public void hideBubble() {
        if(mBubbleManager != null)
            mBubbleManager.hide();
    }
    public void showBubble() {
        if(mBubbleManager != null)
            mBubbleManager.show();
    }
}
