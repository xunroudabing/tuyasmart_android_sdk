package com.tuya.smart.android.demo.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.TuyaSmartApp;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.utils.ActivityUtils;
import com.tuya.smart.bluemesh.mesh.TuyaBlueMeshSearch;
import com.tuya.smart.bluemesh.mesh.builder.TuyaBlueMeshActivatorBuilder;
import com.tuya.smart.bluemesh.mesh.config.ITuyaBlueMeshActivator;
import com.tuya.smart.bluemesh.mesh.config.ITuyaBlueMeshActivatorListener;
import com.tuya.smart.bluemesh.mesh.search.ITuyaBlueMeshSearchListener;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.TuyaBlueMesh;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.tuyamesh.bean.SearchDeviceBean;

import java.util.ArrayList;
import java.util.List;

import static com.tuya.smart.android.demo.activity.SelectDeviceTypeActivity.MESH_BLUETOOTH_CLOSE;
import static com.tuya.smart.android.demo.activity.SelectDeviceTypeActivity.MESH_BLUETOOTH_NULL;
import static com.tuya.smart.android.demo.activity.SelectDeviceTypeActivity.MESH_BLUETOOTH_OPEN;
import static com.tuya.smart.android.demo.activity.SelectDeviceTypeActivity.REQUEST_OPEN_BLE;

/**
 * 发现新设备
 * Created by HanZheng(305058709@qq.com) on 2018-8-18.
 */

public class MeshNewDeviceActivity extends BaseActivity implements View.OnClickListener {
    public static final String INTENT_WIFI_SSID = "INTENT_WIFI_SSID";
    public static final String INTENT_WIFI_PASSWORD = "INTENT_WIFI_PASSWORD";
    public static final String INTENT_CONFIG_TYPE = "INTENT_CONFIG_TYPE";
    public static final String INTENT_MESHID = "INTENT_MESHID";
    public static final String INTENT_FOUND_DEVICE = "INTENT_FOUND_DEVICE";
    static final int CONFIG_DEV_MAX_TIME = 120;
    static final String TAG = MeshNewDeviceActivity.class.getSimpleName();
    int configType = 1;
    String meshId;
    List<SearchDeviceBean> foundDevice;
    List<DeviceBean> addedDevice = new ArrayList<>();
    TextView txtCount;
    LinearLayout layoutDiscover, layoutFoundDevice, layoutProgress, layoutSuccess,
            layoutSuccessGroup, layoutOpenBlueTooth;
    Button btnNext, btnStart;
    ImageView imgSuccess;
    TextView txtSuccess;
    int allCount = 0;
    int succCount = 0;
    int failCount = 0;
    ImageButton btnOpenBlueTooth;
    String mSSID;
    String mPassword;
    private ITuyaBlueMeshActivator iTuyaBlueMeshActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_mesh);
        initToolbar();
        initViews();
        initMenu();
        initData();
        detectBlueTooth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN_BLE) {
            detectBlueTooth();
        }
    }

    protected void initMenu() {
        setTitle(R.string.home_add_device);
        setDisplayHomeAsUpEnabled();
    }

    protected void initViews() {
        btnOpenBlueTooth = (ImageButton) findViewById(R.id.open_bluetooth_btnOpen);
        layoutOpenBlueTooth = (LinearLayout) findViewById(R.id.discover_mesh_layoutOpenBlueTooth);
        layoutDiscover = (LinearLayout) findViewById(R.id.discover_mesh_layoutDiscover);
        layoutSuccessGroup = (LinearLayout) findViewById(R.id.discover_mesh_layoutSuccessGroup);
        layoutFoundDevice = (LinearLayout) findViewById(R.id.discover_mesh_layoutGroup);
        layoutProgress = (LinearLayout) findViewById(R.id.discover_mesh_layoutProgress);
        layoutSuccess = (LinearLayout) findViewById(R.id.discover_mesh_layoutSuccess);
        txtCount = (TextView) findViewById(R.id.discover_mesh_txtCount);
        txtSuccess = (TextView) findViewById(R.id.discover_mesh_txtSuccessCount);
        imgSuccess = (ImageView) findViewById(R.id.discover_mesh_imgSuccess);
        btnStart = (Button) findViewById(R.id.discover_mesh_btnStart);
        btnNext = (Button) findViewById(R.id.discover_mesh_btnNext);
        btnNext.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnOpenBlueTooth.setOnClickListener(this);
    }

    protected void initData() {
        configType = getIntent().getIntExtra(INTENT_CONFIG_TYPE, 0);
        meshId = getIntent().getStringExtra(INTENT_MESHID);
        foundDevice = getIntent().getParcelableArrayListExtra(INTENT_FOUND_DEVICE);

        mSSID = getIntent().getStringExtra(INTENT_WIFI_SSID);
        mPassword = getIntent().getStringExtra(INTENT_WIFI_PASSWORD);
        showFoundDevice();
    }

    protected void detectBlueTooth() {
        int state = checkBluetooth();
        if (state != MESH_BLUETOOTH_OPEN) {
            //提示打开蓝牙
            layoutOpenBlueTooth.setVisibility(View.VISIBLE);
        } else {
            //蓝牙已打开 扫描
            layoutOpenBlueTooth.setVisibility(View.GONE);
            scan();
        }
    }

    protected void showFoundDevice() {
        if (foundDevice == null || foundDevice.isEmpty()) {
            return;
        }
        String message = String.format(getString(R.string.alert_discover_device), foundDevice
                .size());
        txtCount.setText(message);
        int i = 1;
        layoutFoundDevice.removeAllViews();
        for (SearchDeviceBean bean : foundDevice) {
            TextView txtView = (TextView) LayoutInflater.from(this).inflate(R.layout
                    .text_config_mesh, layoutFoundDevice, false);
            String name = getString(R.string.newdevice) + " - " + i;
            txtView.setText(name);
            layoutFoundDevice.addView(txtView);
            i++;
        }
        layoutDiscover.setVisibility(View.VISIBLE);
        layoutProgress.setVisibility(View.GONE);

    }

    public int checkBluetooth() {
        if (!BluetoothUtils.isBleSupported()) {
            return MESH_BLUETOOTH_NULL;
        }
        if (BluetoothUtils.isBluetoothEnabled()) {
            return MESH_BLUETOOTH_OPEN;
        } else {
            return MESH_BLUETOOTH_CLOSE;
        }
    }

    public void config() {
        //配网前要先停止连接
        TuyaHomeSdk.getTuyaBlueMeshClient().stopClient();
        if (configType == 1) {
            //普通设备配网
            configMesh(foundDevice, meshId);
        } else if (configType == 2) {
            //网关设备配网
            configWifiMesh(foundDevice, meshId);
        }
    }

    private void configMesh(List<SearchDeviceBean> foundDevice, String meshId) {
        allCount = foundDevice.size();
        succCount = 0;
        failCount = 0;
        BlueMeshBean blueMeshBean = TuyaBlueMesh.getMeshInstance().getBlueMeshBean(meshId);
        if (blueMeshBean == null) {
            Log.e(TAG, "blueMeshBean is null");
        }
        TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new
                TuyaBlueMeshActivatorBuilder()
                .setSearchDeviceBeans(foundDevice)
                //默认版本号
                .setVersion("1.0")
                .setBlueMeshBean(blueMeshBean)
                //超时时间
                .setTimeOut(CONFIG_DEV_MAX_TIME)
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {
                    @Override
                    public void onSuccess(DeviceBean deviceBean) {
                        Log.d(TAG, "subDevBean onSuccess: " + deviceBean.getName());
                        addedDevice.add(deviceBean);
                        succCount++;
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Log.d(TAG, "config mesh error" + errorCode + " " + errorMsg);
                        failCount = 0;
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "config mesh onFinish： ");
                        complete();
                    }
                });

        iTuyaBlueMeshActivator = TuyaHomeSdk.getBlueMeshActivatorInstance().newActivator
                (tuyaBlueMeshActivatorBuilder);
        iTuyaBlueMeshActivator.startActivator();

    }


    public void configWifiMesh(List<SearchDeviceBean> foundDevice, String meshId) {
        String wifiName = mSSID;
        String wifiPwd = mPassword;
        long homeId = CommonConfig.getHomeId(getApplicationContext());
        allCount = foundDevice.size();
        succCount = 0;
        failCount = 0;
        BlueMeshBean blueMeshBean = TuyaBlueMesh.getMeshInstance().getBlueMeshBean(meshId);
        if (blueMeshBean == null) {
            Log.e(TAG, "blueMeshBean is null");
        }
        TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new
                TuyaBlueMeshActivatorBuilder()
                .setWifiSsid(wifiName)
                .setWifiPassword(wifiPwd)
                .setSearchDeviceBeans(foundDevice)
                .setVersion("2.2")
                .setBlueMeshBean(blueMeshBean)
                .setHomeId(homeId)
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {

                    @Override
                    public void onSuccess(DeviceBean devBean) {
                        //单个设备配网成功回调
                        Log.d(TAG, "startConfig  success");
                        addedDevice.add(devBean);
                        succCount++;
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        //单个设备配网失败回调
                        Log.d(TAG, "errorCode: " + errorCode + " errorMsg: " + errorMsg);
                        failCount = 0;
                    }

                    @Override
                    public void onFinish() {
                        //所有设备配网结束回调
                        Log.d(TAG, "subDevBean onFinish: ");
                        complete();
                    }
                });

        iTuyaBlueMeshActivator = TuyaHomeSdk.getBlueMeshActivatorInstance().newWiFiActivator
                (tuyaBlueMeshActivatorBuilder);
        iTuyaBlueMeshActivator.startActivator();

    }

    protected void next() {
        layoutDiscover.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.VISIBLE);
        config();
    }

    protected void openBlueTooth() {
        Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_OPEN_BLE);
    }

    protected void complete() {
        // 开启连接
        TuyaHomeSdk.getTuyaBlueMeshClient().startClient(TuyaSmartApp.getInstance()
                .getBlueMeshBean());
        if (addedDevice.isEmpty()) {
            txtSuccess.setText(R.string.add_deivce_fail);
            imgSuccess.setImageResource(R.drawable.ty_ez_add_failure);
        } else {
            String message = String.format(getString(R.string.add_device_success), addedDevice
                    .size());
            txtSuccess.setText(message);
            for (DeviceBean bean : addedDevice) {
                TextView txtView = (TextView) LayoutInflater.from(this).inflate(R.layout
                        .text_config_mesh, layoutSuccessGroup, false);
                txtView.setText(bean.getName());
                layoutSuccessGroup.addView(txtView);
            }
        }
        layoutProgress.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.VISIBLE);
    }

    protected void goHome() {
        ActivityUtils.gotoHomeActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.discover_mesh_btnNext:
                next();
                break;
            case R.id.discover_mesh_btnStart:
                goHome();
                break;
            case R.id.open_bluetooth_btnOpen:
                openBlueTooth();
                break;
        }
    }

    private void scan() {
        layoutProgress.setVisibility(View.VISIBLE);
        final List<String> searchNameList = new ArrayList<>();
        final ArrayList<SearchDeviceBean> searchList = new ArrayList();
        final TuyaBlueMeshSearch mMeshSearch = new TuyaBlueMeshSearch.Builder()
                .setMeshName("out_of_mesh")        //要扫描设备的名称（默认会是out_of_mesh，设备处于配网状态下的名称）
                .setTimeOut(30)        //扫描时长 单位秒
                .setTuyaBlueMeshSearchListener(new ITuyaBlueMeshSearchListener() {
                    @Override
                    public void onSearched(final SearchDeviceBean searchDeviceBean) {
                        Log.d(TAG, "onSearched:mac=" + searchDeviceBean.getMacAdress() + "," +
                                "meshname=" + searchDeviceBean.getMeshName());
                        if (foundDevice == null) {
                            foundDevice = new ArrayList<>();
                        }
                        foundDevice.add(searchDeviceBean);
                        showFoundDevice();
                    }

                    @Override
                    public void onSearchFinish() {
                        Toast.makeText(MeshNewDeviceActivity.this, "扫描结束", Toast.LENGTH_SHORT)
                                .show();
                    }
                }).build();
        mMeshSearch.startSearch();
        Log.d(TAG, "mMeshSearch.startSearch();");

    }
}
