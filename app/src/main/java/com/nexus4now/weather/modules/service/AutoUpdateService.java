package com.nexus4now.weather.modules.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.nexus4now.weather.R;
import com.nexus4now.weather.base.C;
import com.nexus4now.weather.common.ACache;
import com.nexus4now.weather.common.utils.Util;
import com.nexus4now.weather.component.RetrofitSingleton;
import com.nexus4now.weather.modules.main.domain.Weather;
import com.nexus4now.weather.modules.main.ui.MainActivity;
import com.nexus4now.weather.modules.setting.Setting;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by HugoXie on 16/4/18.
 *
 * Email: Hugo3641@gamil.com
 * GitHub: https://github.com/xcc3641
 */
public class AutoUpdateService extends Service {

    private final String TAG = AutoUpdateService.class.getSimpleName();
    private Setting mSetting;
    private ACache mAcache;
    // http://blog.csdn.net/lzyzsd/article/details/45033611
    // 在生命周期的某个时刻取消订阅。一个很常见的模式就是使用CompositeSubscription来持有所有的Subscriptions，然后在onDestroy()或者onDestroyView()里取消所有的订阅
    private CompositeSubscription mCompositeSubscription;
    private Subscription mNetSubcription;

    private boolean isUnsubscribed = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSetting = Setting.getInstance();
        mAcache = ACache.get(getApplication());
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (this) {
            unSubscribed();
            if (isUnsubscribed) {
                unSubscribed();
                if (mSetting.getAutoUpdate() != 0) {
                    mNetSubcription = Observable.interval(mSetting.getAutoUpdate(), TimeUnit.HOURS)
                        .subscribe(aLong -> {
                            isUnsubscribed = false;
                            //PLog.i(TAG, SystemClock.elapsedRealtime() + " 当前设置" + mSetting.getAutoUpdate());
                            fetchDataByNetWork();
                        });
                    mCompositeSubscription.add(mNetSubcription);
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void unSubscribed() {
        isUnsubscribed = true;
        mCompositeSubscription.remove(mNetSubcription);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    private void fetchDataByNetWork() {
        String cityName = mSetting.getCityName();
        if (cityName != null) {
            cityName = Util.replaceCity(cityName);
        }
        RetrofitSingleton.getInstance().fetchWeather(cityName)
            .subscribe(weather -> {
                mAcache.put(C.WEATHER_CACHE, weather);
                normalStyleNotification(weather);
            });
    }

    private void normalStyleNotification(Weather weather) {
        Intent intent = new Intent(AutoUpdateService.this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(AutoUpdateService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(AutoUpdateService.this);
        Notification notification = builder.setContentIntent(pendingIntent)
            .setContentTitle(weather.basic.city)
            .setContentText(String.format("%s 实时温度: %s℃ ", weather.now.cond.txt, weather.now.tmp))
            // 这里部分 ROM 无法成功
            .setSmallIcon(mSetting.getInt(weather.now.cond.txt, R.mipmap.none))
            .build();
        notification.flags = mSetting.getNotificationModel();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // tag和id都是可以拿来区分不同的通知的
        manager.notify(1, notification);
    }
}
