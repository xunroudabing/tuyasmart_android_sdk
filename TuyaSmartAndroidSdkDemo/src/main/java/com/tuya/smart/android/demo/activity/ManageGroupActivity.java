package com.tuya.smart.android.demo.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.MeshGroupAddFailListAdapter;
import com.tuya.smart.android.demo.adapter.MeshGroupDevListAdapter;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.model.IMeshOperateGroupListener;
import com.tuya.smart.android.demo.model.MeshGroupDeviceListModel;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.bluemesh.mesh.device.ITuyaBlueMeshDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

import static com.tuya.smart.android.demo.adapter.MeshGroupDevListAdapter.InnerViewHolder
        .ADD_ACTION;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-9-20.
 */

public class ManageGroupActivity extends BaseActivity implements MeshGroupDevListAdapter
        .OnClickSelectListener {
    public static final String INTENT_GROUPID = "INTENT_GROUPID";
    public static final String INTENT_ISMESH = "INTENT_ISMESH";
    static final String TAG = ManageGroupActivity.class.getSimpleName();
    boolean isMesh = false;
    RecyclerView mRecyclerView;
    private MeshGroupDevListAdapter mGroupDevAdapter;
    private ITuyaGroup mTuyaGroup;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;
    private String mVendorId;
    private GroupBean groupBean;
    private String mMeshId;
    private long mGroupId;
    private ArrayList<DeviceBean> mFoundDeviceBean = new ArrayList<>();
    private ArrayList<DeviceBean> mAddDeviceBean = new ArrayList<>();

    private ArrayList<DeviceBean> mOldFoundDeviceBean = new ArrayList<>();
    private ArrayList<DeviceBean> mOldAddDeviceBean = new ArrayList<>();
    private MeshGroupDeviceListModel mMeshGroupDeviceListModel;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initToolbar();
        initMenu();
        getData();
    }

    protected void initViews() {
        setContentView(R.layout.activity_manage_group_list);
        mMeshGroupDeviceListModel = new MeshGroupDeviceListModel(this);
        mRecyclerView = findViewById(R.id.lv_group_device_list);
        mGroupDevAdapter = new MeshGroupDevListAdapter(this, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mGroupDevAdapter);
    }

    protected void initMenu() {
        setTitle(R.string.menu_manage_group);
        setMenu(R.menu.toolbar_manage_group, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_manage_group_confirm:
                        doConfirm();
                        break;
                }
                return false;
            }
        });
        setDisplayHomeAsUpEnabled(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void getData() {
        mGroupId = getIntent().getLongExtra(INTENT_GROUPID, 0);
        mMeshId = CommonConfig.getMeshId(getApplicationContext());

        groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
        mVendorId = groupBean.getCategory();
        mTuyaBlueMeshDevice = TuyaHomeSdk.newBlueMeshDeviceInstance(mMeshId);
        if (groupBean != null) {
            mVendorId = groupBean.getCategory();
            if (!TextUtils.isEmpty(groupBean.getMeshId())) {
                isMesh = true;
            }
        }
        if (isMesh) {
            mTuyaGroup = TuyaHomeSdk.newBlueMeshGroupInstance(mGroupId);
        } else {
            mTuyaGroup = TuyaHomeSdk.newGroupInstance(mGroupId);
        }
        queryDevicesByGroupId();
    }

    private void queryDevicesByGroupId() {
        mFoundDeviceBean.clear();
        mAddDeviceBean.clear();
        List<DeviceBean> devList = TuyaHomeSdk.getDataInstance().getHomeDeviceList
                (CommonConfig.getHomeId(getApplicationContext()));
        //List<DeviceBean> devList = TuyaHomeSdk.getDataInstance().getMeshDeviceList(mMeshId);
        for (DeviceBean bean : devList) {
            //TODO 不同大小类的设备  部分命令不通用 在这里可以过滤出相同的大小类的设备
            if (!bean.getCategory().equals(mVendorId)) {
                continue;
            }
            //过滤wifi 与 mesh 设备
            boolean devMesh = bean.isBleMesh();
            if (isMesh == devMesh) {
                mFoundDeviceBean.add(bean);
            }
            L.d("huohuo", "bean--->  nodeid:" + bean.getNodeId() + "  productId:" + bean
                    .getProductId() + "   devId:" + bean.getDevId());
        }

        List<DeviceBean> deviceBeanList = TuyaHomeSdk.getDataInstance().getGroupDeviceList
                (mGroupId);
        if (deviceBeanList != null && deviceBeanList.size() > 0) {
            List<DeviceBean> mTempList = new ArrayList<>();
            mTempList.addAll(mFoundDeviceBean);
            for (DeviceBean groupBean : mTempList) {
                for (DeviceBean subBean : deviceBeanList) {
                    if (subBean.getDevId().equals(groupBean.getDevId())) {
                        mAddDeviceBean.add(groupBean);
                        mFoundDeviceBean.remove(groupBean);
                    }
                }
            }
            mOldAddDeviceBean.addAll(mAddDeviceBean);
            mOldFoundDeviceBean.addAll(mFoundDeviceBean);
        }
        updateDeviceList();
    }

    protected void updateDeviceList() {
        if (mAddDeviceBean.size() > 0 || mFoundDeviceBean.size() > 0) {
            mGroupDevAdapter.setAddData(mAddDeviceBean);
            mGroupDevAdapter.setFoundData(mFoundDeviceBean);
        }
    }

    public void removeDeviceToMeshGroup(final DeviceBean bean) {

        mAddDeviceBean.remove(bean);
        mFoundDeviceBean.add(bean);
        updateDeviceList();

    }


    public void addDeviceToMeshGroup(final DeviceBean groupDevice) {
        mFoundDeviceBean.remove(groupDevice);
        mAddDeviceBean.add(groupDevice);
        updateDeviceList();
    }

    @Override
    public void onClickSelect(int actionType, DeviceBean bean) {
        if (!bean.getIsOnline()) {
            Toast.makeText(this, R.string.alert_device_offline, Toast.LENGTH_SHORT).show();
            return;
        }

        if (actionType == ADD_ACTION) {
            removeDeviceToMeshGroup(bean);
        } else {
            //添加bean到group
            addDeviceToMeshGroup(bean);
        }
    }

    public ArrayList<DeviceBean> getAddDevice() {
        ArrayList<DeviceBean> addDeviceTemp = new ArrayList<>();
        //找到需要添加的设备
        for (DeviceBean addBean : mAddDeviceBean) {
            if (!mOldAddDeviceBean.contains(addBean)) {
                addDeviceTemp.add(addBean);
            }
        }

        L.e(TAG, "getAddDevice:" + addDeviceTemp.size());
        return addDeviceTemp;
    }

    public ArrayList<DeviceBean> getDeleteDevice() {
        ArrayList<DeviceBean> deleteDeviceTemp = new ArrayList<>();
        //找到需要添加的设备
        for (DeviceBean deleteBean : mOldAddDeviceBean) {
            if (!mAddDeviceBean.contains(deleteBean)) {
                deleteDeviceTemp.add(deleteBean);
            }
        }

        L.e(TAG, "getDeleteDevice:" + deleteDeviceTemp.size());
        return deleteDeviceTemp;
    }
    protected void doConfirm(){
        if (getAddDevice().isEmpty() && getDeleteDevice().isEmpty()) {
            finish();
        } else {
            operateDevice();
        }
    }
    public void operateDevice() {
        //找出要操作的数据
        final ArrayList<DeviceBean> addBeans = getAddDevice();
        final ArrayList<DeviceBean> removeBeans = getDeleteDevice();
        final int subCount = addBeans.size() + removeBeans.size();
        final String msg = "%s/" + subCount + "  修改生效中..请勿关闭应用";

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.bluemesh_dialog_progress);

        final TextView tvMsg = (TextView) dialog.findViewById(R.id.progress_dialog_message);
        tvMsg.setText(String.format(msg, "0"));

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        dialog.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMeshGroupDeviceListModel.operateDevice(mTuyaGroup, addBeans, removeBeans, mVendorId, new IMeshOperateGroupListener() {
                    @Override
                    public void operateSuccess(DeviceBean bean, final int index) {
                        L.d(TAG, "operateSuccess bean:" + bean.getName() + "success " + index);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvMsg.setText(String.format(msg, index + ""));

                            }
                        });

                    }

                    @Override
                    public void operateFinish(ArrayList<DeviceBean> failList) {
                        if (failList == null || failList.isEmpty()) {
                            Toast.makeText(ManageGroupActivity.this,"群组操作成功",Toast.LENGTH_SHORT).show();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },100);
                        } else {
                            showOperateFaileDialog(failList, addBeans.size() + removeBeans.size());
                        }
                        if (failList != null) {
                            for (DeviceBean b : failList) {
                                L.e(TAG, "operateFinish fail:" + b.getName());
                            }
                        }
                        dialog.cancel();
                    }

                    @Override
                    public void operateFail(DeviceBean bean, final int index) {
                        L.d(TAG, "operateFail bean:" + bean.getName() + "success " + index);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvMsg.setText(String.format(msg, index + ""));
                            }
                        });

                    }
                });
            }
        }, 1000);

    }

    public void showOperateFaileDialog(final ArrayList<DeviceBean> failList, final int subOperateCount) {
        MeshGroupAddFailListAdapter adapter = new MeshGroupAddFailListAdapter(this);
        adapter.setData(failList);

        DialogUtil.customerListDialogTitleCenter(this,
                "以下设备执行失败",
                adapter, null);
    }
}
