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
import com.tuya.smart.android.demo.activity.BrowserActivity;
import com.tuya.smart.android.demo.activity.DeviceColorPickActivity;
import com.tuya.smart.android.demo.activity.SelectDeviceTypeActivity;
import com.tuya.smart.android.demo.activity.SharedActivity;
import com.tuya.smart.android.demo.activity.SwitchActivity;
import com.tuya.smart.android.demo.bean.DeviceAndGroupBean;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.fragment.DeviceListFragment;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.utils.ActivityUtils;
import com.tuya.smart.android.demo.utils.ProgressUtil;
import com.tuya.smart.android.demo.utils.ToastUtil;
import com.tuya.smart.android.demo.view.IDeviceListFragmentView;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.bluemesh.mesh.device.ITuyaBlueMeshDevice;
import com.tuya.smart.home.interior.mesh.TuyaBlueMesh;
import com.tuya.smart.home.interior.presenter.TuyaDevice;
import com.tuya.smart.home.interior.presenter.TuyaSmartRequest;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.bluemesh.IMeshDevListener;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by letian on 15/6/1.
 */
public class DeviceListFragmentPresenter extends BasePresenter implements NetWorkStatusEvent,
        IMeshDevListener {

    static final String TAG = DeviceListFragmentPresenter.class.getSimpleName();
    private static final int WHAT_JUMP_GROUP_PAGE = 10212;
    protected Activity mActivity;
    protected IDeviceListFragmentView mView;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;
    private ITuyaHome mTuyaHome;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            registerMeshDevListener();
        }
    };

    public DeviceListFragmentPresenter(DeviceListFragment fragment, IDeviceListFragmentView view) {
        mActivity = fragment.getActivity();
        mView = view;
        //hanzheng to do  registerTuyaListChangedListener
        //TuyaUser.getDeviceInstance().registerTuyaListChangedListener(this);
        getDataFromServer();
        initEventBus();
        mHandler.sendEmptyMessageDelayed(0, 5000);
    }

    public void registerMeshDevListener() {
        try {
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
        if (devBean.getProductId().equals("4eAeY1i5sUPJ8m8d")) {
            Intent intent = new Intent(mActivity, SwitchActivity.class);
            intent.putExtra(SwitchActivity.INTENT_DEVID, devBean.getDevId());
            mActivity.startActivity(intent);
        } else {
            gotoDeviceCommonActivity(devBean);
        }

    }

    private void gotoDeviceCommonActivity(DeviceAndGroupBean devBean) {
        boolean isGroup = false;
        boolean isMesh = false;
        if (devBean.type == 1) {
            isGroup = false;
            if (devBean.device.isBleMesh()) {
                isMesh = true;
            }
        } else if (devBean.type == 2) {
            if(!TextUtils.isEmpty(devBean.group.getMeshId())){
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
        TuyaHomeSdk.newHomeInstance(CommonConfig.getHomeId(mActivity)).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                updateLocalData();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "getHomeDetail.onError" + s + "," + s1);
                mView.loadFinish();
            }
        });
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
                    }
                }
            }
        });
        return true;
    }

    /**
     * 移除网关
     */
    private void unBindDevice(final DeviceAndGroupBean deviceBean) {
        ProgressUtil.showLoading(mActivity, R.string.loading);
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
                            updateLocalData();
                        }
                    });
            return;
        }
        new TuyaDevice(deviceBean.getDevId()).removeDevice(new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                ToastUtil.showToast(mActivity, s1);
            }

            @Override
            public void onSuccess() {
                ProgressUtil.hideLoading();
                updateLocalData();
            }
        });

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


    private void updateLocalData() {
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

//    @Override
//    public void onDeviceChanged(TuyaListBean tuyaListBean) {
//        updateLocalData();
//    }

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
                updateLocalData();
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
}
