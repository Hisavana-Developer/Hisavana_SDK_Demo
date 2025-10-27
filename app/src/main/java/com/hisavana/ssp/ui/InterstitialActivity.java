package com.hisavana.ssp.ui;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.cloud.hisavana.sdk.api.adx.TInterstitial;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;
import com.transsion.core.utils.ToastUtil;

import java.lang.ref.WeakReference;

public class InterstitialActivity extends BaseActivity implements View.OnClickListener {

    private TInterstitial mTInterstitialAd;
    private Button loadBtn;
    private Button showButton;
    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean ifSaveInstanceState = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);
        setTitle("Interstitial");
        tvADStatus=findViewById(R.id.tvADStatus);
        showAdStatus("Ready to load ads");

        if (mTInterstitialAd == null) {
            mTInterstitialAd = new TInterstitial(DemoConstants.getInterstitialSlotId());
        }
        loadBtn = findViewById(R.id.btn_load_interstitial);
        showButton = findViewById(R.id.tAdInterstitial_show);
        showButton.setOnClickListener(this);
        loadBtn.setOnClickListener(this);
        ifSaveInstanceState = false;
    }

    private void creatRequest() {
        TAdListener tAdListener = new TAdListener(this);
        mTInterstitialAd.setListener(tAdListener);
    }

    private void loadAd(){
        showAdStatus("Ad loading...");
        // 加载广告后在设置的等待时间内将最优广告回调返回
        creatRequest();
        mTInterstitialAd.loadAd();
        loadBtn.setText("Loading Interstitial");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tAdInterstitial_show:
                if (mTInterstitialAd != null && mTInterstitialAd.isReady()){
                    mTInterstitialAd.show();
                } else {
                    ToastUtil.showLongToast("Ad expired");
                }
                break;
            case R.id.btn_load_interstitial:
                // 加载广告
                loadAd();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //避免横竖屏切换导致广告销毁
        if (null != outState) {
            ifSaveInstanceState = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出广告场景后，请销毁该广告对象。
        if (mTInterstitialAd != null && !ifSaveInstanceState) {
            mTInterstitialAd.destroy();
            mTInterstitialAd = null;
        }
        handler.removeCallbacksAndMessages(null);
    }


    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    private static class TAdListener extends AdListener {
        WeakReference<InterstitialActivity> weakReference;

        TAdListener(InterstitialActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        // 加载完成回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdLoaded() {
            // Request success
            if (weakReference.get() == null) {
                return;
            }

            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().loadBtn.setText("Load Interstitial");
                    weakReference.get().showAdStatus("get success");
                }
            });
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW,"InterstitialActivity --> onAdLoaded");
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
            // Request failed
            if (weakReference.get() == null) {
                return;
            }

            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().loadBtn.setText("Load Interstitial");
                    weakReference.get().showAdStatus("Ad failed to load Reason for failure: "+errorCode.getErrorMessage());
                }
            });
            CommonLogUtil.Log().d(DemoConstants.AD_FLOW,"InterstitialActivity --> onError");
        }

        // 展示回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdShow() {
            // Called when an ad is displayed
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().showAdStatus("Ad display");
                }
            });

            CommonLogUtil.Log().i(DemoConstants.AD_FLOW,"InterstitialActivity --> onAdShow");
        }

        // 点击回调（适用的广告位：Splash、Interstitial、Banner、Reward）
        @Override
        public void onAdClicked() {

            // Called when an ad is clicked
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW,"InterstitialActivity --> onAdClicked");
            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().showAdStatus("clicking on the ad");
                }
            });
        }

        // 关闭回调（适用的广告位：Splash、Interstitial、Reward）
        @Override
        public void onAdClosed() {
            // Called when an ad close
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW,"InterstitialActivity --> onAdClosed");
            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().showAdStatus("Ad close");
                }
            });

        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW,"InterstitialActivity --> onTimeOut");
            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().showAdStatus("Ad TimeOut");
                }
            });
        }
    }
}
