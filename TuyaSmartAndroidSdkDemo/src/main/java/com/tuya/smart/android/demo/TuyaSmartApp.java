package com.tuya.smart.android.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.tuya.smart.android.demo.activity.LoginActivity;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.service.LocationService;
import com.tuya.smart.android.demo.utils.ApplicationInfoUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.INeedLoginListener;

import java.util.ArrayList;
import java.util.List;

public class TuyaSmartApp extends Application {
    static final String TAG = TuyaSmartApp.class.getSimpleName();
    static final int ACTION_CREATE_HOME = 99;
    static TuyaSmartApp instance;
    public LocationService locationService;
    BDLocation mLocation;
    private HomeBean mHomeBean;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_CREATE_HOME:
                    queryHomeList();
                    break;
            }
        }
    };

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
        mHandler.sendEmptyMessageDelayed(ACTION_CREATE_HOME, 300);
    }

    public HomeBean getHomeBean() {
        return mHomeBean;
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
        TuyaHomeSdk.setDebugMode(true);
//        TuyaSdk.init(this);
//        TuyaSdk.setOnNeedLoginListener(new INeedLoginListener() {
//            @Override
//            public void onNeedLogin(Context context) {
//                Intent intent = new Intent(context, LoginActivity.class);
//                if (!(context instanceof Activity)) {
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//                startActivity(intent);
//            }
//        });
        //TuyaSdk.setDebugMode(true);
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

    //自动创建home 兼容meshSDK
    public void queryHomeList() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (list.isEmpty()) {
                    createHome();
                } else {
                    mHomeBean = list.get(0);
                    CommonConfig.setHomeId(getApplicationContext(), mHomeBean.getHomeId());
                }
            }

            @Override
            public void onError(String code, String error) {
                Log.e(TAG, "onError=" + code + "," + error);
            }
        });
    }

    protected void createHome() {
        double lat = 0D;
        double lon = 0D;
        String cityName = "浙江杭州";
        if (mLocation != null) {
            lat = mLocation.getLatitude();
            lon = mLocation.getLongitude();
            cityName = mLocation.getProvince() + mLocation.getCity();
        }
        Log.d(TAG, "create Home....");
        TuyaHomeSdk.getHomeManagerInstance().createHome("home", lon, lat, cityName, new
                ArrayList<String>(), new
                ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean homeBean) {
                        Log.d(TAG, "createHome onSuccess,homeid=" + homeBean.getHomeId());
                        mHomeBean = homeBean;
                        CommonConfig.setHomeId(getApplicationContext(), mHomeBean.getHomeId());
                    }

                    @Override
                    public void onError(String code, String error) {
                        Log.e(TAG, "createHome.onError=" + error);
                    }
                });
    }
}
