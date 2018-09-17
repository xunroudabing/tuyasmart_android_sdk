package com.tuya.smart.android.demo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.GroupDeviceCheckedAdapter;
import com.tuya.smart.android.demo.adapter.MeshGroupAddFailListAdapter;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.model.IMeshOperateGroupListener;
import com.tuya.smart.android.demo.model.MeshGroupDeviceListModel;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.widget.ListViewForScrollView;
import com.tuya.smart.home.interior.mesh.TuyaBlueMeshDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.api.bluemesh.IAddGroupCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加群组
 * Created by HanZheng(305058709@qq.com) on 2018-4-16.
 */

public class AddGroupActivity extends BaseActivity {
    public static final String INENT_PRODUCTID = "INENT_PRODUCTID";
    static final String TAG = AddGroupActivity.class.getSimpleName();
    List<GroupDeviceBean> mGroupDeviceBeans;
    ListViewForScrollView mUnChkListview, mChkListview;
    GroupDeviceCheckedAdapter mUnChkAdapter, mChkAdapter;
    Button btnOK;
    String mProductId;
    EditText editGroupName;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        initToolbar();
        initMenu();
        initViews();
        getGroupDevList();
    }

    protected void initViews() {
        editGroupName = (EditText) findViewById(R.id.add_group_editGroupName);
        mUnChkListview = (ListViewForScrollView) findViewById(R.id.add_group_listviewUnchk);
        mChkListview = (ListViewForScrollView) findViewById(R.id.add_group_listviewchk);
        btnOK = (Button) findViewById(R.id.add_group_btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGroup();
            }
        });
    }

    protected void initMenu() {
        setDisplayHomeAsUpEnabled();
        setTitle(R.string.add_group);
    }

    protected void bindUncheckListView(List<GroupDeviceBean> list) {
        mUnChkAdapter = new GroupDeviceCheckedAdapter(this, list, false);
        mUnChkListview.setAdapter(mUnChkAdapter);

    }

    protected void bindCheckedListView(List<GroupDeviceBean> list) {
        mChkAdapter = new GroupDeviceCheckedAdapter(this, list, true);
        mChkListview.setAdapter(mChkAdapter);
        mUnChkAdapter.attach(mChkAdapter);
        mChkAdapter.attach(mUnChkAdapter);
    }

    //创建群组
    protected void createNewGroup() {
        String groupName = editGroupName.getText().toString();
        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(AddGroupActivity.this, R.string.alert_group_name_null, Toast
                    .LENGTH_SHORT).show();
            return;
        }
        final List<String> devid_list = new ArrayList<>();
        boolean isMesh = false;
        int meshSize = 0;
        String pcc = "0501";
        if (mGroupDeviceBeans != null) {
            for (GroupDeviceBean bean : mGroupDeviceBeans) {
                if (bean.isChecked()) {
                    String devid = bean.getDeviceBean().getDevId();
                    devid_list.add(devid);
                    if (bean.getDeviceBean().isBleMesh()) {
                        pcc = bean.getDeviceBean().getProductBean().getMeshCategory();
                        meshSize++;
                    }
                }

            }
        }
        if (devid_list.isEmpty()) {
            Toast.makeText(AddGroupActivity.this, R.string.alert_check_device, Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (meshSize == devid_list.size() && !devid_list.isEmpty()) {
            isMesh = true;
        }
        if (isMesh) {
            createMeshGroup(groupName);
        } else {
            long homeId = CommonConfig.getHomeId(getApplicationContext());
            TuyaHomeSdk.newHomeInstance(homeId).createGroup(mProductId, groupName, devid_list, new
                    ITuyaResultCallback<Long>() {
                        @Override
                        public void onSuccess(Long l) {
                            Log.d(TAG, "createNewGroup.onSuccess:" + l);
                            Toast.makeText(AddGroupActivity.this, R.string.alert_group_add_sucess,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String s, String s1) {
                            Log.e(TAG, "createNewGroup.onError:" + s + "," + s1);
                        }
                    });
        }
    }

    protected void getGroupDevList() {
        mProductId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_PRODUCTID);
        TuyaHomeSdk.newHomeInstance(CommonConfig.getHomeId(getApplicationContext()))
                .queryDeviceListToAddGroup(mProductId, new
                        ITuyaResultCallback<List<GroupDeviceBean>>() {

                            @Override
                            public void onSuccess(List<GroupDeviceBean> bizResult) {
                                for (GroupDeviceBean bean : bizResult) {
                                    bean.setChecked(false);
                                }
                                //增加mesh设备支持
                                List<DeviceBean> meshList = TuyaHomeSdk.getDataInstance()
                                        .getMeshDeviceList(CommonConfig.getMeshId
                                                (getApplicationContext()));
                                if (meshList != null) {
                                    for (DeviceBean bean : meshList) {
                                        String category = bean.getProductBean().getMeshCategory();
                                        //过滤网关
                                        if (category.endsWith("08")) {
                                            continue;
                                        }
                                        GroupDeviceBean groupBean = new GroupDeviceBean();
                                        groupBean.setChecked(false);
                                        groupBean.setDeviceBean(bean);
                                        groupBean.setProductId(mProductId);
                                        bizResult.add(groupBean);
                                    }
                                }
                                bindUncheckListView(bizResult);
                                bindCheckedListView(bizResult);
                                mGroupDeviceBeans = bizResult;
                            }

                            @Override
                            public void onError(String s, String s1) {

                            }
                        });
    }

    public void addMeshDevice(long groupId) {
        //mesh设备
        GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (groupBean != null) {
            ITuyaGroup mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(groupId);
            ArrayList<DeviceBean> _addBeans = new ArrayList<>();
            ArrayList<DeviceBean> removeBeans = new ArrayList<>();
            if (mGroupDeviceBeans != null) {
                for (GroupDeviceBean bean : mGroupDeviceBeans) {
                    if (bean.isChecked()) {
                        _addBeans.add(bean.getDeviceBean());
                    }
                }
            }
            String mVendorId = groupBean.getCategory();
            final ArrayList<DeviceBean> addBeans = _addBeans;
            MeshGroupDeviceListModel mMeshGroupDeviceListModel = new MeshGroupDeviceListModel
                    (AddGroupActivity.this);
            mMeshGroupDeviceListModel.operateDevice(mGroup, addBeans, removeBeans, mVendorId, new
                    IMeshOperateGroupListener() {
                @Override
                public void operateSuccess(DeviceBean bean, final int index) {
                    L.d(TAG, "operateSuccess bean:" + bean.getName() + "success " + index);

                }

                @Override
                public void operateFinish(ArrayList<DeviceBean> failList) {
                    if (failList == null || failList.isEmpty()) {
                        Toast.makeText(AddGroupActivity.this, "群组操作成功", Toast.LENGTH_SHORT).show();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    } else {
                        showOperateFaileDialog(failList, addBeans.size());
                    }
                    if (failList != null) {
                        for (DeviceBean b : failList) {
                            L.e(TAG, "operateFinish fail:" + b.getName());
                        }
                    }
                }

                @Override
                public void operateFail(DeviceBean bean, final int index) {
                    L.d(TAG, "operateFail bean:" + bean.getName() + "success " + index);
                }
            });
        }
    }

    public void createMeshGroup(String groupName) {
        //检查群组是否已满
        String enableLocalId = getEnableGroupId(CommonConfig.getMeshId(getApplicationContext()));
        if (TextUtils.isEmpty(enableLocalId)) {
            Toast.makeText(AddGroupActivity.this, R.string.alert_group_isfull, Toast
                    .LENGTH_SHORT).show();
        } else {
            TuyaBlueMeshDevice mITuyaBlueMesh = new TuyaBlueMeshDevice(CommonConfig.getMeshId
                    (getApplicationContext()));
            //跨小类创建群组
            mITuyaBlueMesh.addGroup(groupName, "0501", enableLocalId, new IAddGroupCallback() {
                @Override
                public void onSuccess(long groupId) {
                    Toast.makeText(AddGroupActivity.this
                            , R.string.alert_group_add_sucess, Toast.LENGTH_SHORT).show();
                    //getDataFromServer();
                    final long mGroupId = groupId;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addMeshDevice(mGroupId);
                        }
                    }, 400);

                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    Toast.makeText(AddGroupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    public String getEnableGroupId(String meshId) {
        List<GroupBean> groupBeanList = TuyaHomeSdk.getDataInstance().getMeshGroupList(meshId);
        if (groupBeanList == null || groupBeanList.size() == 0) {
            return "8001";
        } else {
            String[] localIds = {"8001", "8002", "8003", "8004",
                    "8005", "8006", "8007", "8008"};
            List<String> localIdList = new ArrayList<>();
            for (String id : localIds) {
                localIdList.add(id);
            }
            for (GroupBean bean : groupBeanList) {
                if (localIdList.contains(bean.getLocalId())) {
                    localIdList.remove(bean.getLocalId());
                }
            }
            if (localIdList.size() == 0) {
                //callback.onFail(mContext.getString(R.string.mesh_group_full_tip));
                return "";
            } else {
                return localIdList.get(0);
            }
        }

    }

    public void showOperateFaileDialog(final ArrayList<DeviceBean> failList, final int
            subOperateCount) {
        MeshGroupAddFailListAdapter adapter = new MeshGroupAddFailListAdapter(AddGroupActivity
                .this);
        adapter.setData(failList);

        DialogUtil.customerListDialogTitleCenter(AddGroupActivity.this,
                "以下设备执行失败",
                adapter, null);
    }
}
