package com.tuya.smart.android.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.tuya.smart.android.base.TuyaSmartSdk;
import com.tuya.smart.android.demo.activity.LoginActivity;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.model.HomeBeanEvent;
import com.tuya.smart.android.demo.service.LocationService;
import com.tuya.smart.android.demo.utils.ApplicationInfoUtil;
import com.tuya.smart.home.interior.presenter.TuyaHomeShare;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.bean.BlueMeshBean;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class TuyaSmartApp extends MultiDexApplication {
    static final String TAG = TuyaSmartApp.class.getSimpleName();
    static final int ACTION_CREATE_HOME = 99;
    static TuyaSmartApp instance;
    public LocationService locationService;
    BDLocation mLocation;
    private HomeBean mHomeBean;
    private BlueMeshBean mBlueMeshBean;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_CREATE_HOME:
                    try {
                        queryHomeList();
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        TuyaHomeSdk.onDestroy();
        TuyaHomeSdk.onDestroyMesh();
    }

    public HomeBean getHomeBean() {
        return mHomeBean;
    }

    public BlueMeshBean getBlueMeshBean() {
        return mBlueMeshBean;
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
        long homeId = CommonConfig.getHomeId(getApplicationContext());
        if (homeId > 0) {
            Log.d(TAG,"getHomeDetail");
            TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                @Override
                public void onSuccess(HomeBean homeBean) {
                    mHomeBean = homeBean;
                    initMesh();
                }

                @Override
                public void onError(String s, String s1) {
                    Log.e(TAG, "onError:" + s1);
                }
            });
        } else {
            Log.d(TAG,"queryHomeList");
            TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
                @Override
                public void onSuccess(List<HomeBean> list) {
                    if (list == null || list.isEmpty()) {
                        createHome();
                    } else {
                        mHomeBean = list.get(0);
                        CommonConfig.setHomeId(getApplicationContext(), mHomeBean.getHomeId());
                        initMesh();
                    }
                }

                @Override
                public void onError(String code, String error) {
                    Log.e(TAG, "onError=" + code + "," + error);
                }
            });
        }
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
                        initMesh();
                    }

                    @Override
                    public void onError(String code, String error) {
                        Log.e(TAG, "createHome.onError=" + error);
                    }
                });
    }

    protected void initMesh() {
        if (mHomeBean != null) {
            List<BlueMeshBean> meshList = mHomeBean.getMeshList();
            if (meshList.isEmpty()) {
                TuyaHomeSdk.newHomeInstance(mHomeBean.getHomeId()).createBlueMesh("mesh", new
                        ITuyaResultCallback<BlueMeshBean>() {
                            @Override
                            public void onError(String errorCode, String errorMsg) {
                                Log.e(TAG, "initMesh.onError:" + errorMsg);
                            }

                            @Override
                            public void onSuccess(BlueMeshBean blueMeshBean) {
                                Log.d(TAG, "initMesh.onSuccess");
                                mBlueMeshBean = blueMeshBean;
                                CommonConfig.setMeshId(getApplicationContext(), mBlueMeshBean
                                        .getMeshId());
                                TuyaHomeSdk.initMesh(mBlueMeshBean.getMeshId());
                            }
                        });
            } else {
                mBlueMeshBean = meshList.get(0);
                CommonConfig.setMeshId(getApplicationContext(), mBlueMeshBean.getMeshId());
                TuyaHomeSdk.initMesh(mBlueMeshBean.getMeshId());
            }

        }
    }
}
