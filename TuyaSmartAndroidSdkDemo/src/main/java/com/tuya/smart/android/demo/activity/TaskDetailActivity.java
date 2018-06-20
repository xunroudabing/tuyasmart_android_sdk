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
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.dev.SceneDevBean;
import com.tuya.smart.sdk.bean.scene.dev.TaskListBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择功能
 * Created by HanZheng(305058709@qq.com) on 2018-3-27.
 */

public class TaskDetailActivity extends BaseActivity {
    public static final int REQUEST_SECOND = 101;
    public static final String BUNDLE_TASKDES = "BUNDLE_TASKDES";
    public static final String BUNDLE_DEVICEID = "BUNDLE_DEVICEID";
    public static final String BUNDLE_DEVICENAME = "BUNDLE_DEVICENAME";
    public static final String BUNDLE_DATA = "BUNDLE_DATA";
    public static final String INTENT_SCENE_BEAN = "INTENT_SCENE_BEAN";
    public static final String INTENT_DEVICEID = "INTENT_DEVICEID";
    public static final String INTENT_DEVICENAME = "INTENT_DEVICENAME";
    static final String TAG = TaskDetailActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    FunctionListItemRecyclerAdapter mAdapter;
    SceneDevBean mBean;
    String mDevId;
    String mTaskDescription;
    Map<String, Object> mMap = new HashMap<String, Object>();

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
                String json = data.getStringExtra(TaskSecondDetailActivity.BUNDLE_TASK);
                Log.d(TAG, "json:" + json);
                Map<String, Object> map = JSONObject.parseObject(json, Map.class);
                mMap.putAll(map);
                if (mAdapter != null) {
                    String dpname = data.getStringExtra(TaskSecondDetailActivity.BUNDLE_DPNAME);
                    mAdapter.setSelectedValue(dpname);
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
            intent.putExtra(BUNDLE_DATA, json);
            intent.putExtra(BUNDLE_DEVICEID, mDevId);
            intent.putExtra(BUNDLE_DEVICENAME, deviceName);
            intent.putExtra(BUNDLE_TASKDES, mTaskDescription);
            intent.putExtras(getIntent());
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
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport
                .OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                try {
                    FunctionListItemRecyclerAdapter adapter = (FunctionListItemRecyclerAdapter)
                            recyclerView.getAdapter();
                    TaskListBean bean = adapter.getTaskListBean(position);
                    Intent intent = new Intent(TaskDetailActivity.this, TaskSecondDetailActivity
                            .class);
                    intent.putExtra(TaskSecondDetailActivity.INTENT_PARMS_DATA, bean);
                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_SECOND);
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
                        List<TaskListBean> list = conditionActionBeans;
                        for (TaskListBean bean : list) {
                            Log.d(TAG, "bean.name" + bean.getName() + " " + bean.getTasks()
                                    .toString());
                        }
                        bindListView(list);
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                    }
                });
    }
}

