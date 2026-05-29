package com.hisavana.ssp.ui;

import static com.hisavana.ssp.util.DemoConstants.LOG_TAG;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cloud.hisavana.sdk.api.adx.TBannerView;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;

import java.lang.ref.WeakReference;

/**
 * @author: peng.sun
 * @date: 2018/7/11 10:41
 * ==================================
 * Copyright (c) 2018 TRANSSION.Co.Ltd.
 * All rights reserved.
 */
public class BannerAdActivity extends BaseActivity {

    private TBannerView adview;
    private Button loadBtn;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout mLlBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        tvADStatus = findViewById(R.id.tvADStatus);
        mLlBanner = findViewById(R.id.ll_banner);
        loadBtn = findViewById(R.id.load_banner);
        showAdStatus("Ready to load ads");
    }

    public void loadBannerAd(View view) {
        if (adview == null) {
            adview = new TBannerView(this, DemoConstants.getBannerSlotId());
            Log.d(LOG_TAG, "adview == " + adview.hashCode());
            adview.setListener(new TAdListener(this));

            mLlBanner.addView(adview);
        }
        CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "BannerAdActivity --> BannerAdActivity --> start load Banner");
        loadBtn.setText("Loading Banner");
        showAdStatus("ad loading...");
        adview.loadAd();
    }

    public void showBannerAd(View view) {
        if (adview != null && adview.isReady()) {
            adview.show();
        }
    }

    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    private static class TAdListener extends AdListener {
        WeakReference<BannerAdActivity> weakReference;

        TAdListener(BannerAdActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
            // Request failed
            if (weakReference.get() == null || null == errorCode) {
                return;
            }
            weakReference.get().showAdStatus("Ad failed to load Reason for failure: " + errorCode.getErrorMessage());

            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().loadBtn.setText("Load Banner");
                }
            });
            CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "BannerAdActivity --> onError errorCode=" + errorCode.getErrorMessage());
        }

        // 加载完成回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdLoaded() {
            // Request success
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "BannerAdActivity --> onLoad");
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("get success");

            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().loadBtn.setText("Load Banner");
                }
            });
        }

        // 点击回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdClicked() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "BannerAdActivity --> onClicked");
            weakReference.get().showAdStatus("clicked on the ad");
        }

        // 展示回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdShow() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "BannerAdActivity --> onShow");
            weakReference.get().showAdStatus("Ad display");
        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "BannerAdActivity --> onTimeOut");
        }

        // 关闭回调（适用的广告位：Banner）
        @Override
        public void onAdClosed(TBannerView bannerView) {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "BannerAdActivity --> onClosed");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adview != null) {
            adview.destroy();
            removeView(adview);
            adview = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    // 移除广告视图
    private void removeView(View v) {
        if (v == null) return;
        ViewParent viewGroup = v.getParent();
        if (viewGroup instanceof ViewGroup) {
            ((ViewGroup) viewGroup).removeView(v);
        }
    }
}
