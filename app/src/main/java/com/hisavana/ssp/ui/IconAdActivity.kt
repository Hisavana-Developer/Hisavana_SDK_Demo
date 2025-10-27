package com.hisavana.ssp.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cloud.hisavana.sdk.api.adx.TNative
import com.cloud.hisavana.sdk.api.listener.AdListener
import com.cloud.hisavana.sdk.api.view.TNativeView
import com.cloud.hisavana.sdk.common.bean.TaNativeInfo
import com.cloud.hisavana.sdk.common.constant.TaErrorCode
import com.cloud.sdk.commonutil.util.CommonLogUtil
import com.hisavana.ssp.R
import com.hisavana.ssp.util.DemoConstants
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/**
 * Created  ON 2025/10/19
 * @author :liuzq
 */
class IconAdActivity : BaseActivity(), OnRefreshLoadMoreListener {

    lateinit var smartRefreshLayout: SmartRefreshLayout
    lateinit var iconListView: RecyclerView
    lateinit var mAdapter: IconAdapter
    val data = mutableListOf<DataBean>()
    val adList = mutableListOf<AdRequest>()
    val mHandler = Handler(Looper.getMainLooper())
    var page = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Native Icon");
        setContentView(R.layout.activity_icon)
        smartRefreshLayout = findViewById(R.id.icon_refresh)
        iconListView = findViewById(R.id.icon_list_view)
        iconListView.layoutManager = LinearLayoutManager(this)
        mAdapter = IconAdapter(data)
        iconListView.adapter = mAdapter
        smartRefreshLayout.setOnRefreshLoadMoreListener(this)
        smartRefreshLayout.autoRefresh()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 0

        adList.forEach {
            it.destroy()
        }
        adList.clear()
        data.forEach {
            it.nativeInfo?.destroy()
        }
        data.clear()
        mHandler.postDelayed({
            getItemData()
            smartRefreshLayout.finishRefresh()
        }, 200)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mHandler.postDelayed({
            getItemData()
            mAdapter.notifyDataSetChanged()
            smartRefreshLayout.finishLoadMore()
        }, 200)
    }

    fun getItemData() {
        val position = data.size
        // 媒体数据模拟
        getData(data.size)
        //获取广告数据
        val adRequest = AdRequest(this, data.size)
        val ads = adRequest.loadAd()
        ads.forEach {
            data.add(DataBean(null, null, adRequest.tNativeAd, it, 1))
        }
        val count = data.size - position
        if(position == 0){
            mAdapter.notifyDataSetChanged()
        }else{
            mAdapter.notifyItemRangeInserted(position, count)
        }

    }

    fun getData(startPosition: Int) {
        for (i in startPosition until startPosition + 10) {
            data.add(DataBean("$i ", "normal item", null, null, 0))
        }
    }

  fun setAdData(ads:List<TaNativeInfo>?, tNativeAd: TNative, position: Int){
      if (ads.isNullOrEmpty()){
          return
      }
      val d = mutableListOf<DataBean>()
      ads.forEach {
          d.add(DataBean(null, null, tNativeAd, it, 1))
      }
      if(data.size<=position){
          val start = data.size
          data.addAll(d)
          mAdapter.notifyItemRangeChanged(start,d.size)
      }else{
          data.addAll(position,d)
          mAdapter.notifyItemRangeChanged(position,d.size)
      }
  }


    inner class IconAdapter(data: MutableList<DataBean>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                0 -> NormalHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon_normal, parent, false))
                1 -> AdHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon_ad, parent, false))
                else -> NormalHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon_normal, parent, false))
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (holder) {
                is NormalHolder -> {
                    holder.title.text = data[position].title
                    holder.des.text = data[position].content
                }
                is AdHolder -> {
                    holder.bindAd(data[position])
                }
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemViewType(position: Int): Int {
            if (data.size > position) {
                return data[position].itemType
            }
            return 0
        }

    }

    inner class NormalHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById<TextView>(R.id.title)
        val des: TextView = itemView.findViewById<TextView>(R.id.des)
    }

    inner class AdHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tAdNativeView = itemView.findViewById<TNativeView>(R.id.native_view)

        fun bindAd(ad: DataBean) {
             ad.nativeInfo?.let {
                inflateView(it, ad.tNativeAd, tAdNativeView)
            }
        }

        // 自渲染广告，在此处绑定填充数据
        private fun inflateView(
            adNativeInfo: TaNativeInfo,
            nativeAd: TNative?,
            nativeView: TNativeView
        ) {
            val adView = LayoutInflater.from(nativeView.context)
                .inflate(R.layout.layout_icon_ad, nativeView, false) as ConstraintLayout?
            // 绑定icon控件
            val icon = adView?.findViewById<ImageView?>(R.id.icon)
            // 绑定标题控件
            val title = adView?.findViewById<TextView?>(R.id.title)

            nativeView.destroy()
            // 设置icon控件
            nativeView.setIconView(icon)
            // 添加广告视图
            nativeView.addView(adView)

            title?.text = adNativeInfo.title

            val adContains: MutableList<View?> = ArrayList()
            adContains.add(title)

            nativeAd?.registerViews(nativeView, adContains, adNativeInfo)
        }
    }

    inner class AdRequest(context: Context, position: Int) {
        val tNativeAd by lazy {
            TNative(DemoConstants.getIconSlotId())
        }

        val mNativeAdInfo by lazy {
            ArrayList<TaNativeInfo>()
        }

        // 广告监听器，监听广告的请求超时、加载完成（填充）、展示、点击、异常、关闭动作的回调
        val adListener = object : AdListener() {

            // 加载完成回调（适用的广告位：Native/Icon）
            override fun onAdLoaded(nativeAdInfo: List<TaNativeInfo>?) {

                nativeAdInfo?.let {
                    mNativeAdInfo.clear()
                    mNativeAdInfo.addAll(it)
                }

                setAdData( mNativeAdInfo, tNativeAd, position)
                CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "IconAdActivity --> onAdLoaded")
            }

            // 异常回调（适用的广告位：所有广告位）
            override fun onError(errorCode: TaErrorCode?) {
                CommonLogUtil.Log().d(DemoConstants.AD_FLOW, "IconAdActivity --> onError = ${errorCode?.errorMessage}")
            }

            // 点击回调（适用的广告位：Native/Icon）
            override fun onNativeAdClick(p0: TaNativeInfo?) {
                CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "IconAdActivity --> onNativeAdClick")
            }

            // 展示回调（适用的广告位：Native/Icon）
            override fun onNativeAdShow(p0: TaNativeInfo?) {
                CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "IconAdActivity --> onNativeAdShow")
            }

            // 请求超时回调（适用的广告位：所有广告位）
            override fun onTimeOut() {
                CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "IconAdActivity --> onTimeOut")
            }

            // 关闭回调（适用的广告位：Native/Icon）
            override fun onAdClosed(p0: TaNativeInfo?) {
                CommonLogUtil.Log().i(DemoConstants.AD_FLOW, "IconAdActivity --> onAdClosed")
            }
        }

        init {
            // 设置广告监听器
            tNativeAd.setListener(adListener)
            // 设置是否是请求ICON类型的广告位
            tNativeAd.setLoadIcon(true)
            // 设置请求广告数量
            tNativeAd.setAdCount(4)
        }

        fun loadAd(): MutableList<TaNativeInfo> {

            if (mNativeAdInfo.isEmpty()) {
                // 加载广告
                tNativeAd.loadAd()
            }
            return mNativeAdInfo
        }

        fun destroy() {
            tNativeAd.destroy()
        }
    }


    data class DataBean(
        var title: String?,
        var content: String?,
        var tNativeAd: TNative?,
        var nativeInfo: TaNativeInfo?,
        var itemType: Int = 0
    )
}