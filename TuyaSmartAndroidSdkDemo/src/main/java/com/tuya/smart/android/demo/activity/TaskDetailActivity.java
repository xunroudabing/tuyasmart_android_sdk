package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.FunctionListItemRecyclerAdapter;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.android.demo.bean.SceneActionBean;
import com.tuya.smart.android.demo.bean.SceneConditonBean;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.dev.SceneDevBean;
import com.tuya.smart.sdk.bean.scene.dev.TaskListBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 选择功能
 * Created by HanZheng(305058709@qq.com) on 2018-3-27.
 */

public class TaskDetailActivity extends BaseActivity {
    public static final int REQUEST_SECOND = 101;
    public static final String RESULT_SCENECONDITIONBEAN = "RESULT_SCENECONDITIONBEAN";
    public static final String BUNDLE_SCENE_ACTION = "BUNDLE_SCENE_ACTION";
    public static final String BUNDLE_RULE_TYPE = "BUNDLE_RULE_TYPE";
    public static final String BUNDLE_RULE = "BUNDLE_RULE";
    public static final String BUNDLE_DPID = "BUNDLE_DPID";
    public static final String BUNDLE_TASKDES = "BUNDLE_TASKDES";
    public static final String BUNDLE_DEVICEID = "BUNDLE_DEVICEID";
    public static final String BUNDLE_DEVICENAME = "BUNDLE_DEVICENAME";
    public static final String BUNDLE_DATA = "BUNDLE_DATA";
    public static final String INTENT_SCENE_ACTION = "INTENT_SCENE_ACTION";
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    public static final String INTENT_SCENE_BEAN = "INTENT_SCENE_BEAN";
    public static final String INTENT_DEVICEID = "INTENT_DEVICEID";
    public static final String INTENT_DEVICENAME = "INTENT_DEVICENAME";
    static final String TAG = TaskDetailActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    FunctionListItemRecyclerAdapter mAdapter;
    SceneDevBean mBean;
    String mRule;
    String mRuleType;
    String mDevId;
    String mTaskDescription;
    String mDpId;
    Map<String, Object> mMap = new HashMap<String, Object>();
    SceneActionBean mActionBean;
    //用于设备条件
    SceneConditonBean mConditonBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        initToolbar();
        initMenu();
        initParms();
        initViews();
        getOperationList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SECOND) {
            if (resultCode == RESULT_OK) {
                mTaskDescription = data.getStringExtra(TaskSecondDetailActivity.BUNDLE_TASK_DES);
                int position = data.getIntExtra(DeviceValueConditonActivity.BUNDLE_POSITION, 0);
                String json = data.getStringExtra(TaskSecondDetailActivity.BUNDLE_TASK);
                Log.d(TAG, "json:" + json);
                Map<String, Object> map = JSONObject.parseObject(json, Map.class);
                mMap.putAll(map);
                mRule = data.getStringExtra(DeviceValueConditonActivity.BUNDLE_RULE);
                mRuleType = data.getStringExtra(DeviceValueConditonActivity.BUNDLE_RULE_TYPE);
                mDpId = data.getStringExtra(DeviceValueConditonActivity.BUNDLE_DPID);
                //add by hanzheng 2018-7-16 15:13
                if (mActionBean != null) {
                    SceneActionBean temp = (SceneActionBean) data.getSerializableExtra
                            (BUNDLE_SCENE_ACTION);
                    mActionBean.executorProperty = temp.executorProperty;
                    mActionBean.actionDisplay = temp.actionDisplay;
                    mActionBean.entityId = mDevId;
                } else {
                    mActionBean = (SceneActionBean) data.getSerializableExtra(BUNDLE_SCENE_ACTION);
                    if (mActionBean != null) {
                        mActionBean.entityId = mDevId;
                    }
                }
                //end by hanzheng
                mConditonBean = (SceneConditonBean) data.getSerializableExtra
                        (RESULT_SCENECONDITIONBEAN);
                if (mConditonBean != null) {
                    mConditonBean.entityId = mDevId;
                }
                if (mAdapter != null) {
                    String dpname = data.getStringExtra(TaskSecondDetailActivity.BUNDLE_DPNAME);
                    mAdapter.setSelectedValue(position, dpname);
                }
            }
        }
    }

    protected void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.task_detail_recyclerView);
    }

    protected void initParms() {
        //mBean = (SceneDevBean) getIntent().getSerializableExtra(INTENT_SCENE_BEAN);
        mDevId = getIntent().getStringExtra(INTENT_DEVICEID);
        //Log.d(TAG,"mBean.id" + mBean.devId + "," + mBean.name);
        mActionBean = (SceneActionBean) getIntent().getSerializableExtra(INTENT_SCENE_ACTION);
        mConditonBean = (SceneConditonBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
    }

    protected void initMenu() {
        setTitle(R.string.title_choose_function);
        setDisplayHomeAsUpEnabled();
        setMenu(R.menu.toolbar_choose_function, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.toolbar_choose_function_save:
                        try {
                            save();
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                        break;
                }
                return false;
            }
        });
    }

    protected void save() {
        if (mMap.size() <= 0) {
            Toast.makeText(TaskDetailActivity.this, R.string.alert_choose_function, Toast
                    .LENGTH_SHORT).show();
            return;
        }
        try {
            String deviceName = getIntent().getStringExtra(INTENT_DEVICENAME);
            String json = JSONObject.toJSONString(mMap);
            Intent intent = new Intent();
            intent.putExtra(BUNDLE_DPID, mDpId);
            intent.putExtra(BUNDLE_RULE, mRule);
            intent.putExtra(BUNDLE_RULE_TYPE, mRuleType);
            intent.putExtra(BUNDLE_DATA, json);
            intent.putExtra(BUNDLE_DEVICEID, mDevId);
            intent.putExtra(BUNDLE_DEVICENAME, deviceName);
            intent.putExtra(BUNDLE_TASKDES, mTaskDescription);
            intent.putExtras(getIntent());
            if (mActionBean != null) {
                intent.putExtra(BUNDLE_SCENE_ACTION, mActionBean);
            }
            if (mConditonBean != null) {
                intent.putExtra(RESULT_SCENECONDITIONBEAN, mConditonBean);
            }
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    protected void bindListView(List<TaskListBean> list) {
        mAdapter = new FunctionListItemRecyclerAdapter(list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration
                .VERTICAL_LIST));
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        if (mActionBean != null) {
            try {
                Map<String, Object> property = mActionBean.executorProperty;
                if (property != null) {
                    if (property.size() > 0) {
                        Set<String> keys = property.keySet();
                        String[] array = new String[keys.size()];
                        keys.toArray(array);
                        String key = array[array.length - 1];
                        String value = property.get(key).toString();
                        long k = Long.valueOf(key);
                        if (k > 4) {
                            key = array[0];
                        }
                        mAdapter.setSelectedValue(key, mActionBean.getShortActionDisplay());
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        } else if (mConditonBean != null) {
            try {
                //List<Object> expr = mConditonBean.expr;
                mAdapter.setSelectedValue(mConditonBean.entitySubIds, mConditonBean.getShortDisplay());
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport
                .OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                try {
                    FunctionListItemRecyclerAdapter adapter = (FunctionListItemRecyclerAdapter)
                            recyclerView.getAdapter();
                    TaskListBean bean = adapter.getTaskListBean(position);
                    long dpid = bean.getDpId();
                    if (dpid == 3 || dpid == 4) {
                        Intent intent = new Intent(TaskDetailActivity.this,
                                DeviceValueConditonActivity
                                        .class);
                        intent.putExtra(DeviceValueConditonActivity.INTENT_POSITION, position);
                        intent.putExtra(TaskSecondDetailActivity.INTENT_PARMS_DATA, bean);
                        intent.putExtra(DeviceValueConditonActivity.INTENT_DPID, String.valueOf
                                (dpid));
                        intent.putExtras(getIntent());
                        startActivityForResult(intent, REQUEST_SECOND);
                    } else {
                        Intent intent = new Intent(TaskDetailActivity.this, TaskSecondDetailActivity
                                .class);
                        intent.putExtra(DeviceValueConditonActivity.INTENT_DPID, String.valueOf
                                (dpid));
                        intent.putExtra(TaskSecondDetailActivity.INTENT_POSITION, position);
                        intent.putExtra(TaskSecondDetailActivity.INTENT_PARMS_DATA, bean);
                        //startActivity(intent);
                        intent.putExtras(getIntent());
                        startActivityForResult(intent, REQUEST_SECOND);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }

    protected void getOperationList() {
        if (TextUtils.isEmpty(mDevId)) {
            return;
        }
        TuyaScene.getTuyaSceneManager().getDevOperationList(
                mDevId, //设备id
                new ITuyaDataCallback<List<TaskListBean>>() {
                    @Override
                    public void onSuccess(List<TaskListBean> conditionActionBeans) {
                        if (conditionActionBeans != null) {
                            List<TaskListBean> list = new ArrayList<>();
                            //DpId 1-开关 bool 3-亮度 value 4-冷暖 value 2-模式 enum
                            for (TaskListBean bean : conditionActionBeans) {
                                Log.d(TAG, "bean.name" + bean.getName() + " " + bean.getDpId() +
                                        "," + bean.getTasks().toString());
                                if (bean.getDpId() == 1 || bean.getDpId() == 2 || bean.getDpId()
                                        == 3 || bean.getDpId() == 4) {
                                    list.add(bean);
                                }
                            }
                            bindListView(conditionActionBeans);
                        }
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                    }
                });
    }
}

