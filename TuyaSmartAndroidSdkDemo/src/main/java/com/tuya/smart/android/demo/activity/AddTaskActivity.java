package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.SceneDeviceAdapter;
import com.tuya.smart.android.demo.bean.SceneActionBean;
import com.tuya.smart.android.demo.bean.SceneConditonBean;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;


import java.util.List;

/**
 * 添加任务-选择设备
 * Created by HanZheng(305058709@qq.com) on 2018-3-25.
 */

public class AddTaskActivity extends BaseActivity {
    public static final String BUNDLE_SCENE_ACTION = "BUNDLE_SCENE_ACTION";
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    public static final String INTENT_SCENE_ACTION = "INTENT_SCENE_ACTION";
    static final int REQUEST_CHOOSE_FUNCTION = 101;
    static final String TAG = AddTaskActivity.class.getSimpleName();
    SceneActionBean mActionBean;
    RelativeLayout mDataEmpty;
    SwipeRefreshLayout mRefreshLayout;
    ListView mListView;
    SceneDeviceAdapter mAdapter;
    ConditionListBean mConditionListBean;
    //用于设备条件
    SceneConditonBean mConditonBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        initToolbar();
        initMenu();
        initViews();
        initData();
        getTaskDevList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_FUNCTION) {
            if (resultCode == RESULT_OK) {
                if (mConditionListBean != null) {
                    data.putExtra(ConditionDetailActivity.RESULT_CONDITIONLISTBEAN,
                            mConditionListBean);
                }
                //mActionBean = (SceneActionBean) data.getSerializableExtra(BUNDLE_SCENE_ACTION);
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    protected void initMenu() {
        setTitle(R.string.title_choosedevice);
        setDisplayHomeAsUpEnabled();
    }

    protected void initData() {
        if (getIntent().hasExtra(ConditionDetailActivity.INTENT_PARMS_CONDITION_BEAN)) {
            mConditionListBean = (ConditionListBean) getIntent().getSerializableExtra
                    (ConditionDetailActivity.INTENT_PARMS_CONDITION_BEAN);
        }
        mActionBean = (SceneActionBean) getIntent().getSerializableExtra(INTENT_SCENE_ACTION);
        mConditonBean = (SceneConditonBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
        if (mActionBean != null) {
            mAdapter.setDevAction(mActionBean.entityId, mActionBean.actionDisplay);
        }else if(mConditonBean != null){
            mAdapter.setDevAction(mConditonBean.entityId, mConditonBean.exprDisplay);
        }
    }

    protected void initViews() {
        mDataEmpty = (RelativeLayout) findViewById(R.id.list_background_tip);
        mListView = (ListView) findViewById(R.id.lv_device_list);
        mAdapter = new SceneDeviceAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    DeviceBean bean = mAdapter.getItem(position);
                    String json = JSONObject.toJSONString(bean);
                    Intent intent = new Intent(AddTaskActivity.this, TaskDetailActivity.class);
                    intent.putExtra(TaskDetailActivity.INTENT_DEVICEID, bean.devId);
                    intent.putExtra(TaskDetailActivity.INTENT_DEVICENAME, bean.getName());
                    intent.putExtra(TaskDetailActivity.INTENT_SCENE_BEAN, json);
                    intent.putExtras(getIntent());
                    startActivityForResult(intent, REQUEST_CHOOSE_FUNCTION);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }

    protected void getTaskDevList() {
        //hanzheng to do
        long homeId = CommonConfig.getHomeId(this);
        TuyaHomeSdk.getSceneManagerInstance().getTaskDevList(homeId, new ITuyaResultCallback<List<DeviceBean>>() {

            @Override
            public void onSuccess(List<DeviceBean> sceneDevBeans) {
                if (sceneDevBeans != null && sceneDevBeans.size() > 0) {
                    mAdapter.setData(sceneDevBeans);
                    mDataEmpty.setVisibility(View.GONE);
                } else {
                    mDataEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }
}
