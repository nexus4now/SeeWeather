package com.nexus4now.weather.modules.setting.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import com.nexus4now.weather.R;
import com.nexus4now.weather.base.BaseActivity;


public class SettingActivity extends BaseActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("设置");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_32dpdp));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setStatusBarColor(R.color.toolbar);
        if (mSetting.getCurrentHour()< 6 || mSetting.getCurrentHour() > 18) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.night_toolbar));
            setStatusBarColor(R.color.night_toolbar);
        }
        getFragmentManager().beginTransaction().replace(R.id.framelayout, new SettingFragment()).commit();
    }
}
