package com.example.launchermamie;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by alexandre on 12/02/16.
 */
public class BubbleLayout extends LinearLayout {

    private final ViewGroup mMainContainer;
    private final boolean mIsLoaded;
    private View mMainView;
    private Handler mHandler = new Handler(){

        public void handleMessage(Message msg) {
            mListener.onRemoveToast();
        }
    };

    private OnBubbleClickListener mListener;

    public View getMainView() {
        return mMainView;
    }


    public interface OnBubbleClickListener{
        public void onMainBubbleClick();
        public void onSecondaryBubbleClick(View v);
        void onRemoveToast();
        void onDisplayToast();
    }

    public BubbleLayout(Context context) {
        this(context, null);
    }


    public BubbleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.bubble_layout, this);

        mMainContainer = (ViewGroup)findViewById(R.id.main_container);

        mIsLoaded = true;


    }

    public void addView(View child, int index, LayoutParams params) {

        if(!mIsLoaded)
            super.addView(child,index,params);
        else{

            if(mMainView==null)
                setMainBubbleView(child);

        }
    }
    public void setMainBubbleView(View v){
        mMainView = v;
        mMainContainer.removeAllViews();
        mMainContainer.addView(v);
    }


    public void setOnBubbleClickListener(OnBubbleClickListener listener){
        mListener= listener;
    }
    public void onMainClick() {
        mListener.onMainBubbleClick();

    }

}
