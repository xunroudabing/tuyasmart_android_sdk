package com.tuya.smart.android.demo.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tuya.smart.android.base.event.NetWorkStatusEvent;
import com.tuya.smart.android.base.event.NetWorkStatusEventModel;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.TuyaSmartApp;
import com.tuya.smart.android.demo.activity.BrowserActivity;
import com.tuya.smart.android.demo.activity.DeviceColorPickActivity;
import com.tuya.smart.android.demo.activity.MeshGateWayActivity;
import com.tuya.smart.android.demo.activity.SelectDeviceTypeActivity;
import com.tuya.smart.android.demo.activity.SharedActivity;
import com.tuya.smart.android.demo.activity.SwitchActivity;
import com.tuya.smart.android.demo.bean.DeviceAndGroupBean;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.fragment.DeviceListFragment;
import com.tuya.smart.android.demo.model.HomeBeanEvent;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.utils.ActivityUtils;
import com.tuya.smart.android.demo.utils.ProgressUtil;
import com.tuya.smart.android.demo.utils.ToastUtil;
import com.tuya.smart.android.demo.view.IDeviceListFragmentView;
import com.tuya.smart.android.device.event.MeshOnlineStatusUpdateEventModel;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.bluemesh.mesh.TuyaBlueMeshSearch;
import com.tuya.smart.bluemesh.mesh.device.ITuyaBlueMeshDevice;
import com.tuya.smart.bluemesh.mesh.search.ITuyaBlueMeshSearchListener;
import com.tuya.smart.home.interior.mesh.TuyaBlueMesh;
import com.tuya.smart.home.interior.presenter.TuyaDevice;
import com.tuya.smart.home.interior.presenter.TuyaSmartRequest;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.api.bluemesh.IMeshDevListener;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.tuyamesh.bean.SearchDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by letian on 15/6/1.
 */
public class DeviceListFragmentPresenter extends BasePresenter implements NetWorkStatusEvent,
        IMeshDevListener {

    public static final int ACTION_REGISTER = 200;
    public static final int ACTION_GETDATA = 201;
    static final String TAG = DeviceListFragmentPresenter.class.getSimpleName();
    private static final int WHAT_JUMP_GROUP_PAGE = 10212;
    protected Activity mActivity;
    protected IDeviceListFragmentView mView;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;
    private ITuyaHome mTuyaHome;
    //*************************************
    private HomeBean mHomeBean;
    private BlueMeshBean mBlueMeshBean;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_REGISTER:
                    registerMeshDevListener();
                    break;
                case ACTION_GETDATA:
                    getDataFromServer();
                    break;
            }

        }
    };

    public DeviceListFragmentPresenter(DeviceListFragment fragment, IDeviceListFragmentView view) {
        mActivity = fragment.getActivity();
        mView = view;
        //hanzheng to do  registerTuyaListChangedListener
        //TuyaUser.getDeviceInstance().registerTuyaListChangedListener(this);
        getDataFromServer();
        initEventBus();
        mHandler.sendEmptyMessageDelayed(ACTION_REGISTER, 5000);
    }

    public void onEvent(HomeBeanEvent event) {
        Log.d(TAG, "onEvent homebean");
        HomeBean homeBean = event.homeBean;
        Log.d(TAG, "homeid=" + homeBean.getHomeId());
    }

    public void registerMeshDevListener() {
        try {
            TuyaHomeSdk.getTuyaBlueMeshClient().startClient(TuyaSmartApp.getInstance()
                    .getBlueMeshBean());
            TuyaHomeSdk.getTuyaBlueMeshClient().startSearch();
            mTuyaHome = TuyaHomeSdk.newHomeInstance(CommonConfig.getHomeId(mActivity));
            Log.d(TAG, "DeviceListFragmentPresenter,meshid=" + CommonConfig.getMeshId
                    (mActivity));
            BlueMeshBean meshBean = TuyaBlueMesh.getMeshInstance().getBlueMeshBean(CommonConfig
                    .getMeshId(mActivity));
            mTuyaBlueMeshDevice = TuyaHomeSdk.newBlueMeshDeviceInstance(CommonConfig.getMeshId
                    (mActivity));
            mTuyaBlueMeshDevice.registerMeshDevListener(this);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    public void getData() {
        mView.loadStart();
        getDataFromServer();
    }

    private void showDevIsNotOnlineTip(final DeviceAndGroupBean deviceBean) {
        final boolean isShared = deviceBean.getIsShare();
        DialogUtil.customDialog(mActivity, mActivity.getString(R.string.title_device_offline),
                mActivity.getString(R.string.content_device_offline),
                mActivity.getString(isShared ? R.string.ty_offline_delete_share : R.string
                        .cancel_connect),
                mActivity.getString(R.string.right_button_device_offline), mActivity.getString(R
                        .string.left_button_device_offline), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (isShared) {
                                    //跳转到删除共享
                                    Intent intent = new Intent(mActivity, SharedActivity.class);
                                    intent.putExtra(SharedActivity.CURRENT_TAB, SharedActivity
                                            .TAB_RECEIVED);
                                    mActivity.startActivity(intent);
                                } else {
                                    DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString
                                            (R.string.device_confirm_remove), new DialogInterface
                                            .OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                                unBindDevice(deviceBean);
                                            }
                                        }
                                    });
                                }
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:
//                                //重置说明
                                Intent intent = new Intent(mActivity, BrowserActivity.class);
                                intent.putExtra(BrowserActivity.EXTRA_LOGIN, false);
                                intent.putExtra(BrowserActivity.EXTRA_REFRESH, true);
                                intent.putExtra(BrowserActivity.EXTRA_TOOLBAR, true);
                                intent.putExtra(BrowserActivity.EXTRA_TITLE, mActivity.getString
                                        (R.string.left_button_device_offline));
                                intent.putExtra(BrowserActivity.EXTRA_URI, CommonConfig.RESET_URL);
                                mActivity.startActivity(intent);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                }).show();

    }

    protected void onItemClick(DeviceAndGroupBean devBean) {
        if (devBean == null) {
            ToastUtil.showToast(mActivity, R.string.no_device_found);
            return;
        }
        if (devBean.getProductId() != null && devBean.getProductId().equals("4eAeY1i5sUPJ8m8d")) {
            Intent intent = new Intent(mActivity, SwitchActivity.class);
            intent.putExtra(SwitchActivity.INTENT_DEVID, devBean.getDevId());
            mActivity.startActivity(intent);
        } else {
            gotoDeviceCommonActivity(devBean);
        }

    }

    public void scan() {
        final TuyaBlueMeshSearch mMeshSearch = new TuyaBlueMeshSearch.Builder()
                .setMeshName("out_of_mesh")        //要扫描设备的名称（默认会是out_of_mesh，设备处于配网状态下的名称）
                .setTimeOut(10)        //扫描时长 单位秒
                .setTuyaBlueMeshSearchListener(new ITuyaBlueMeshSearchListener() {
                    @Override
                    public void onSearched(final SearchDeviceBean searchDeviceBean) {
                        Log.d(TAG, "onSearched:mac=" + searchDeviceBean.getMacAdress() + "," +
                                "meshname=" + searchDeviceBean.getMeshName());

                    }

                    @Override
                    public void onSearchFinish() {

                    }
                }).build();
        mMeshSearch.startSearch();

    }

    private void gotoDeviceCommonActivity(DeviceAndGroupBean devBean) {
        boolean isGroup = false;
        boolean isMesh = false;
        if (devBean.type == 1) {
            isGroup = false;
            if (devBean.device.isBleMesh()) {
                isMesh = true;
            }
            String category = devBean.device.getProductBean().getMeshCategory();
            //网关设备
            if (category.endsWith("08")) {
                Intent intent = new Intent(mActivity.getApplicationContext(), MeshGateWayActivity
                        .class);
                intent.putExtra(MeshGateWayActivity.INTNET_TITLE, devBean.getName());
                intent.putExtra(MeshGateWayActivity.INTENT_DEVID, devBean.getDevId());
                mActivity.startActivity(intent);
                return;
            }
        } else if (devBean.type == 2) {
            if (!TextUtils.isEmpty(devBean.group.getMeshId())) {
                isMesh = true;
            }
            isGroup = true;
        }

        //跳转至控制界面
        Intent intent = new Intent(mActivity, DeviceColorPickActivity.class);
        intent.putExtra(DeviceColorPickActivity.INTNET_TITLE, devBean.getName());
        intent.putExtra(DeviceColorPickActivity.INTENT_DEVID, devBean.getDevId());
        intent.putExtra(DeviceColorPickActivity.INTENT_PRODUCTID, devBean.getProductId());
        intent.putExtra(DeviceColorPickActivity.INTENT_ISGROUP, isGroup);
        intent.putExtra(DeviceColorPickActivity.INTENT_ISMESH, isMesh);

        if (isGroup) {
            intent.putExtra(DeviceColorPickActivity.INTENT_GROUPID, devBean.group.getId());
        } else {
            intent.putExtra(DeviceColorPickActivity.INTENT_MESH_NODEID, devBean.device.getNodeId());
            intent.putExtra(DeviceColorPickActivity.INTENT_MESH_CATEGORY, devBean.device
                    .getCategory());
        }
        mActivity.startActivity(intent);
//        Intent intent = new Intent(mActivity, DeviceCommonActivity.class);
//        intent.putExtra(DeviceCommonPresenter.INTENT_DEVID, devBean.getDevId());
//        mActivity.startActivity(intent);

//        Intent intent = new Intent(mActivity, CommonDeviceDebugActivity.class);
//        intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, devBean.getDevId());
//        mActivity.startActivity(intent);
    }

    public void getDataFromServer() {
        //hanzheng to do queryDevList
        //TuyaUser.getDeviceInstance().queryDevList();
        long homeId = CommonConfig.getHomeId(mActivity);
        Log.d(TAG, "getDataFromServer,homeId=" + homeId);
        if (homeId > 0) {
            TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                @Override
                public void onSuccess(HomeBean homeBean) {
                    updateLocalData(homeBean);
                }

                @Override
                public void onError(String s, String s1) {
                    Log.e(TAG, "getHomeDetail.onError" + s + "," + s1);
                    mView.loadFinish();
                }
            });
        } else {
            queryHomeList();
        }
    }

    public void gotoAddDevice() {
        //ActivityUtils.gotoActivity(mActivity, AddDeviceTipActivity.class, ActivityUtils
        // .ANIMATE_SLIDE_TOP_FROM_BOTTOM, false);
        ActivityUtils.gotoActivity(mActivity, SelectDeviceTypeActivity.class, ActivityUtils
                .ANIMATE_SLIDE_TOP_FROM_BOTTOM, false);
    }

    //添加设备
    public void addDevice() {
        final WifiManager mWifiManager = (WifiManager) mActivity.getApplicationContext()
                .getSystemService(Context
                        .WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString(R.string.open_wifi),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    mWifiManager.setWifiEnabled(true);
                                    gotoAddDevice();
                                    break;
                            }
                        }
                    });
        } else {
            gotoAddDevice();
        }
    }

    public void onDeviceClick(DeviceAndGroupBean deviceBean) {
        if (!deviceBean.getIsOnline()) {
            showDevIsNotOnlineTip(deviceBean);
            return;
        }
        onItemClick(deviceBean);
    }

    public boolean onDeviceLongClick(final DeviceAndGroupBean deviceBean) {
        if (deviceBean.getIsShare()) {
            return false;
        }
        DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString(R.string
                .device_confirm_remove), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (deviceBean.type == 1) {
                        unBindDevice(deviceBean);
                    } else if (deviceBean.type == 2) {
                        unBindGroup(deviceBean);
                    }
                }
            }
        });
        return true;
    }

    protected void unBindGroup(DeviceAndGroupBean deviceBean) {
        ProgressUtil.showLoading(mActivity, R.string.loading);
        if (!TextUtils.isEmpty(deviceBean.group.getMeshId())) {
            TuyaHomeSdk.newBlueMeshGroupInstance(deviceBean.group.getId()).dismissGroup(new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    ProgressUtil.hideLoading();
                    ToastUtil.showToast(mActivity, s1);
                }

                @Override
                public void onSuccess() {
                    ProgressUtil.hideLoading();
                    updateLocalData(null);
                }
            });
        } else {
            ITuyaGroup mTuyaGroup = TuyaHomeSdk.newGroupInstance(deviceBean.group.getId());
            mTuyaGroup.dismissGroup(new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    ProgressUtil.hideLoading();
                    ToastUtil.showToast(mActivity, s1);
                }

                @Override
                public void onSuccess() {
                    ProgressUtil.hideLoading();
                    updateLocalData(null);
                }
            });
        }
    }

    /**
     * 移除网关
     */
    private void unBindDevice(final DeviceAndGroupBean deviceBean) {
        ProgressUtil.showLoading(mActivity, R.string.loading);
        if (deviceBean.device != null) {
            if (deviceBean.device.isBleMesh()) {
                TuyaHomeSdk.newBlueMeshDeviceInstance(CommonConfig.getMeshId(mActivity))
                        .removeMeshSubDev(deviceBean.getDevId(), new IResultCallback() {
                            @Override
                            public void onError(String s, String s1) {
                                ProgressUtil.hideLoading();
                                ToastUtil.showToast(mActivity, s1);
                            }

                            @Override
                            public void onSuccess() {
                                ProgressUtil.hideLoading();
                                updateLocalData(null);
                            }
                        });
                return;
            } else {
                new TuyaDevice(deviceBean.getDevId()).removeDevice(new IResultCallback() {
                    @Override
                    public void onError(String s, String s1) {
                        ProgressUtil.hideLoading();
                        ToastUtil.showToast(mActivity, s1);
                    }

                    @Override
                    public void onSuccess() {
                        ProgressUtil.hideLoading();
                        updateLocalData(null);
                    }
                });
            }
        } else if (deviceBean.group != null) {
            boolean isMesh = false;
            if (!TextUtils.isEmpty(deviceBean.group.getMeshId())) {
                isMesh = true;
            }
            ITuyaGroup mTuyaGroup = null;
            long mGroupId = deviceBean.group.getId();
            if (isMesh) {
                mTuyaGroup = TuyaHomeSdk.newBlueMeshGroupInstance(mGroupId);
            } else {
                mTuyaGroup = TuyaHomeSdk.newGroupInstance(mGroupId);
            }
            mTuyaGroup.dismissGroup(new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    ProgressUtil.hideLoading();
                    ToastUtil.showToast(mActivity, s1);
                }

                @Override
                public void onSuccess() {
                    ProgressUtil.hideLoading();
                    updateLocalData(null);
                }
            });
        }


    }

    private void updateDeviceData(List<DeviceAndGroupBean> list) {
        if (list.size() == 0) {
            mView.showBackgroundView();
        } else {
            mView.hideBackgroundView();
            mView.updateDeviceData(list);
            mView.loadFinish();
        }
    }

    private void updateLocalData(HomeBean homeBean) {
        //updateDeviceData(TuyaUser.getDeviceInstance().getDevList());
        //调试信息
        //List<GroupBean> grouplist = TuyaUser.getDeviceInstance().getGroupList();
        List<GroupBean> grouplist = TuyaHomeSdk.getDataInstance().getHomeGroupList(CommonConfig
                .getHomeId(mActivity));
        //List<DeviceBean> devicelist = TuyaUser.getDeviceInstance().getDevList();
        List<DeviceBean> devicelist = TuyaHomeSdk.getDataInstance().getHomeDeviceList
                (CommonConfig.getHomeId(mActivity));
        List<DeviceAndGroupBean> mixlist = new ArrayList<>();
        if (devicelist != null) {
            for (DeviceBean bean : devicelist) {
                DeviceAndGroupBean item = new DeviceAndGroupBean();
                item.device = bean;
                item.type = 1;
                mixlist.add(item);
            }
        }
        if (grouplist != null) {
            for (GroupBean bean : grouplist) {
                DeviceAndGroupBean item = new DeviceAndGroupBean();
                item.group = bean;
                item.type = 2;
                mixlist.add(item);
            }
        }
        updateDeviceData(mixlist);
    }

    @Override
    public void onEvent(NetWorkStatusEventModel eventModel) {
        netStatusCheck(eventModel.isAvailable());
    }

    public void onEvent(MeshOnlineStatusUpdateEventModel eventModel) {
        Log.d(TAG, "MeshOnlineStatusUpdateEventModel:" + eventModel.getDevId() + "," + eventModel
                .getOnline());
        if (eventModel.getOnline() != null) {
            for (String s : eventModel.getOnline()) {
                Log.d(TAG, "online:" + s);
            }
        }
    }

    public boolean netStatusCheck(boolean isNetOk) {
        networkTip(isNetOk, R.string.ty_no_net_info);
        return true;
    }

    private void networkTip(boolean networkok, int tipRes) {
        if (networkok) {
            mView.hideNetWorkTipView();
        } else {
            mView.showNetWorkTipView(tipRes);
        }
    }

//    @Override
//    public void onDeviceChanged(TuyaListBean tuyaListBean) {
//        updateLocalData();
//    }

    public void onDestroy() {
        super.onDestroy();

        //hanzheng to do unRegisterTuyaListChangedListener
        //TuyaUser.getDeviceInstance().unRegisterTuyaListChangedListener(this);
    }

    public void addDemoDevice() {
        ProgressUtil.showLoading(mActivity, null);
        TuyaSmartRequest.getInstance().requestWithApiName("s.m.dev.sdk.demo.list", "1.0", null,
                new IRequestCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        ProgressUtil.hideLoading();
                        getDataFromServer();
                    }

                    @Override
                    public void onFailure(String errorCode, String errorMsg) {
                        ProgressUtil.hideLoading();
                        ToastUtil.showToast(mActivity, errorMsg);
                    }
                });
    }

    @Override
    public void onDpUpdate(String s, String s1, boolean b) {

    }

    @Override
    public void onStatusChanged(List<String> list, List<String> list1, String s) {
        Log.d(TAG, "onStatusChanged");
        if (list != null) {
            for (String s1 : list) {
                Log.d(TAG, "online:" + s1);
            }
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateLocalData(null);
            }
        });

    }

    @Override
    public void onNetworkStatusChanged(String s, boolean b) {
        Log.d(TAG, "onNetworkStatusChanged:s=" + s + ",b=" + b);
    }

    @Override
    public void onRawDataUpdate(byte[] bytes) {

    }

    @Override
    public void onDevInfoUpdate(String s) {

    }

    @Override
    public void onRemoved(String s) {

    }

    public void queryHomeList() {
        long homeId = CommonConfig.getHomeId(mActivity.getApplicationContext());
        if (homeId > 0) {
            Log.d(TAG, "getHomeDetail");
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
            Log.d(TAG, "queryHomeList");
            TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
                @Override
                public void onSuccess(List<HomeBean> list) {
                    if (list == null || list.isEmpty()) {
                        createHome();
                    } else {
                        mHomeBean = list.get(0);
                        CommonConfig.setHomeId(mActivity.getApplicationContext(), mHomeBean
                                .getHomeId());
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
        if (TuyaSmartApp.getInstance().getLocation() != null) {
            lat = TuyaSmartApp.getInstance().getLocation().getLatitude();
            lon = TuyaSmartApp.getInstance().getLocation().getLongitude();
            cityName = TuyaSmartApp.getInstance().getLocation().getProvince() + TuyaSmartApp
                    .getInstance().getLocation().getCity();
        }
        Log.d(TAG, "create Home....");
        TuyaHomeSdk.getHomeManagerInstance().createHome("home", lon, lat, cityName, new
                ArrayList<String>(), new
                ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean homeBean) {
                        Log.d(TAG, "createHome onSuccess,homeid=" + homeBean.getHomeId());
                        mHomeBean = homeBean;
                        CommonConfig.setHomeId(mActivity.getApplicationContext(), mHomeBean
                                .getHomeId());
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
            mHandler.sendEmptyMessageDelayed(ACTION_GETDATA, 3000);
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
                                CommonConfig.setMeshId(mActivity.getApplicationContext(),
                                        mBlueMeshBean
                                        .getMeshId());
                                TuyaHomeSdk.initMesh(mBlueMeshBean.getMeshId());
                            }
                        });
            } else {
                mBlueMeshBean = meshList.get(0);
                CommonConfig.setMeshId(mActivity.getApplicationContext(), mBlueMeshBean.getMeshId
                        ());
                TuyaHomeSdk.initMesh(mBlueMeshBean.getMeshId());
            }

        }
    }
}
