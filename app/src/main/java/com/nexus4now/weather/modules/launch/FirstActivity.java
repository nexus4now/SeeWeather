package com.nexus4now.weather.modules.launch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.nexus4now.weather.R;
import com.nexus4now.weather.modules.main.ui.MainActivity;

public class FirstActivity extends Activity {
    private static final String TAG = FirstActivity.class.getSimpleName();
    //private SwitchHandler mHandler = new SwitchHandler(Looper.getMainLooper(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //mHandler.sendEmptyMessageDelayed(1, 1000);
        setContentView(R.layout.activity_first);
        //splashcode
        new Handler().postDelayed(new Runnable(){
            public void run (){
                Intent intent = new Intent(FirstActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                FirstActivity.this.finish();
            }
        },2000);

    }

    /**class SwitchHandler extends Handler {
        private WeakReference<FirstActivity> mWeakReference;

        public SwitchHandler(Looper mLooper, FirstActivity activity) {
            super(mLooper);
            mWeakReference = new WeakReference<FirstActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent i = new Intent(FirstActivity.this, MainActivity.class);
            FirstActivity.this.startActivity(i);
            //activity切换的淡入淡出效果
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            FirstActivity.this.finish();
        }
    }*/
}