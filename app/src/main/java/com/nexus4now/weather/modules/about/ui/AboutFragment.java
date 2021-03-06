package com.nexus4now.weather.modules.about.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.nexus4now.weather.R;

/**
 * Created by hugo on 2016/2/20 0020.
 */
public class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    //private AboutActivity mActivity;
    private final String EMAIL = "email";
    private final String SHARE = "share";
    private Preference mShare;
    private Preference mEmail;
    /**private final String INTRODUCTION = "introduction";
    private final String CURRENT_VERSION = "current_version";

    private final String STAR = "Star";
    private final String ENCOURAGE = "encourage";
    private final String BLOG = "blog";
    private final String GITHUB = "github";

    private final String CHECK = "check_version";

    private Preference mIntroduction;
    private Preference mVersion;
    private Preference mCheckVersion;

    private Preference mStar;
    private Preference mEncounrage;
    private Preference mBolg;
    private Preference mGithub;
    */

    //@Override public void onAttach(Context context) {
    //    super.onAttach(context);
    //    mActivity = (AboutActivity) context;
    //}


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        //mIntroduction = findPreference(INTRODUCTION);
        //mVersion = findPreference(CURRENT_VERSION);
        //mCheckVersion = findPreference(CHECK);
        mShare = findPreference(SHARE);
        //mStar = findPreference(STAR);
       // mEncounrage = findPreference(ENCOURAGE);
       // mBolg = findPreference(BLOG);
       // mGithub = findPreference(GITHUB);
        mEmail = findPreference(EMAIL);

        //mIntroduction.setOnPreferenceClickListener(this);
        //mVersion.setOnPreferenceClickListener(this);
        //mCheckVersion.setOnPreferenceClickListener(this);
        mShare.setOnPreferenceClickListener(this);
        //mStar.setOnPreferenceClickListener(this);
        //mEncounrage.setOnPreferenceClickListener(this);
        //mBolg.setOnPreferenceClickListener(this);
        //mGithub.setOnPreferenceClickListener(this);
        mEmail.setOnPreferenceClickListener(this);

        //mVersion.setSummary(getActivity().getString(R.string.version_name) + Util.getVersion(getActivity()));
    }


    @Override public boolean onPreferenceClick(Preference preference) {
        /**if (mVersion == preference) {
            new AlertDialog.Builder(getActivity()).setTitle("就看天气的完成离不开开源项目的支持，向以下致谢：")
                                                  .setMessage("Google Support Design,Gson,Rxjava,RxAndroid,Retrofit," +
                                                          "Glide,systembartint")
                                                  .setPositiveButton("关闭", null)
                                                  .show();
        }
        else*/ if (mShare == preference) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_txt));
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_app)));

        }
        else if(mEmail == preference){
            //copyToClipboard(getView(), mEmail.getSummary().toString());
            click(getView());
        }
        /**else if (mStar == preference) {

            new AlertDialog.Builder(getActivity()).setTitle("点赞")
                                                  .setMessage("去项目地址给作者个Star，鼓励下作者୧(๑•̀⌄•́๑)૭✧")
                                                  .setNegativeButton("复制", (dialog, which) -> {
                                                      //copyToClipboard(getView(), getActivity().getResources()
                                                                                              .getString(
                                                                                                      R.string.app_html));
                                                  })
                                                  .setPositiveButton("打开", (dialog, which) -> {
                                                      Uri uri = Uri.parse(getString(R.string.app_html));   //指定网址
                                                      Intent intent = new Intent();
                                                      intent.setAction(Intent.ACTION_VIEW);           //指定Action
                                                      intent.setData(uri);                            //设置Uri
                                                      getActivity().startActivity(intent);        //启动Activity
                                                  })
                                                  .show();
        }
        else if (mIntroduction == preference) {
            //Uri uri = Uri.parse(getString(R.string.readme));   //指定网址
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);           //指定Action
            //intent.setData(uri);                            //设置Uri
            getActivity().startActivity(intent);        //启动Activity
        }
        else if (mEncounrage == preference) {
            new AlertDialog.Builder(getActivity()).setTitle("请作者喝杯咖啡？(〃ω〃)")
                                                  .setMessage("点击按钮后，作者支付宝账号将会复制到剪切板，" + "你就可以使用支付宝转账给作者啦( •̀ .̫ •́ )✧")
                                                  .setPositiveButton("好叻", (dialog, which) -> {
                                                      /**copyToClipboard(getView(), getActivity().getResources()
                                                                                              .getString(
                                                                                                      R.string.alipay));
                                                  })
                                                  .show();
        }
        else if (mBolg == preference) {
            copyToClipboard(getView(), mBolg.getSummary().toString());
        }
        else if (mGithub == preference) {

            copyToClipboard(getView(), mGithub.getSummary().toString());
        }
        else if (mEmail == preference) {
            copyToClipboard(getView(), mEmail.getSummary().toString());
        }
        else if (mCheckVersion == preference) {
            //Snackbar.make(getView(), "正在检查(σﾟ∀ﾟ)σ", Snackbar.LENGTH_SHORT).show();
            CheckVersion.checkVersion(getActivity(), getView());
        }*/
        return false;
    }


    //复制黏贴板
    /**private void copyToClipboard(View view, String info) {
        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("msg", info);
        manager.setPrimaryClip(clipData);
        Snackbar.make(view, "邮箱地址已复制至剪贴板", Snackbar.LENGTH_SHORT).show();

    }*/
    //连续点击事件
    long[] mHits = new long[3];
    public void click(View view){
        System. arraycopy(mHits, 1, mHits, 0, mHits.length-1);
        mHits[ mHits. length-1] = SystemClock. uptimeMillis();
        if ( mHits[0] >= (SystemClock. uptimeMillis()-500)) {

            Snackbar.make(view, "发现小彩蛋", Snackbar.LENGTH_SHORT).
                    setAction("打开",new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent("android.intent.action.egg");
                            startActivity(intent);
                        }
                    }).show();
        }
    }

}
