package com.hisavana.ssp.util;

import com.cloud.hisavana.sdk.config.AdxServerConfig;

public class DemoConstants {

    public static final boolean IS_DEBUG =  true;

    public static final String LOG_TAG = "Hisavana_SDK";

    public static final String SDK_INIT = "sdk_init";
    public static final String AD_FLOW = "ad_flow";
    public static final String VIDEO_TAG = "video";
    // 生产环境
    private static final String APP_ID = "251022dtZJHhP0";
    private static final String SLOT_ID_BANNER = "251022drPYyKUV";
    private static final String SLOT_ID_SPLASH = "251022gEbYlOH9";
    private static final String SLOT_ID_NATIVE = "251022bygZOk73";
    private static final String SLOT_ID_INTERSTITIAL = "2510223pl72sct";
    private static final String SLOT_ID_REWARDED = "251022koW8TzeK";
    private static final String SLOT_ID_ICON = "251022KM9DlbtB";

    // 测试环境
    private static final String TEST_APP_ID = "251023x2FurTjr";
    private static final String TEST_SLOT_ID_BANNER = "2510233KBkaN6z";
    private static final String TEST_SLOT_ID_SPLASH = "251023Kfobf96i";
    private static final String TEST_SLOT_ID_NATIVE = "251023Ve7NeBqB";
    private static final String TEST_SLOT_ID_INTERSTITIAL = "251023Kf3qVR1G";
    private static final String TEST_SLOT_ID_REWARDED = "251023BYHAOv8i";
    private static final String TEST_SLOT_ID_ICON = "251023vvQLH7mf";

    public static String getAppId() {
        // 广告SDK 环境设置
        AdxServerConfig.setAppModle(IS_DEBUG ? AdxServerConfig.TEST : AdxServerConfig.RELEASE);
        return IS_DEBUG ? TEST_APP_ID : APP_ID;
    }

    public static String getBannerSlotId() {
        return IS_DEBUG ? TEST_SLOT_ID_BANNER : SLOT_ID_BANNER;
    }

    public static String getSplashSlotId() {
        return IS_DEBUG ? TEST_SLOT_ID_SPLASH : SLOT_ID_SPLASH;
    }

    public static String getNativeSlotId() {
        return IS_DEBUG ? TEST_SLOT_ID_NATIVE : SLOT_ID_NATIVE;
    }

    public static String getInterstitialSlotId() {
        return IS_DEBUG ? TEST_SLOT_ID_INTERSTITIAL : SLOT_ID_INTERSTITIAL;
    }

    public static String getRewardedSlotId() {
        return IS_DEBUG ? TEST_SLOT_ID_REWARDED : SLOT_ID_REWARDED;
    }

    public static String getIconSlotId() {
        return IS_DEBUG ? TEST_SLOT_ID_ICON : SLOT_ID_ICON;
    }

    public static final String USER_AGREE_PRIVACY = "user_agree_privacy";

}
