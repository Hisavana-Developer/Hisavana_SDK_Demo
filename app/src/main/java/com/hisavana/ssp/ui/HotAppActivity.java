package com.hisavana.ssp.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloud.hisavana.sdk.api.adx.TNative;
import com.cloud.hisavana.sdk.api.listener.AdListener;
import com.cloud.hisavana.sdk.api.view.TNativeView;
import com.cloud.hisavana.sdk.common.bean.TaNativeInfo;
import com.cloud.hisavana.sdk.common.constant.TaErrorCode;
import com.cloud.sdk.commonutil.util.CommonLogUtil;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;
import com.transsion.core.utils.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class HotAppActivity extends BaseActivity implements View.OnClickListener {
    private Button loadBtn, show_ad;
    private TNative tNativeAd;
    private RecyclerView nativeRv;
    private LinearLayoutManager linearLayoutManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    private HotAdapter hotAdapter;
    private List<TaNativeInfo> nativeInfos = new ArrayList<>();
    private String sceneToken;

    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_app);
        tvADStatus = findViewById(R.id.tvADStatus);
        showAdStatus("Ready to load ad");

        nativeRv = findViewById(R.id.rv_native);
        linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        nativeRv.setLayoutManager(linearLayoutManager);
        hotAdapter = new HotAdapter();
        nativeRv.setAdapter(hotAdapter);
        loadBtn = this.findViewById(R.id.load_ad);
        loadBtn.setOnClickListener(this);
        show_ad = this.findViewById(R.id.show_ad);
        show_ad.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        if (tNativeAd != null) {
            tNativeAd.destroy();
            tNativeAd = null;
        }
        nativeInfoRelease();
        //    nativeView = null;
        super.onDestroy();
    }

    private void nativeInfoRelease(){
        for (TaNativeInfo nativeInfo : nativeInfos) {
            if(nativeInfo != null){
                nativeInfo.destroy();
            }
        }
        nativeInfos.clear();
    }
    private void removeView(View v) {
        if (v == null) return;
        ViewParent viewGroup = v.getParent();
        if (viewGroup != null && viewGroup instanceof ViewGroup) {
            ((ViewGroup) viewGroup).removeView(v);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_ad:
                loadAd();
                break;
            case R.id.show_ad:
                hotAdapter.setNativeInfos(nativeInfos);
                break;
        }
    }

    private void loadAd() {
        showAdStatus("Ad loading...");
        tNativeAd = new TNative(DemoConstants.getIconSlotId());

        loadBtn.setText("Loading Ad");
        tNativeAd.setListener(new TAdListener(this));
        tNativeAd.loadAd();
    }

    // =============================================================================================



    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    private static class TAdListener extends AdListener {
        WeakReference<HotAppActivity> weakReference;

        TAdListener(HotAppActivity activity) {
            this.weakReference = new WeakReference(activity);
        }

        // 加载完成回调（适用的广告位：Native/Icon）
        @Override
        public void onAdLoaded(List<TaNativeInfo> adInfos) {
            if (weakReference.get() == null) {
                return;
            }

            weakReference.get().loadBtn.setText("Load native");
            weakReference.get().showAdStatus("get success");
            weakReference.get().nativeInfoRelease();
            weakReference.get().hotAdapter.clear();

            weakReference.get().nativeInfos.addAll(adInfos);
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
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

        // 展示回调（适用的广告位：Native/Icon）
        @Override
        public void onNativeAdShow(TaNativeInfo taNativeInfo) {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onNativeAdShow");
        }

        // 点击回调（适用的广告位：Native/Icon）
        @Override
        public void onNativeAdClick(TaNativeInfo taNativeInfo)  {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onNativeAdClick");
        }

        // 关闭回调（适用的广告位：Native/Icon）
        @Override
        public void onAdClosed(TaNativeInfo taNativeInfo) {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onClosed");
        }

        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdActivity --> onTimeOut");
        }
    }

    public class HotAdapter extends RecyclerView.Adapter{

        private List<TaNativeInfo> nativeInfos = new ArrayList();

        public void setNativeInfos(List<TaNativeInfo> nativeInfos) {
            if(nativeInfos!=null){
                Iterator<TaNativeInfo> iterator = nativeInfos.iterator();
                while(iterator.hasNext()){
                    TaNativeInfo next = iterator.next();
                    if(tNativeAd.isReady(next)){
                        iterator.remove();
                    }
                }
                this.nativeInfos = nativeInfos;
                if(nativeInfos.isEmpty()){
                    ToastUtil.showLongToast("Ad isEmpty");
                }
                notifyDataSetChanged();
            }
        }

        public void clear(){
           if(nativeInfos != null){
               nativeInfos.clear();
           }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_hot_layout, viewGroup, false);
            HotHolder hotHolder = new HotHolder(itemView);
            return hotHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            inflateView(nativeInfos.get(i), tNativeAd, ((HotHolder)viewHolder).native_layout);
        }

        // 自渲染广告，在此处绑定填充数据
        private void inflateView(TaNativeInfo adNativeInfo, TNative nativeAd, TNativeView nativeView) {
            LinearLayout adView = (LinearLayout) LayoutInflater.from(nativeView.getContext()).inflate(R.layout.native_hotapp_install, null);
            // 绑定icon控件
            ImageView icon = adView.findViewById(R.id.native_ad_icon);
            // 绑定标题控件
            TextView title = adView.findViewById(R.id.native_ad_title);

            nativeView.destroy();
            // 设置icon控件
            nativeView.setIconView(icon);
            // 添加广告视图
            nativeView.addView(adView);

            title.setText(adNativeInfo.getTitle());

            List<View> adContains = new ArrayList<>();
            adContains.add(title);

            nativeAd.registerViews(nativeView, adContains, adNativeInfo);
        }

        @Override
        public int getItemCount() {
            return nativeInfos == null?0:nativeInfos.size();
        }
    }

    public class HotHolder extends RecyclerView.ViewHolder{

        public TNativeView native_layout;
        public HotHolder(@NonNull View itemView) {
            super(itemView);
            native_layout = itemView.findViewById(R.id.native_layout);
        }
    }
}


