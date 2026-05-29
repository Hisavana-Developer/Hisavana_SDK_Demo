package com.hisavana.ssp.ui;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.cloud.sdk.commonutil.widget.TranCircleImageView;
import com.hisavana.ssp.R;
import com.hisavana.ssp.util.DemoConstants;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 列表中的 NativeAd
 */
public class NativeAdInListActivity extends BaseActivity {

    /*** 列表 Item 封装 JavaBean*/
    private final List<ItemBean> list = new ArrayList<>();
    private static final String TAG = "ADSDK_DEMO";
    private static final String NATIVE_TAG = "native_tag";
    private static final int ITEM_TYPE_DEFAULT = 0;
    private static final int ITEM_TYPE_Native = 1;

    private  InnerAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad_in_list);
        setTitle("Native List");
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InnerAdapter(list);
        rv.setAdapter(adapter);

        // 加载默认数据
        for (int i = 0; i < 100; i++) {
            if (i == 0 || i % 10 == 0) {
                list.add(new ItemBean(NATIVE_TAG, null, i, this));
            } else {
                list.add(new ItemBean("native demo item " + i, null, i, this));
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 释放资源
        for (ItemBean itemBean : list) {
            if (null == itemBean) {
                continue;
            }
            if (null != itemBean.nativeInfo) {
                itemBean.nativeInfo.destroy();
            }
            if (itemBean.nativeAd != null) {
                itemBean.nativeAd.destroy();
            }
            if (itemBean.tAdNativeView != null) {
                itemBean.tAdNativeView.destroy();
            }
            itemBean.weakReference = null;
            CommonLogUtil.Log().d(TAG, "资源回收");
        }
        list.clear();
        super.onDestroy();
    }

    private void closeAd(TaNativeInfo tAdNativeInfo){
        int position = -1;
        for (int i = 0; i<list.size(); i++){
            ItemBean item = list.get(i);
            if (item!=null && tAdNativeInfo == item.nativeInfo){
                position = i;
                break;
            }
        }
        if(position>-1){
            list.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    // =============================================================================================


    /**
     * 列表适配器
     */
    static class InnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ItemBean> itemBeanList;

        InnerAdapter(List<ItemBean> list) {
            if (null == list) {
                itemBeanList = new ArrayList<>();
            } else {
                itemBeanList = list;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            if (viewType == 0) {
                View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout, viewGroup, false);
                viewHolder = new VHHolder(itemView);
            } else {
                View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_ad_layout, viewGroup, false);
                viewHolder = new VHADHolder(itemView);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vhHolder, int position) {
            if (getItemViewType(position) == ITEM_TYPE_Native) {
                ((VHADHolder) vhHolder).setData(itemBeanList.get(position));
            } else {
                ((VHHolder) vhHolder).setData(itemBeanList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return itemBeanList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (TextUtils.equals(NATIVE_TAG, itemBeanList.get(position).getTitle())) {
                return ITEM_TYPE_Native;
            } else {
                return ITEM_TYPE_DEFAULT;
            }
        }

        static class VHHolder extends RecyclerView.ViewHolder {

            private final AppCompatButton btn;

            public VHHolder(@NonNull View itemView) {
                super(itemView);
                btn = itemView.findViewById(R.id.btn);
            }

            public void setData(ItemBean itemBean) {
                btn.setText(itemBean.title);
            }
        }

        static class VHADHolder extends RecyclerView.ViewHolder {

            /***Native 广告容器*/
            TNativeView nativeLayout;

            public VHADHolder(@NonNull View itemView) {
                super(itemView);
                nativeLayout = itemView.findViewById(R.id.native_layout);
            }

            public void setData(ItemBean itemBean) {
                itemBean.bindValues(nativeLayout);
            }
        }
    }

    // =============================================================================================

    /**
     * 列表的 JavaBean
     */
    // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
    static class ItemBean extends AdListener implements Serializable {
        private String mSlotId = DemoConstants.getNativeSlotId();
        /***标题*/
        private final String title;
        /***广告信息*/
        private TaNativeInfo nativeInfo;
        /***广告请求对象*/
        private TNative nativeAd;
        /***列表中的位置*/
        private final int position;
        /***AdView*/
        private TNativeView tAdNativeView;
        /***Activity*/
        private WeakReference<NativeAdInListActivity> weakReference;
        public ItemBean(String title, TaNativeInfo nativeInfo, int position, NativeAdInListActivity activity) {
            this.title = title;
            this.nativeInfo = nativeInfo;
            this.position = position;
            this.weakReference = new WeakReference<>(activity);
        }

        public String getTitle() {
            return title;
        }

        public void bindValues(TNativeView tAdNativeView) {
            this.tAdNativeView = tAdNativeView;
            this.tAdNativeView.setTag(position);
            this.tAdNativeView.setVisibility(View.GONE);

            if (nativeAd == null){
                nativeAd = new TNative(mSlotId);
                nativeAd.setListener(this);
            }

            // 加载广告
            if (nativeInfo == null) {
                if (weakReference == null || weakReference.get() == null) {
                    return;
                }
                nativeAd.loadAd();
            } else {
                bindAd();
            }
        }

        private void bindAd() {
            tAdNativeView.setVisibility(View.VISIBLE);
            inflateView(nativeInfo, nativeAd, tAdNativeView);
            CommonLogUtil.Log().d(TAG, "------->bindValues --> bindNativeView(),position == " + position + "/" + nativeInfo.getAdCreateId());
            setAdjustView(nativeInfo, tAdNativeView.findViewById(R.id.coverview));
        }

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

        private void setAdjustView(TaNativeInfo adNativeInfo, MediaView tMediaView) {
            if (adNativeInfo == null || tMediaView == null) {
                return;
            }
            if (tMediaView.getChildCount() == 0) {
                return;
            }
            View childView = tMediaView.getChildAt(0);
            if (childView instanceof MediaView && ((MediaView) childView).getChildCount() > 0) {
                View mainView = ((MediaView) childView).getChildAt(0);
                if (mainView instanceof TranCircleImageView) {
                    ((TranCircleImageView) mainView).setAdjustViewBounds(true);
                }
            }
        }

        // ----------------------------------------------------------------------------------------
        // 加载完成回调（适用的广告位：Native/Icon）
        @Override
        public void onAdLoaded(List<TaNativeInfo> adInfos) {
            CommonLogUtil.Log().i(TAG, "------->NativeAdInListActivity --> onLoad position ==== " + position);

            if (weakReference == null || weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("Get success = " + position);
            if (nativeInfo == null && nativeAd != null) {
                if(adInfos != null && !adInfos.isEmpty()){
                    nativeInfo = adInfos.get(0);
                }
                bindAd();
            }
        }

        // 异常回调（适用的广告位：所有广告位）
        @Override
        public void onError(TaErrorCode errorCode) {
            CommonLogUtil.Log().d(TAG, "NativeAdInListActivity --> onError position = " + position);
            if (weakReference == null || weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("i = " + position + " ,Ad failed to load Reason for failure:" + errorCode.getErrorMessage());
        }

        // 展示回调（适用的广告位：Native/Icon）
        @Override
        public void onNativeAdShow(TaNativeInfo taNativeInfo) {
            CommonLogUtil.Log().i(TAG, "NativeAdInListActivity --> show position = " + position);
            if (weakReference == null || weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("Ad display,  i = " + position);
        }

        // 点击回调（适用的广告位：Native/Icon）
        @Override
        public void onNativeAdClick(TaNativeInfo taNativeInfo)  {
            CommonLogUtil.Log().i(TAG, "NativeAdInListActivity --> onClicked position = " + position);
            if (weakReference == null || weakReference.get() == null) {
                return;
            }
            weakReference.get().showAdStatus("Clicking on the ad, i = " + position);
        }

        // 关闭回调（适用的广告位：Native/Icon）
        @Override
        public void onAdClosed(TaNativeInfo taNativeInfo) {
            if (weakReference == null || (weakReference.get() == null) || taNativeInfo == null) {
                return;
            }
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdInListActivity --> onClosed");
            weakReference.get().showAdStatus("Ad close");
        }

        // 请求超时回调（适用的广告位：所有广告位）
        @Override
        public void onTimeOut() {
            CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "NativeAdInListActivity --> onTimeOut");
        }
    }

}
