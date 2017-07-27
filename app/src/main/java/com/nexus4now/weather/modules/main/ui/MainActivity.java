package com.nexus4now.weather.modules.main.ui;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.nexus4now.weather.R;
import com.nexus4now.weather.base.BaseActivity;
import com.nexus4now.weather.base.C;
import com.nexus4now.weather.base.RxBus;
import com.nexus4now.weather.common.PLog;
import com.nexus4now.weather.common.utils.ToastUtil;
import com.nexus4now.weather.common.utils.Util;
import com.nexus4now.weather.component.ImageLoader;
import com.nexus4now.weather.component.RetrofitSingleton;
import com.nexus4now.weather.modules.about.ui.AboutActivity;
import com.nexus4now.weather.modules.city.ui.ChoiceCityActivity;
import com.nexus4now.weather.modules.main.adapter.WeatherAdapter;
import com.nexus4now.weather.modules.main.domain.ChangeCityEvent;
import com.nexus4now.weather.modules.main.domain.Weather;
import com.nexus4now.weather.modules.main.listener.HidingScrollListener;
import com.nexus4now.weather.modules.service.AutoUpdateService;
import com.nexus4now.weather.modules.setting.Setting;
import com.nexus4now.weather.modules.setting.ui.SettingActivity;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Calendar;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

//import com.nexus4now.weather.common.utils.CheckVersion;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
    AMapLocationListener {
    private final String TAG = MainActivity.class.getSimpleName();

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private SwipeRefreshLayout mRefreshLayout;
    private ImageView bannner;
    private ProgressBar mProgressBar;
    private ImageView mErroImageView;
    private RelativeLayout headerBackground;

    private RecyclerView mRecyclerView;
    private Weather mWeather = new Weather();
    private WeatherAdapter mAdapter;
    private Observer<Weather> observer;
    private long exitTime = 0; ////记录第一次点击的时间

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "请选择城市查看天气", Snackbar.LENGTH_LONG)
                        .setAction("选择", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this,ChoiceCityActivity.class);

                                startActivity(intent);
                            }
                        }).show();
            }
        });
        PLog.i("onCreate");
        //ButterKnife.bind(this);
        initView();
        initDrawer();
        initDataObserver();
        initIcon();
        startService(new Intent(this, AutoUpdateService.class));
        //CheckVersion.checkVersion(this, fab);
        // https://github.com/tbruyelle/RxPermissions
        RxPermissions.getInstance(this).request(Manifest.permission.ACCESS_COARSE_LOCATION)
            .subscribe(granted -> {
                if (granted) {
                    location();
                } else {
                    load();
                }
            });
        // bus
        compositeSubscription.add(
            RxBus.getDefault().toObserverable(ChangeCityEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                changeCityEvent -> {
                    mRefreshLayout.setRefreshing(true);
                    load();
                }, throwable -> {
                    PLog.e(throwable.getMessage())
                    ;
                }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        PLog.i("onStart");
        showEggs();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        PLog.i("onRestart");
        //为了实现 Intent 重启使图标生效
        initIcon();
        // 修改 adapter 的初始化
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PLog.i("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        PLog.i("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        PLog.i("onStop");
    }

    /**
     * 初始化基础View
     */
    private void initView() {
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        // http://stackoverflow.com/questions/30655939/programmatically-collapse-or-expand-collapsingtoolbarlayout
        if (appBarLayout != null) {
            //控制是否展开
            appBarLayout.setExpanded(false);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bannner = (ImageView) findViewById(R.id.banner);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mErroImageView = (ImageView) findViewById(R.id.iv_erro);
        // Glide 加载本地 GIF 图的方法
        //GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(mErroImageView);
        //Glide.with(this).load(R.raw.loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageViewTarget);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiprefresh);
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(
                    () -> mRefreshLayout.postDelayed(this::load, 1000));
        }

        //标题
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(" ");
        }

        //fab
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            final int fabBottomMargin = lp.bottomMargin;

            //recclerview
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addOnScrollListener(new HidingScrollListener() {
                @Override
                //fab隐藏动画
                public void onHide() {
                    fab.animate()
                            .translationY(fab.getHeight()+fabBottomMargin)
                            .setInterpolator(new AccelerateInterpolator(2))
                            .start();
                }

                @Override
                public void onShow() {
                    fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                }
            });
            mAdapter = new WeatherAdapter(MainActivity.this, mWeather);
            mRecyclerView.setAdapter(mAdapter);
            //主页内容点击事件
            /**mAdapter.setOnItemClickListener(mWeather1 -> {
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogLayout = inflater.inflate(R.layout.weather_dialog, (ViewGroup) this.findViewById(
                        R.id.weather_dialog_root));
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setView(dialogLayout);
                final AlertDialog alertDialog = builder.create();

                RelativeLayout root = (RelativeLayout) dialogLayout.findViewById(R.id.weather_dialog_root);
                switch (Util.getWeatherType(Integer.parseInt(mWeather1.now.cond.code))) {
                    case "晴":
                        root.setBackgroundResource(R.mipmap.dialog_bg_sunny);
                        break;
                    case "阴":
                        root.setBackgroundResource(R.mipmap.dialog_bg_cloudy);
                        break;
                    case "雨":
                        root.setBackgroundResource(R.mipmap.dialog_bg_rainy);
                        break;
                    default:
                        break;
                }

                TextView city = (TextView) dialogLayout.findViewById(R.id.dialog_city);
                city.setText(mWeather1.basic.city);
                TextView temp = (TextView) dialogLayout.findViewById(R.id.dialog_temp);
                temp.setText(String.format("%s°", mWeather1.now.tmp));
                ImageView icon = (ImageView) dialogLayout.findViewById(R.id.dialog_icon);

                Glide.with(this)
                        .load(mSetting.getInt(mWeather1.now.cond.txt, R.mipmap.none))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                icon.setImageBitmap(resource);
                                icon.setColorFilter(Color.WHITE);
                            }
                        });

                alertDialog.show();
            });*/
        }
    }


    private void showEggs() {
        //彩蛋-夜间模式
        Calendar calendar = Calendar.getInstance();
        mSetting.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        setStatusBarColorForKitkat(R.color.toolbar);
        ImageLoader.load(this, R.mipmap.nav_head_day, bannner);
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.toolbar));
        if (mSetting.getCurrentHour() < 6 || mSetting.getCurrentHour() > 18) {
            ImageLoader.load(this, R.mipmap.nav_head_night, bannner);
            collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.night_toolbar));
            setStatusBarColor(R.color.night_toolbar);
            setStatusBarColorForKitkat(R.color.night_toolbar);
            //headerBackground.setBackground(ContextCompat.getDrawable(this, R.mipmap.sight));
        }
    }

    /**
     * 初始化抽屉
     */
    private void initDrawer() {
        //https://segmentfault.com/a/1190000004151222
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        Calendar calendar = Calendar.getInstance();
        mSetting.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
            ImageView image = (ImageView) headerLayout.findViewById(R.id.image);
        if (mSetting.getCurrentHour() < 6 || mSetting.getCurrentHour() > 18) {
                ImageLoader.load(this, R.mipmap.nav_head_night, image);
        }
            headerBackground = (RelativeLayout) headerLayout.findViewById(R.id.header_background);
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    /**
     * 初始化Icon
     */
    private void initIcon() {

            mSetting.putInt("未知", R.mipmap.none);
            mSetting.putInt("晴", R.mipmap.clear_day_colored);
            mSetting.putInt("阴", R.mipmap.cloudy_colored);
            mSetting.putInt("多云", R.mipmap.partly_cloudy_day_colored);
            //mSetting.putInt("少云", R.mipmap.);
            mSetting.putInt("晴间多云", R.mipmap.partly_cloudy_day_colored);
            mSetting.putInt("小雨", R.mipmap.rain_colored);
            mSetting.putInt("中雨", R.mipmap.rain_colored);
            mSetting.putInt("大雨", R.mipmap.rain_colored);
            mSetting.putInt("阵雨", R.mipmap.thunderstorm_colored);
            mSetting.putInt("雷阵雨", R.mipmap.thunderstorm_colored);
            mSetting.putInt("霾", R.mipmap.fog_colored);
            mSetting.putInt("雾", R.mipmap.fog_colored);

    }

    /**
     * 初始化 observer (观察者)
     * 拿到数据后的操作
     */
    private void initDataObserver() {

        observer = new Observer<Weather>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                PLog.e(e.toString());
                Snackbar.make(fab, "网络异常", Snackbar.LENGTH_INDEFINITE).setAction("重试", v -> {
                    load();
                }).show();
            }

            @Override
            public void onNext(Weather weather) {
                if (mSetting.getAutoUpdate() == 0) {
                    aCache.put(C.WEATHER_CACHE, weather);
                } else {
                    aCache.put(C.WEATHER_CACHE, weather,
                        (mSetting.getAutoUpdate() * Setting.ONE_HOUR));//默认3小时后缓存失效
                }
                mWeather.status = weather.status;
                mWeather.aqi = weather.aqi;
                mWeather.basic = weather.basic;
                mWeather.suggestion = weather.suggestion;
                mWeather.now = weather.now;
                mWeather.dailyForecast = weather.dailyForecast;
                mWeather.hourlyForecast = weather.hourlyForecast;
                collapsingToolbarLayout.setTitle(mWeather.basic.city);
                //mAdapter = new WeatherAdapter(MainActivity.this, weather);
                //mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                normalStyleNotification(mWeather);
                showSnackbar(fab, "更新成功。");
            }
        };
    }

    /**
     * 优化网络+缓存逻辑
     * 优先网络
     */
    private void load() {
        compositeSubscription.add(Observable.concat(fetchDataByNetWork(), fetchDataByCache())
            .first(weather -> weather != null)
            .doOnError(throwable -> {
                mErroImageView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            })
            .doOnNext(weather -> {
                mErroImageView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            })
            .doOnTerminate(() -> {
                mRefreshLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
            })
            .subscribe(observer));
    }

    /**
     * 从本地获取
     */
    private Observable<Weather> fetchDataByCache() {
        return Observable.defer(() -> {
            Weather weather = (Weather) aCache.getAsObject(C.WEATHER_CACHE);
            return Observable.just(weather);
        });
    }

    /**
     * 从网络获取
     */
    private Observable<Weather> fetchDataByNetWork() {
        String cityName = Util.replaceCity(mSetting.getCityName());
        return RetrofitSingleton.getInstance()
            .fetchWeather(cityName)
            .onErrorReturn(throwable -> {
                PLog.e(throwable.getMessage());
                ToastUtil.showLong(throwable.getMessage());
                return null;
            });
    }


    /**private void showFabDialog() {
        new AlertDialog.Builder(MainActivity.this)//setTitle("选择城市")
            .setMessage("请选择所要查看天气的城市")
            .setPositiveButton("选择城市", (dialog, which) -> {
                startActivity(new Intent(MainActivity.this,ChoiceCityActivity.class));
                    })
                Uri uri = Uri.parse(getString(R.string.app_html));   //指定网址
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);           //指定Action
                intent.setData(uri);                            //设置Uri
                MainActivity.this.startActivity(intent);        //启动Activity
            })
            .show();
    }*/

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_set:
                Intent intentSetting = new Intent(MainActivity.this, SettingActivity.class);
                intentSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentSetting);
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;


            /**case R.id.nav_city:
                startActivityForResult(new Intent(this, ChoiceCityActivity.class), 1);
                break;*/
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showSnackbar(fab, "按两次返回键退出。");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    /**
     * 高德定位
     */
    private void location() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔 单位毫秒
        int tempTime = mSetting.getAutoUpdate();
        if (tempTime == 0) {
            tempTime = 100;
        }
        mLocationOption.setInterval(tempTime * Setting.ONE_HOUR);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(this);
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                mSetting.setCityName(aMapLocation.getCity());
            } else {
                showSnackbar(fab, "定位失败,加载默认城市", true);
            }
            load();
        }
    }

    private void normalStyleNotification(Weather weather) {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(MainActivity.this);
        Notification notification = builder.setContentIntent(pendingIntent)
            .setContentTitle(weather.basic.city +String.format("气温: %s℃ ",  weather.now.tmp))
            .setContentText(String.format("%s", weather.now.cond.txt))
            // 这里部分 ROM 无法成功
            .setSmallIcon(mSetting.getInt(weather.now.cond.txt, R.mipmap.none))
            .build();
        notification.flags = mSetting.getNotificationModel();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // tag和id都是可以拿来区分不同的通知的
        manager.notify(1, notification);
    }
}
