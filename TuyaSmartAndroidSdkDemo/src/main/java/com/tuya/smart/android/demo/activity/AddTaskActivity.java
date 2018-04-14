package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.SceneDeviceAdapter;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.dev.SceneDevBean;

import java.util.List;

/**
 * 添加任务-选择设备
 * Created by HanZheng(305058709@qq.com) on 2018-3-25.
 */

public class AddTaskActivity extends BaseActivity {
    static final int REQUEST_CHOOSE_FUNCTION = 101;
    static final String TAG = AddTaskActivity.class.getSimpleName();
    RelativeLayout mDataEmpty;
    SwipeRefreshLayout mRefreshLayout;
    ListView mListView;
    SceneDeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        initToolbar();
        initMenu();
        initViews();
        getTaskDevList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_FUNCTION) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    protected void initMenu() {
        setTitle(R.string.title_choosedevice);
        setDisplayHomeAsUpEnabled();
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
                    SceneDevBean bean = mAdapter.getItem(position);
                    Intent intent = new Intent(AddTaskActivity.this, TaskDetailActivity.class);
                    intent.putExtra(TaskDetailActivity.INTENT_DEVICEID, bean.devId);
                    intent.putExtra(TaskDetailActivity.INTENT_DEVICENAME, bean.getName());
                    startActivityForResult(intent, REQUEST_CHOOSE_FUNCTION);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }

    protected void getTaskDevList() {
        TuyaScene.getTuyaSceneManager().getTaskDevList(new ITuyaDataCallback<List<SceneDevBean>>() {
            @Override
            public void onSuccess(List<SceneDevBean> sceneDevBeans) {
                if (sceneDevBeans != null && sceneDevBeans.size() > 0) {
                    mAdapter.setData(sceneDevBeans);
                    mDataEmpty.setVisibility(View.GONE);
                } else {
                    mDataEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
            }
        });
    }
}
