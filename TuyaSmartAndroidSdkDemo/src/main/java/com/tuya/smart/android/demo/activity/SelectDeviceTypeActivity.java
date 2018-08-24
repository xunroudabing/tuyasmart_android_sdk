package com.tuya.smart.android.demo.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.test.utils.UIFactory;
import com.tuya.smart.android.demo.utils.ActivityUtils;
import com.tuya.smart.android.demo.utils.BluetoothUtils;
import com.tuya.smart.android.demo.utils.CheckPermissionUtils;
import com.tuya.smart.bluemesh.mesh.TuyaBlueMeshSearch;
import com.tuya.smart.bluemesh.mesh.search.ITuyaBlueMeshSearchListener;
import com.tuya.smart.tuyamesh.bean.SearchDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择设备类型
 */
public class SelectDeviceTypeActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_OPEN_BLE = 1234;
    public static final int REQUEST_CODE_FOR_PERMISSION = 222;
    public static final int GPS_REQUEST_CODE = 223;
    public static final int MESH_BLUETOOTH_OPEN = 1;
    public static final int MESH_BLUETOOTH_CLOSE = 2;
    public static final int MESH_BLUETOOTH_NULL = -1;
    static final String TAG = SelectDeviceTypeActivity.class.getSimpleName();
    AlertDialog mDialog;
    ArrayList<SearchDeviceBean> mDeviceList = new ArrayList<>();
    ArrayList<SearchDeviceBean> mGateWayList = new ArrayList<>();
    LinearLayout btn1, btn2, btn3, btn4;
    Button btnScan;
    DialogInterface.OnClickListener onGateWayClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Intent intent = new Intent(getApplicationContext(), ECActivity.class);
                intent.putExtra(ECActivity.CONFIG_MODE, ECActivity.AP_MODE);
                intent.putParcelableArrayListExtra(ECActivity.INTENT_FOUND_DEVICE,
                        mGateWayList);
                startActivity(intent);
                finish();
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            }
        }
    };
    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener
            () {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Intent intent = new Intent(getApplicationContext(), MeshNewDeviceActivity.class);
                intent.putExtra(MeshNewDeviceActivity.INTENT_CONFIG_TYPE, 1);
                intent.putParcelableArrayListExtra(MeshNewDeviceActivity.INTENT_FOUND_DEVICE,
                        mDeviceList);
                intent.putExtra(MeshNewDeviceActivity.INTENT_MESHID, CommonConfig.getMeshId
                        (getApplicationContext()));
                startActivity(intent);
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_type);
        initToolbar();
        initMenu();
        initViews();
    }

    protected void initMenu() {
        setDisplayHomeAsUpEnabled();
        setTitle(R.string.title_select_device_type);
    }

    protected void initViews() {
        btnScan = (Button) findViewById(R.id.select_device_btnScan);
        btn1 = (LinearLayout) findViewById(R.id.select_device_btn1);
        btn2 = (LinearLayout) findViewById(R.id.select_device_btn2);
        btn3 = (LinearLayout) findViewById(R.id.select_device_btn3);
        btn4 = (LinearLayout) findViewById(R.id.select_device_btn4);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btnScan.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_device_btn1:
                ActivityUtils.gotoActivity(this, AddDeviceTipActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
            case R.id.select_device_btn2:
                ActivityUtils.gotoActivity(this, AddDeviceTipActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
            case R.id.select_device_btn3:
                ActivityUtils.gotoActivity(this, AddDeviceTipActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
            case R.id.select_device_btn4:
                ActivityUtils.gotoActivity(this, MeshNewDeviceActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
            case R.id.select_device_btnScan:
                check();
                break;
        }
    }

    public boolean check() {
        //检查 蓝牙和位置权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //不支持ble
            Toast.makeText(this, "This version not support bluetooth", Toast.LENGTH_LONG).show();
            return false;
        }

        if (checkBluetooth() == MESH_BLUETOOTH_CLOSE) {
            //开启蓝牙

            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_OPEN_BLE);
        } else {
            //检查位置权限
            Log.d(TAG, "check location permission");
            CheckPermissionUtils checkPermission = new CheckPermissionUtils(this);
            if (checkPermission.checkSiglePermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                    REQUEST_CODE_FOR_PERMISSION)
                    && checkPermission.checkSiglePermission(Manifest.permission
                    .ACCESS_FINE_LOCATION, REQUEST_CODE_FOR_PERMISSION)) {
                //检查gps
                Log.d(TAG, "check location gps");
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                // 判断GPS模块是否开启，如果没有则开启
                if (!locationManager
                        .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    DialogUtil.simpleConfirmDialog(this, getString(R.string.tip),
                            getString(R.string.open_gps), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_POSITIVE) {
                                        Intent intent = new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivityForResult(intent, GPS_REQUEST_CODE); //
                                        // 设置完成后返回到原来的界面
                                    }
                                }
                            });

                } else {
                    //扫描
                    scan();
                    return true;
                }
            } else {
                Toast.makeText(this, "请先开启定位权限", Toast.LENGTH_LONG).show();
            }
        }

        return false;

    }

    public int checkBluetooth() {
        if (!BluetoothUtils.isBleSupported(getApplicationContext())) {
            return MESH_BLUETOOTH_NULL;
        }
        if (BluetoothUtils.isBluetoothEnabled()) {
            return MESH_BLUETOOTH_OPEN;
        } else {
            return MESH_BLUETOOTH_CLOSE;
        }
    }

    private void scan() {
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
                        String venderId = Integer.toHexString(searchDeviceBean.getVendorId()).toUpperCase();
                        //网关设备
                        if(venderId.endsWith("08")){
                            mGateWayList.clear();
                            mGateWayList.add(searchDeviceBean);
                            showGateWayDialog();
                        }else {
                            mDeviceList.add(searchDeviceBean);
                            showDialog();
                        }

                    }

                    @Override
                    public void onSearchFinish() {
                        Toast.makeText(SelectDeviceTypeActivity.this, "扫描结束", Toast.LENGTH_SHORT)
                                .show();

                    }
                }).build();
        mMeshSearch.startSearch();
        Log.d(TAG, "mMeshSearch.startSearch();");

    }
    protected void showGateWayDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = String.format(getString(R.string.alert_discover_gateway),
                            mGateWayList
                                    .size());
                    if (mDialog == null) {
                        AlertDialog.Builder dialog = UIFactory.buildAlertDialog
                                (SelectDeviceTypeActivity.this);
                        dialog.setNegativeButton(R.string.ty_cancel, onGateWayClickListener);
                        dialog.setPositiveButton(R.string.ty_confirm, onGateWayClickListener);
                        dialog.setTitle(getString(R.string.title_discover_deviceToConnect));
                        dialog.setCancelable(false);
                        mDialog = dialog.create();
                    }
                    mDialog.setMessage(message);
                    if (!mDialog.isShowing()) {
                        mDialog.show();
                    }
                }catch (Exception ex){
                    Log.e(TAG,ex.toString());
                }

            }
        });
    }
    protected void showDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = String.format(getString(R.string.alert_discover_device),
                            mDeviceList
                                    .size());
                    if (mDialog == null) {
                        AlertDialog.Builder dialog = UIFactory.buildAlertDialog
                                (SelectDeviceTypeActivity.this);
                        dialog.setNegativeButton(R.string.ty_cancel, onClickListener);
                        dialog.setPositiveButton(R.string.ty_confirm, onClickListener);
                        dialog.setTitle(getString(R.string.title_discover_deviceToConnect));
                        dialog.setCancelable(false);
                        mDialog = dialog.create();
                    }
                    mDialog.setMessage(message);
                    if (!mDialog.isShowing()) {
                        mDialog.show();
                    }
                }catch (Exception ex){
                    Log.e(TAG,ex.toString());
                }

            }
        });

    }
}
