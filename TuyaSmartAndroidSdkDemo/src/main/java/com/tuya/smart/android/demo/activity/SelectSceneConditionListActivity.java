package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.ConditionListAdapter;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.condition.ConditionListBean;

import java.util.List;

/**
 * 智能场景-选择条件
 */
public class SelectSceneConditionListActivity extends BaseActivity {
    static final int REQUEST_SELECT_CONDITION = 100;
    static final String TAG = SelectSceneConditionListActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    ConditionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_scene_condition_list);
        initToolbar();
        initMenu();
        initViews();
        getConditionList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_CONDITION) {
            if (resultCode == RESULT_OK) {
                try {
                    String value = data.getStringExtra(ConditionDetailActivity.RESULT_VALUE);
                    Log.d(TAG, "onActivityResult:" + value);
                    ConditionListBean bean = (ConditionListBean) data.getSerializableExtra
                            (ConditionDetailActivity.RESULT_CONDITIONLISTBEAN);
                    int position = adapter.getPosition(bean);
                    Log.d(TAG,"getPosition=" + position);
                    adapter.setConditionDetail(position, value);
                    setResult(RESULT_OK,data);
                    finish();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }


            }
        }
    }

    protected void initMenu() {
        setTitle(R.string.title_select_condition);
        setDisplayHomeAsUpEnabled();
    }

    protected void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.select_scene_recyclerView);
    }

    protected void getConditionList() {

        TuyaScene.getTuyaSceneManager().getConditionList(new ITuyaDataCallback<List<ConditionListBean>>() {
            @Override
            public void onSuccess(List<ConditionListBean> conditionListBeans) {
                Log.d(TAG, "onSuccess:" + conditionListBeans);
                bindRecyclerView(conditionListBeans);
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }

    protected void bindRecyclerView(List<ConditionListBean> list) {
        adapter = new ConditionListAdapter(list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration
                .VERTICAL_LIST));
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport
                .OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                try {
                    ConditionListAdapter adapter = (ConditionListAdapter) recyclerView.getAdapter();
                    ConditionListBean item = adapter.getData(position);
                    Log.d(TAG, "onItemClicked:" + item.getName() + "," + item.getType());
                    Intent intent = new Intent(SelectSceneConditionListActivity.this,
                            ConditionDetailActivity.class);
                    intent.putExtra(ConditionDetailActivity.INTENT_PARMS_CONDITION_BEAN, item);
                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_SELECT_CONDITION);
//                    IProperty property = item.getProperty();
//                    if(item.getType().equals("condition")){
//                        EnumProperty enumProperty = (EnumProperty) property;
//                       Map<Object,String> map = enumProperty.getEnums();
//                       for(Object key : map.keySet()){
//                           Log.d(TAG,"key:" + key + ",value:" + map.get(key));
//                       }
//                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }
}
