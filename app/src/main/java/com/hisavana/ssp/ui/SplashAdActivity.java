package com.hisavana.ssp.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.cloud.hisavana.sdk.api.adx.TSplashView;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.api.listener.OnSkipListener;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;
import com.transsion.core.utils.ToastUtil;

import java.lang.ref.WeakReference;

/**
 * 开屏广告Demo
 */
public class SplashAdActivity extends BaseActivity implements View.OnClickListener{

    /**
     * 开屏广告 View
     */
    private TSplashView tSplashView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTitle("Splash");
        setContentView(R.layout.splash_ad);
        super.onCreate(savedInstanceState);
        tvADStatus = findViewById(R.id.tvADStatus);
        tSplashView = findViewById(R.id.splash_ad);
        showAdStatus("Ready to load ads");
    }

    @Override
    protected void initListener() {
        super.initListener();
        findViewById(R.id.load_ad_btn).setOnClickListener(this);
        findViewById(R.id.show_ad_btn).setOnClickListener(this);
    }

    private void goToHome() {
        CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "SplashAdActivity --> go to home");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注意及时释放 destroy 否则容易发生内存泄漏
        if (tSplashView != null) {
            tSplashView.destroy();
            tSplashView = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.load_ad_btn:
                showAdStatus("Ad loading...");
                if (tSplashView == null) return;

                // 设置广告位ID，其中"splash_id"是splash广告位ID
                tSplashView.setPlacementId(DemoConstants.getSplashSlotId());
                // 设置广告监听器
                tSplashView.setListener(new TAdListener(this));
                // 监听跳过动作
                tSplashView.setSkipListener(new TOnSkipListener());
                // 加载广告
                tSplashView.loadAd();
                break;
            case R.id.show_ad_btn:
                if (tSplashView != null && tSplashView.isReady()) {
                    tSplashView.show();
                } else {
                    ToastUtil.showLongToast("Ad has expired");
                }
                CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "SplashAdActivity --> showAd:");
                break;
        }
    }

    // 监听跳过动作
    private class TOnSkipListener implements OnSkipListener {
        @Override
        public void onClick() {
            goToHome();
            SplashAdActivity.this.finish();
            ToastUtil.showLongToast("skip button click");
        }

        @Override
        public void onTimeEnd() {
            goToHome();
            ToastUtil.showLongToast("time reach");
        }
    }

    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    private static class TAdListener extends AdListener {
        WeakReference<SplashAdActivity> weakReference;

        TAdListener(SplashAdActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        // 加载完成回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdLoaded() {
            if (null == weakReference.get()) {
                return;
            }
            weakReference.get().showAdStatus("Get success");
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
            // Request failed
            CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "SplashAdActivity --> errorCode:" + errorCode.toString());
            weakReference.get().showAdStatus("Ad failed to load Reason for failure: " + errorCode.getErrorMessage());
        }

        // 展示回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdShow() {
            // Called when an ad is displayed
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "SplashAdActivity --> showAd:");
            weakReference.get().showAdStatus("Ad display");
        }

        // 点击回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdClicked() {
            // Called when an ad is clicked
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "SplashAdActivity --> ad click");
            weakReference.get().showAdStatus("Clicking on the ad");
        }

        // 关闭回调（适用的广告位：Splash、Interstitial、Reward）
        @Override
        public void onAdClosed() {
            // Called when an ad close
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "SplashAdActivity --> ad close");
            weakReference.get().showAdStatus("Ad close");
            weakReference.get().goToHome();
            // 注意： 请选择合适的时机调用tSplashAd destroy 方法 否则容易产生内存泄漏
        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "SplashAdActivity --> ad onTimeOut");
            weakReference.get().showAdStatus("Ad TimeOut");
            weakReference.get().goToHome();
        }
    }
}
