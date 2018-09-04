package com.tuya.smart.android.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.GroupDeviceCheckedAdapter;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.widget.ListViewForScrollView;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.bluemesh.IAddGroupCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
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
            int localId = CommonConfig.getLocalId(getApplicationContext()) + 1;
            localId = Math.max(8008, localId);
            final int lid = localId;
            TuyaHomeSdk.newBlueMeshInstance(CommonConfig.getMeshId(getApplicationContext()))
                    .addGroup
                            (groupName, pcc, String.valueOf(localId), new IAddGroupCallback() {
                                @Override
                                public void onSuccess(long l) {
                                    Log.d(TAG, "createNewGroup.onSuccess:groupid=" + l);
                                    CommonConfig.setLocalId(getApplicationContext(), lid);
                                    for (int i = 0; i < devid_list.size(); i++) {
                                        final String devid = devid_list.get(i);
                                        final int j = i;
                                        TuyaHomeSdk.newBlueMeshGroupInstance(l).addDevice(devid,
                                                new IResultCallback() {
                                                    @Override
                                                    public void onError(String s, String s1) {
                                                        Log.e(TAG, "group add device.onError:" + s
                                                                + "," + s1 + ",devid=" + devid);
                                                    }

                                                    @Override
                                                    public void onSuccess() {
                                                        Log.d(TAG, "group add device onSuccess," +
                                                                "devid=" + devid);
                                                        if (j == devid_list.size() - 1) {
                                                            Toast.makeText(AddGroupActivity.this, R
                                                                            .string
                                                                            .alert_group_add_sucess,
                                                                    Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    }
                                                });
                                        //间隔320ms
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            Log.e(TAG,e.toString());
                                        }
                                    }

                                }

                                @Override
                                public void onError(String s, String s1) {
                                    Log.e(TAG, "createNewGroup.onError:" + s + "," + s1);
                                    Toast.makeText(AddGroupActivity.this, s1, Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
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
                                        if(category.endsWith("08")){
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
}
