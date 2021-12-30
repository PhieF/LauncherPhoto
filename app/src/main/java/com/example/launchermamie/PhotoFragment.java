package com.example.launchermamie;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.List;

public class PhotoFragment extends Fragment {

    private WebView mWebView;
    private boolean isPaused;
    private long  lastclick = 0;
    private String mPassword;

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main,container, false);
    }

    @Override
    public void onViewCreated(View v,
                             @Nullable Bundle savedInstanceState) {
        mPassword = getString(R.string.password);

        mWebView = new WebView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        ((FrameLayout)v.findViewById(R.id.click_container)).addView(mWebView,0, params);
        mWebView.requestFocus();
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        //mWebView.getSettings().setSupportZoom(false);
        mWebView.setWebViewClient(mClient);
        mWebView.setWebChromeClient(new WebChromeClient() {

        });
        mWebView.loadUrl("https://ovh2.phie.ovh/mamie/?psw="+mPassword);
        v.findViewById(R.id.click_handler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()-lastclick<5000){
                    MainActivity.restoreAll(getActivity());

                }
                lastclick = System.currentTimeMillis();
            }
        });

        v.findViewById(R.id.click_handler).setOnLongClickListener(new View.OnLongClickListener() {
            private long lastLongclick;
            private int click=0;

            @Override
            public boolean onLongClick(View v) {
                if(click < 2)
                    Toast.makeText(getContext(), "Encore "+(2-click)+" clics", Toast.LENGTH_SHORT).show();
                if(System.currentTimeMillis()-lastLongclick<2000){
                    if(click == 2)
                        startOtherLauncher(getActivity());

                }
                lastLongclick = System.currentTimeMillis();
                click = (click+1)%3;
                return true;
            }


        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void onResume(){
        super.onResume();
        isPaused = false;
        if(FloatingService.sService != null)
           FloatingService.sService.hideBubble();
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
            if(!info.activityInfo.name.contains("mamie")){
                packacge= info.activityInfo.packageName;
                activity= info.activityInfo.targetActivity;
                break;
            }
            Log.d("launcherdebug","name "+info.activityInfo.name);
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
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ForegroundDetector.sService != null)
                    ForegroundDetector.sService.performGlobalAction(
                            ForegroundDetector.GLOBAL_ACTION_HOME
                    );
                else {
                    Intent intent = new Intent(ct, PhotoFragment.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    ct.startActivity(intent);
                }

            }
        },wait);
    }


    WebViewClient mClient = new WebViewClient() {
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            handler.cancel();
        }

        /*
         **  Manage if the url should be load or not, and get the result of the request
         **
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            return true;
        }


        /*
         **  Catch the error if an error occurs
         **
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

        }


        /*
         **  Display a dialog when the page start
         **
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }


        /*
         **  Remove the dialog when the page finish loading
         **
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


        }
    };
}
