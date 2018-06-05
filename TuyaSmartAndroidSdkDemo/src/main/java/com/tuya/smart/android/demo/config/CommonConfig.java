package com.tuya.smart.android.demo.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mikeshou on 15/5/26.
 */
public class CommonConfig {
    public static final String PACKAGE_NAME = "com.nbera.smartlife";
    public static final String RESET_URL = "http://smart.tuya.com/reset";

    public static final String FAILURE_URL = "http://smart.tuya.com/failure";

    public static final String TY_WIFI_PASSWD = "TY_WIFI_PASSWD";

    // FIXME: 16/4/7  改成通用
    public static final String DEFAULT_OLD_AP_SSID = "TuyaSmart";
    public static final String DEFAULT_COMMON_AP_SSID = "SmartLife";

    public static final String TY_ROUTER = "TY_ROUTER";
    public static final String DEFAULT_KEY_AP_SSID = "-TLinkAP-";

    public static void setBindColorList(Context context, String json) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("colorlist", json);
        editor.commit();
    }

    public static String getBindColorList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        String ret = preferences.getString("colorlist", null);
        return ret;
    }

    public static void setChooseDay(Context context, String str) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("day", str);
        editor.commit();
    }

    public static String getChooseDay(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        String ret = preferences.getString("day", "0000000");
        return ret;
    }

    public static void setChooseDayString(Context context, String str) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("daystring", str);
        editor.commit();
    }

    public static String getChooseDayString(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        String ret = preferences.getString("daystring", "仅限一次");
        return ret;
    }

    public static boolean getSwitch(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        boolean ret = preferences.getBoolean("switch", true);
        return ret;
    }

    public static void setSwitch(Context context, boolean enable) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("switch", enable);
        editor.commit();
    }

    public static void setTimer(Context context,String timer){
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("timer",timer);
        editor.commit();
    }

    public static String getTimer(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        String ret = preferences.getString("timer", null);
        return ret;
    }

    public static String getWifiPassword(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        String ret = preferences.getString("wifipass", null);
        return ret;
    }

    public static void setWifiPassword(Context context,String pass){
        SharedPreferences preferences = context.getSharedPreferences(PACKAGE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("wifipass",pass);
        editor.commit();
    }
}
