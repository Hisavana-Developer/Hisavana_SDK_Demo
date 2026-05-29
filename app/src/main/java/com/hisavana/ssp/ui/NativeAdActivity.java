package com.hisavana.ssp.ui;


import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloud.hisavana.sdk.api.adx.TNative;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.api.view.AdChoicesView;
import com.cloud.hisavana.sdk.api.view.AdCloseView;
import com.cloud.hisavana.sdk.api.view.MediaView;
import com.cloud.hisavana.sdk.api.view.StoreMarkView;
import com.cloud.hisavana.sdk.api.view.TNativeView;
import com.cloud.hisavana.sdk.common.bean.TaNativeInfo;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * @author liuzq
 * @data 2025/10/18
 * ========================================
 * CopyRight (c) 2025 TRANSSION.Co.Ltd.
 * All rights reserved.
 */

public class NativeAdActivity extends BaseActivity implements View.OnClickListener {
    //改变加载广告，布局宽度，微调样式
    private Button loadBtn,  showBtn;
    private TNative tNativeAd;
    private TNativeView nativeView;
    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad);
        setTitle("Native");
        tvADStatus = findViewById(R.id.tvADStatus);
        showAdStatus("Ready to load ads");

        // 创建一个 加载器
        tNativeAd = new TNative(DemoConstants.getNativeSlotId());

        nativeView = findViewById(R.id.native_layout);
        loadBtn = this.findViewById(R.id.load_ad);
        loadBtn.setOnClickListener(this);

        showBtn = this.findViewById(R.id.show_ad);
        showBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放TaNativeInfo资源，"adInfos"是onAdLoaded(List<TaNativeInfo> adInfos)中的列表数据
        for (TaNativeInfo taNativeInfo : mNativeInfos) {
            if(taNativeInfo != null){
                taNativeInfo.destroy();
            }
        }
        mNativeInfos.clear();

        // 容器资源释放
        if (nativeView != null) {
            nativeView.destroy();
        }

        // 广告对象释放
        if (tNativeAd != null) {
            tNativeAd.destroy();
            tNativeAd = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_ad:
                // 加载广告
                loadAd();
                break;
            case R.id.show_ad:
                if (mNativeInfos != null && !mNativeInfos.isEmpty()) {
                    TaNativeInfo nativeInfo = mNativeInfos.get(0);
                    mNativeInfos.add(nativeInfo);
                    inflateView(nativeInfo, tNativeAd, nativeView);
                    showAdStatus("MaterialStyle--" + (nativeInfo).getMaterialStyle());
                }
                break;
        }
    }

    private void loadAd() {
        showAdStatus("Ad loading...");

        loadBtn.setText("Loading Ad");
        tNativeAd.setListener(new TAdListener(this));
        tNativeAd.loadAd();

    }


    // =============================================================================================
    /**
     * Native 广告 里面有多条
     */
    private final  List<TaNativeInfo> mNativeInfos = new ArrayList<>();


    /**
     * 装载 广告View
     *
     * @param adNativeInfo 广告信息
     */
    // 自渲染广告，在此处绑定填充数据
    private void inflateView(TaNativeInfo adNativeInfo, TNative nativeAd, TNativeView nativeView) {
        RelativeLayout adView = (RelativeLayout) LayoutInflater.from(nativeView.getContext()).inflate(R.layout.native_install, null);
        // 绑定icon控件
        ImageView icon = adView.findViewById(R.id.native_ad_icon);
        // 绑定广告主图控件
        MediaView mediaView = adView.findViewById(R.id.coverview);
        // 绑定广告⻆标控件
        AdChoicesView adChoicesView = adView.findViewById(R.id.adChoicesView);
        // 绑定广告关闭控件
        AdCloseView adCloseView = adView.findViewById(R.id.adCloseView);
        // 必选项，绑定应用商店标识控件
        StoreMarkView storeMarkView = adView.findViewById(R.id.store_mark_view);

        // 绑定标题控件
        TextView title = adView.findViewById(R.id.native_ad_title);
        // 绑定描述控件
        TextView des = adView.findViewById(R.id.native_ad_body);
        // 绑定响应动作控件
        Button calltoaction = adView.findViewById(R.id.call_to_action);

        TextView rating = adView.findViewById(R.id.rating);

        nativeView.destroy();

        // 设置icon控件
        nativeView.setIconView(icon);
        // 设置广告主图控件
        nativeView.setMediaView(mediaView, ImageView.ScaleType.FIT_XY);
        // 设置广告⻆标控件
        nativeView.setAdChoiceView(adChoicesView);
        // 设置广告关闭控件
        nativeView.setAdCloseView(adCloseView);
        // 必选项，设置应用商店标识控件
        nativeView.setPsMarkView(storeMarkView);
        // 添加广告视图
        nativeView.addView(adView);

        title.setText(adNativeInfo.getTitle());
        des.setText(adNativeInfo.getDescription());
        calltoaction.setText(adNativeInfo.getCtatext());

        if (!TextUtils.isEmpty(adNativeInfo.getRating())) {
            rating.setText("R : " + adNativeInfo.getRating());
        }
        List<View> adContains = new ArrayList<>();
        adContains.add(title);
        adContains.add(icon);
        adContains.add(mediaView);
        adContains.add(calltoaction);
        adContains.add(des);
        adContains.add(adView);
        adContains.add(adCloseView);
        adContains.add(storeMarkView);
        nativeAd.registerViews(nativeView, adContains, adNativeInfo);
    }

    private void setButtonsVisibility(Boolean shouldShowButtons) {
        int orientation = getResources().getConfiguration().orientation;
        // 仅在横屏时隐藏按钮
        if (orientation != Configuration.ORIENTATION_LANDSCAPE) { return;}
        int visibilityState = shouldShowButtons ? View.VISIBLE : View.GONE;
        loadBtn.setVisibility(visibilityState);
        showBtn.setVisibility(visibilityState);
    }

    // =============================================================================================


    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    private static class TAdListener extends AdListener {
        WeakReference<NativeAdActivity> weakReference;

        TAdListener(NativeAdActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        // 加载完成回调（适用的广告位：Native/Icon）
        @Override
        public void onAdLoaded(List<TaNativeInfo> adInfos) {
            // Request success
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().mNativeInfos.clear();
            weakReference.get().mNativeInfos.addAll(adInfos);
            weakReference.get().loadBtn.setText("Load native");
            weakReference.get().showAdStatus("get success");
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
            // Request failed
            if (weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("Ad failed to load Reason for failure: " + errorCode.getErrorMessage());

            weakReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    weakReference.get().loadBtn.setText("Load native");
                }
            });
            CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "NativeAdActivity --> onError");
        }

        // 点击回调（适用的广告位：Native/Icon）
        @Override
        public void onNativeAdClick(TaNativeInfo taNativeInfo)  {
            // Called when a native ad is clicked
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onNativeAdClick");
        }

        // 展示回调（适用的广告位：Native/Icon）
        @Override
        public void onNativeAdShow(TaNativeInfo taNativeInfo) {
            // Called when a native ad is displayed
            NativeAdActivity activity = weakReference.get();
            if (activity != null) {
                weakReference.get().setButtonsVisibility(false);
            }
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onNativeAdShow");
        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onTimeOut");
        }

        // 关闭回调（适用的广告位：Native/Icon）
        @Override
        public void onAdClosed(TaNativeInfo taNativeInfo) {
            // Called when a native ad close
            NativeAdActivity activity = weakReference.get();
            if (activity != null){
                // 适当时机释放
                // 广告关闭后，假如不再继续使用，建议您及时释放视图和广告对象，避免内存泄漏。
                if(activity.nativeView != null){
                    activity.nativeView.destroy();
                    taNativeInfo.destroy();
                }
                weakReference.get().setButtonsVisibility(true);
            }
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onAdClosed");
        }
    }
}
