package com.hisavana.ssp.ui;

import static com.hisavana.ssp.util.DemoConstants.LOG_TAG;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.cloud.hisavana.sdk.api.adx.TSplashView;
import com.cloud.hisavana.sdk.api.config.AdManager;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.api.listener.OnSkipListener;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.hisavana.sdk.config.AdxServerConfig;
import com.cloud.sdk.commonutil.control.AdxPreferencesHelper;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;

import java.lang.ref.WeakReference;

/**
 * DemoSplashActivity
 * 启动后展示的 Splash 的 Activity, 广告 SDK 在此类初始化。
 *
 * Created  ON 2023/7/3
 *
 * @author :fangxuhui
 */
public class DemoSplashActivity extends AppCompatActivity implements PrivacyAgreementDialog.OnClickListener {

    private Handler delayHandler = new Handler(Looper.getMainLooper());
    private boolean isShowAd;

    private TSplashView tSplashView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_splash);
        tSplashView = findViewById(R.id.splash_ad);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(lp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController windowInsetsController = getWindow().getInsetsController();
            if (windowInsetsController != null) {
                windowInsetsController.hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        splashScreen.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
            @Override
            public boolean shouldKeepOnScreen() {
                return false;
            }
        });


        boolean aBoolean = AdxPreferencesHelper.getInstance().getBoolean(DemoConstants.USER_AGREE_PRIVACY);
        if (aBoolean) {
            initAd();
            loadAd();
            delayHandler.postDelayed(mRunnable,3000);
        } else {
            Log.d(LOG_TAG, "展示弹窗");
            PrivacyAgreementDialog dialog = new PrivacyAgreementDialog(this);
            dialog.show(getSupportFragmentManager(), "privacy");
        }
    }

    // TODO Init AdManger
    private void initAd() {
        AdManager.init(this,new AdManager.AdConfigBuilder()
                // 必须设置，appId用于标识apk身份，只有被识别的身份才会有广告资源返回。
                .setAppId(DemoConstants.getAppId())
                // 可选项，假如开启内置广告功能需要设置，值为zip包的版本号（由运营提供，单位：Long），用于兜底广告的更新
                .setInternalDefaultAdVersion(1761547170965L)
                // 可选项，是否启用图片加载资源竞争优化功能（仅在使用 Glide 图片加载库且代码位是Splash、Interstitial、Reward时生效）。开启设置为true，关闭设置为false，默认关闭
                .setShouldOptimizeImageLoading(true)
                // 可选项，适用于插屏、激励广告新样式在激励完成后toast提示开关；true：提示，false：不提示，默认为true
                .setRewardedCompletionToastEnabled(true)
                // 可选项，是否打印广告日志， 默认为false。 关键字：ADSDK_S、ADSDK_N
                .setDebug(false)
                // 可选项，是否请求测试广告，设置为true时请求到的广告为测试广告， 默认为false。
                .testRequest(false)
                // 可选项，当前是否在跑monkey测试， 默认为false。测试时建议设置为true，正式环境设置为false。
                .setMonkey(false)
                .build());
    }

    private void loadAd() {
        if (tSplashView == null) return;

        // 设置广告位ID，其中"splash_id"是splash广告位ID
        tSplashView.setPlacementId(DemoConstants.getSplashSlotId());
        // 设置广告监听器
        tSplashView.setListener(new TAdListener(this));
        // 监听跳过动作
        tSplashView.setSkipListener(new SkipListener(this));
        // 加载广告
        tSplashView.loadAd();
    }

    private void goToMain(){
        delayHandler.removeCallbacks(mRunnable);
        Intent intent = new Intent(this,DemoMainActivity.class);
        startActivity(intent);
        finishPage();
    }

    private void showAd(){
        if (tSplashView != null && tSplashView.isReady()) {
            tSplashView.show();
        }
    }

    private void finishPage() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    @Override
    public void agree() {
        initAd();
        loadAd();
        delayHandler.postDelayed(mRunnable,3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注意及时释放 destroy 否则容易发生内存泄漏
        if (tSplashView != null) {
            tSplashView.destroy();
            tSplashView = null;
        }
        delayHandler.removeCallbacks(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(!isShowAd){
                goToMain();
            }
        }
    };

    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    public static class TAdListener extends AdListener {

        private boolean isClosed;

        private final WeakReference<DemoSplashActivity>  activityWeakReference;

        public TAdListener(DemoSplashActivity demoSplashActivity){
            activityWeakReference = new WeakReference<>(demoSplashActivity);
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode adError) {
            CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "DemoSplashActivity --> onError = " + adError.getErrorMessage());
            // Request failed
            DemoSplashActivity activity = activityWeakReference.get();
            if(activity != null) {
                activity.goToMain();
            }
        }

        // 加载完成回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdLoaded() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "DemoSplashActivity --> onAdLoaded");
            // Request success
            DemoSplashActivity activity = activityWeakReference.get();
            if(activity != null){
                activity.isShowAd = true;
                activity.showAd();
            }
        }

        // 点击回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdClicked() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "DemoSplashActivity --> onAdClicked");
        }

        // 展示回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdShow() {
            // Called when an ad is displayed
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "DemoSplashActivity --> onAdShow");
        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "DemoSplashActivity --> onTimeOut");
        }

        // 关闭回调（适用的广告位：Splash、Interstitial、Reward）
        @Override
        public void onAdClosed() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "DemoSplashActivity --> onAdClosed");

            // Called when an ad is clicked
            DemoSplashActivity activity = activityWeakReference.get();
            if(activity != null && !isClosed) {
                activity.goToMain();
                isClosed = true;
            }
        }
    }
    // splash 跳过动作监听器
    public  static class SkipListener implements OnSkipListener {
        private WeakReference<DemoSplashActivity>  activityWeakReference;

        public SkipListener(DemoSplashActivity demoSplashActivity){
            activityWeakReference = new WeakReference<>(demoSplashActivity);
        }
        @Override
        public void onClick() {
            DemoSplashActivity activity = activityWeakReference.get();
            if(activity != null) {
                activity.goToMain();
            }
        }

        @Override
        public void onTimeEnd() {
            DemoSplashActivity activity = activityWeakReference.get();
            if(activity != null) {
                activity.goToMain();
            }
        }
    }
}
