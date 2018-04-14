package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.FunctionSecondListItemRecyclerAdapter;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.sdk.bean.scene.dev.TaskListBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 选择功能-二级页面
 * Created by HanZheng(305058709@qq.com) on 2018-4-3.
 */

public class TaskSecondDetailActivity extends BaseActivity {
    public static final String BUNDLE_TASK_DES = "BUNDLE_TASK_DES";
    public static final String BUNDLE_DEVICE_NAME = "BUNDLE_DEVICE_NAME";
    public static final String BUNDLE_TASK = "BUNDLE_TASK";
    public static final String INTENT_PARMS_DATA = "INTENT_PARMS_DATA";
    static final String TAG = TaskSecondDetailActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    FunctionSecondListItemRecyclerAdapter mAdapter;
    TaskListBean mBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        initToolbar();
        initViews();
        initParms();
        initMenu();
        bindRecyclerView();
    }

    protected void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.task_detail_recyclerView);
    }

    protected void initParms() {
        mBean = (TaskListBean) getIntent().getSerializableExtra(INTENT_PARMS_DATA);
    }

    protected void initMenu() {
        setTitle(mBean.getName());
        setDisplayHomeAsUpEnabled();

    }

    protected void bindRecyclerView() {
        if (mBean == null) {
            return;
        }
        mAdapter = new FunctionSecondListItemRecyclerAdapter(mBean.getTasks());
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
                    FunctionSecondListItemRecyclerAdapter adapter =
                            (FunctionSecondListItemRecyclerAdapter) recyclerView.getAdapter();
                    Object object = adapter.getDpValue(position);
                    String des = adapter.getDpName(position);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(String.valueOf(mBean.getDpId()), object);
                    final String value = JSONObject.toJSONString(map);
                    String task_des = String.format("%s：%s", mBean.getName(), des);
                    Log.d(TAG, "result=" + value + " task_des=" + task_des);
                    Intent intent = new Intent();
                    intent.putExtra(BUNDLE_TASK_DES, task_des);
                    intent.putExtra(BUNDLE_TASK, value);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }
}
