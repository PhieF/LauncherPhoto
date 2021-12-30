package com.example.launchermamie;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.InvocationTargetException;

public class WebViewWrapper {
    private static final String TAG = "WebViewWrapper";
    private final Context mContext;
    private WebView mAndroidWebView;
    private View webview;
    private static Object sRuntime;
    private Object mSession;
    private Class mGeckoSessionCl;
    public WebViewWrapper(Context context){
        mContext = context;
        Class c = null;
        try {
            Class geckoViewCl = Class.forName("org.mozilla.geckoview.GeckoView");
            mGeckoSessionCl = Class.forName("org.mozilla.geckoview.GeckoSession");
            Object geckoView = geckoViewCl.getDeclaredConstructor(Context.class).newInstance(context);
            mSession = mGeckoSessionCl.getDeclaredConstructor().newInstance();
            Class runtimeCl = Class.forName("org.mozilla.geckoview.GeckoRuntime");
            if(sRuntime==null) {
                sRuntime = runtimeCl.getMethod("create", Context.class).invoke(null, context);
            }
            mGeckoSessionCl.getMethod("open", runtimeCl).invoke(mSession, sRuntime);
            geckoViewCl.getMethod("setSession", mGeckoSessionCl).invoke(geckoView, mSession);


            webview = (View) geckoView;


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if(webview == null){
            //using android webview
            Log.d(TAG, "Using android webview permdebug");
            mAndroidWebView = new WebView(context);
            mAndroidWebView.setWebViewClient(mClient);
            mAndroidWebView.setWebChromeClient(new WebChromeClient(){
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    return super.onJsAlert(view, url, message, result);
                }
            });
            mAndroidWebView.getSettings().setJavaScriptEnabled(true);
            mAndroidWebView.getSettings().setDomStorageEnabled(true);
            webview = mAndroidWebView;
        }

    }

    public View getView(){
        return webview;
    }

    public void loadUri(String s) {
        if(mAndroidWebView == null) {
            try {
                mGeckoSessionCl.getMethod("loadUri", String.class).invoke(mSession, s);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            mAndroidWebView.loadUrl(s);
        }
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

    public void onResume() {
       getView().setVisibility(View.VISIBLE);

        if(mAndroidWebView != null) {
            mAndroidWebView.loadUrl("javascript:onResume();", null);
            mAndroidWebView.onResume();
        }

    }

    public void onPause() {
       getView().setVisibility(View.GONE);

        if(mAndroidWebView != null) {
            mAndroidWebView.loadUrl("javascript:onPause();", null);

            mAndroidWebView.onPause();

        }
    }
}
