package com.hisavana.ssp.rewarded;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.cloud.hisavana.sdk.api.adx.TRewarded;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.ui.BaseActivity;
import com.hisavana.ssp.util.DemoConstants;

import java.lang.ref.WeakReference;

/**
 * 聚合 激励视频
 */
public class ADMediationRewardedAdActivity extends BaseActivity {

    /**
     * 记载激励广告的
     */
    private TRewarded tRewardedAd;
    private boolean loading = false;
    private boolean showing = false;
    private Button load;
    private Button show;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Rewarded Ad");
        setContentView(R.layout.activity_admediation_rewarded);

        tvADStatus = findViewById(R.id.tvADStatus);
        load = findViewById(R.id.load);
        show = findViewById(R.id.show);
    }

    @Override
    protected void onDestroy() {
        if (null != tRewardedAd) {
            tRewardedAd.destroy();
            tRewardedAd = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        super.onDestroy();
    }

    /**
     * 加载广告
     */
    public void onLoadClick(View view) {
        loadAd();
    }

    private void loadAd(){
        if (!loading) {
            loading = true;
            tRewardedAd = new TRewarded(DemoConstants.getRewardedSlotId());
            tRewardedAd.setAdListener(new TAdListener(this));
            load.setTextColor(Color.GRAY);
            showAdStatus("广告加载中...");
            tRewardedAd.loadAd();
        }
    }

    /**
     * 显示广告
     */
    public void onShowClick(View view) {
        if (!showing && loading) {
            showing = true;
            if (tRewardedAd != null && tRewardedAd.isReady()) {
                tRewardedAd.show();
                startDelayTimer();
            }
        }
    }

    private void startDelayTimer() {
        countDownTimer = new CountDownTimer(25 * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                load.setText(millisUntilFinished / 1000 + "s后可再次点击");
                load.setTextColor(Color.GRAY);
                show.setText(millisUntilFinished / 1000 + "s后可再次点击");
                show.setTextColor(Color.GRAY);
            }

            @Override
            public void onFinish() {
                load.setText("LoadAd");
                load.setTextColor(Color.BLACK);
                show.setText("Show");
                show.setTextColor(Color.BLACK);
                showing = false;
                loading = false;
            }
        };
        countDownTimer.start();
    }

    // =============================================================================================

    /**
     * 广告会回调
     */
    private static class TAdListener extends AdListener {

        WeakReference<ADMediationRewardedAdActivity> weakReference;

        TAdListener(ADMediationRewardedAdActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        // 加载完成回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdLoaded() {
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("get ad success ");
            CommonLogUtil.Log().i(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> onAdLoaded");
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().loading = false;
            weakReference.get().load.setTextColor(Color.BLACK);
            weakReference.get().showAdStatus("广告加载失败 失败的原因：" + errorCode.getErrorMessage() + "，errorcode：" + errorCode.getErrorCode());
            CommonLogUtil.Log().d(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> onError");
        }

        // 展示回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdShow() {
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("广告展示");
            CommonLogUtil.Log().i(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> onAdShow");
        }

        // 点击回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdClicked() {
            if (weakReference.get() == null) {
                return;
            }
            CommonLogUtil.Log().i(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> onClicked");
            weakReference.get().showAdStatus("点击了广告");
        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            if (weakReference.get() == null) {
                return;
            }
            CommonLogUtil.Log().i(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> onTimeOut");
            weakReference.get().showAdStatus("广告超时");
        }

        // 关闭回调（适用的广告位：Splash、Interstitial、Reward）
        @Override
        public void onAdClosed() {
            if (weakReference.get() == null) {
                return;
            }
            CommonLogUtil.Log().i(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> onAdClosed");
            weakReference.get().showAdStatus("广告关闭");
        }

        @Override
        public void onRewarded() {
            super.onRewarded();
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("获取激励奖励");
            CommonLogUtil.Log().i(DemoConstants.VIDEO_TAG, "ADMediationVideoActivity --> 获取激励奖励");
        }
    }
}
