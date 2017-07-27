package com.nexus4now.weather.modules.about.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import com.nexus4now.weather.R;
import com.nexus4now.weather.base.BaseActivity;

/**
 * Created by hugo on 2016/2/20 0020.
 */
public class AboutActivity extends BaseActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("关于");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.ic_keyboard_arrow_left_32dpdp));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setStatusBarColor(R.color.toolbar);
        if (mSetting.getCurrentHour()< 6 || mSetting.getCurrentHour() > 18) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.night_toolbar));
            setStatusBarColor(R.color.night_toolbar);
        }
        getFragmentManager().beginTransaction().replace(R.id.framelayout, new AboutFragment()).commit();
    }
}
