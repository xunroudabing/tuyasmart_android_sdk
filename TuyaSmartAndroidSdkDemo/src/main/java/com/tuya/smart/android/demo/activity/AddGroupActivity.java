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
import com.tuya.smart.sdk.TuyaGroup;
import com.tuya.smart.sdk.api.ICreateGroupCallback;
import com.tuya.smart.sdk.api.IGetDevsFromGroupByPidCallback;
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
    ListView mUnChkListview, mChkListview;
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
        mUnChkListview = (ListView) findViewById(R.id.add_group_listviewUnchk);
        mChkListview = (ListView) findViewById(R.id.add_group_listviewchk);
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
        List<String> devid_list = new ArrayList<>();
        if (mGroupDeviceBeans != null) {
            for (GroupDeviceBean bean : mGroupDeviceBeans) {
                if (bean.isChecked()) {
                    String devid = bean.getDeviceBean().getDevId();
                    devid_list.add(devid);
                }
            }
        }
        TuyaGroup.getGroupInstance().createNewGroup(mProductId, groupName, devid_list, new
                ICreateGroupCallback() {
                    @Override
                    public void onSuccess(long l) {
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

    protected void getGroupDevList() {
        mProductId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_PRODUCTID);
        TuyaGroup.getGroupInstance().getGroupDevList(mProductId, new
                IGetDevsFromGroupByPidCallback() {
                    @Override
                    public void onSuccess(List<GroupDeviceBean> bizResult) {
                        for (GroupDeviceBean bean : bizResult) {
                            bean.setChecked(false);
                        }
                        bindUncheckListView(bizResult);
                        bindCheckedListView(bizResult);
                        mGroupDeviceBeans = bizResult;
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
    }
}