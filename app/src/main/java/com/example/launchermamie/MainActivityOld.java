package com.example.launchermamie;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivityOld extends AppCompatActivity {

    public static MainActivityOld sActivity;
    private boolean isPaused;
    private long  lastclick = 0;

    //private String addUrl="&child=suzanne";
    private ServiceConnection mConn;
    private WebViewWrapper mWebViewWrapper;
    private String addUrl="";
    private String mPassword;

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void unsetWebview(){

    }

    public void setWebview(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPassword = getString(R.string.password);
        getSupportActionBar().hide();
        sActivity = this;
        Log.d("permdebug","poil");

        setContentView(R.layout.activity_main);
        mConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("ispauseddebug","isPaused "+isPaused);
                if(!isPaused)
                    FloatingService.sService.hideBubble();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        if(!OnAndOffService.isStarted) {
            Intent servIntent = new Intent(this, OnAndOffService.class);
            servIntent.setAction("turnon");
            startService(servIntent);
        }

        startService(new Intent(this, FloatingService.class));
        bindService(new Intent(this, FloatingService.class), mConn, BIND_AUTO_CREATE);
        mWebViewWrapper = new WebViewWrapper(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        ((FrameLayout)findViewById(R.id.click_container)).addView(mWebViewWrapper.getView(),0, params);
        mWebViewWrapper.getView().requestFocus();
        mWebViewWrapper.getView().setVerticalScrollBarEnabled(false);
        mWebViewWrapper.getView().setHorizontalScrollBarEnabled(false);

        mWebViewWrapper.loadUri("https://ovh2.phie.ovh/mamie/?psw="+mPassword+"&t="+Math.random() +addUrl);
        findViewById(R.id.click_handler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()-lastclick<5000){
                    restoreAll(MainActivityOld.this);

                }
                lastclick = System.currentTimeMillis();
            }
        });
        Log.d("permdebug","asking");

        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.M)
            requestPermissions(new String[]{"android.permission.FAKE_PACKAGE_SIGNATURE"}, 200);
        findViewById(R.id.click_handler).setOnLongClickListener(new View.OnLongClickListener() {
            private long lastLongclick;
            private int click=0;

            @Override
            public boolean onLongClick(View v) {
                if(click < 3)
                Toast.makeText(MainActivityOld.this, "Encore "+(2-click)+" clics", Toast.LENGTH_SHORT).show();
                if(System.currentTimeMillis()-lastLongclick<2000){
                    if(click == 2)
                    startOtherLauncher(MainActivityOld.this);

                }
                lastLongclick = System.currentTimeMillis();
                click = (click+1)%3;
                return true;
            }

          
        });
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, "android.permission.FAKE_PACKAGE_SIGNATURE") != PackageManager.PERMISSION_GRANTED) {
            Log.d("permdebug","not granted");
        }else
            Log.d("permdebug","granted");

    }
    public void onResume(){
        super.onResume();
        mWebViewWrapper.onResume();
        isPaused = false;
        if(FloatingService.sService != null)
           FloatingService.sService.hideBubble();
       hideSystemUI();
       DownloadIntentService.startActionDownload(this);
    }

    public void onStop(){
        super.onStop();
        try {
            unbindService(mConn);
        }catch(IllegalArgumentException e){

        }
    }

    public static void staticReload(){
        sActivity.reload();
    }

    private void reload() {
        mWebViewWrapper.loadUri("https://ovh2.phie.ovh/mamie/?psw="+mPassword+"&b="+Math.random()+addUrl);
    }

    public static void startOtherLauncher(Context ct){
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> pkgAppsList = ct.getPackageManager().queryIntentActivities( mainIntent, 0);
        String packacge = null;
        String activity = null;
        if(pkgAppsList == null)
            return;

        for(ResolveInfo info: pkgAppsList){

            if(info.activityInfo.name != null && !info.activityInfo.name.contains("mamie")){
                packacge= info.activityInfo.packageName;
                activity= info.activityInfo.name;
                break;
            }
        }
        if(packacge == null || activity == null)
            return;
        Intent intent = new Intent();
        intent.setAction(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setComponent(new ComponentName(packacge, activity));
        try {
            ct.startActivity(intent);
        }catch (android.content.ActivityNotFoundException e){
        }
    }

    public void onPause(){
        super.onPause();
        mWebViewWrapper.onPause();
        isPaused = true;
        if(FloatingService.sService != null)

            FloatingService.sService.showBubble();
    }
    public static void restoreAll(final Context ct) {
        Handler mHandler = new Handler();
        int wait = 5000;
        Toast.makeText(ct, "restore",Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.setAction("android.intent.category.LAUNCHER");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setComponent(new ComponentName("com.teamviewer.host.market", "com.teamviewer.host.ui.HostActivity"));
        try {
            ct.startActivity(intent);
        }catch (android.content.ActivityNotFoundException e){
            wait = 0;
        } catch(Exception e){

        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ForegroundDetector.sService != null)
                    ForegroundDetector.sService.performGlobalAction(
                            ForegroundDetector.GLOBAL_ACTION_HOME
                    );
                else {
                    Intent intent = new Intent(ct, MainActivityOld.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    ct.startActivity(intent);
                }

            }
        },wait);
    }


}
