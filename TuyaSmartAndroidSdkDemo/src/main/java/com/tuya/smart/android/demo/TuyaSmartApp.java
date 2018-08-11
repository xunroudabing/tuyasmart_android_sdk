package com.tuya.smart.android.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.tuya.smart.android.demo.activity.LoginActivity;
import com.tuya.smart.android.demo.service.LocationService;
import com.tuya.smart.android.demo.utils.ApplicationInfoUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;

public class TuyaSmartApp extends Application {
    static final String TAG = TuyaSmartApp.class.getSimpleName();
    static TuyaSmartApp instance;
    public LocationService locationService;
    BDLocation mLocation;

    public static TuyaSmartApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        if (isInitAppkey()) {
            initSdk();
        }
        initBaiduLocation();
    }

    private void initSdk() {
        TuyaHomeSdk.init(this);
        TuyaHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Intent intent = new Intent(context, LoginActivity.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });
        TuyaSdk.init(this);
        TuyaSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Intent intent = new Intent(context, LoginActivity.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });
        TuyaSdk.setDebugMode(true);
    }

    private void initBaiduLocation() {
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        //SDKInitializer.initialize(getApplicationContext());
        locationService.registerListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation != null && !TextUtils.isEmpty(bdLocation.getCity())) {
                    mLocation = bdLocation;
                    Log.d(TAG, "location:" + mLocation.getLatitude() + "," + mLocation
                            .getLongitude() + "," + mLocation.getCity());
                }
            }
        });
        locationService.start();
    }

    public BDLocation getLocation() {
        return mLocation;
    }

    private boolean isInitAppkey() {
        String appkey = ApplicationInfoUtil.getInfo("TUYA_SMART_APPKEY", this);
        String appSecret = ApplicationInfoUtil.getInfo("TUYA_SMART_SECRET", this);
        if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(appSecret)) {
            return false;
        }
        return true;
    }


}
