package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.adapter.ConditionListAdapter;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.android.demo.bean.SceneConditonBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.bean.scene.condition.property.BoolProperty;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;


import java.util.List;

/**
 * 智能场景-选择条件
 */
public class SelectSceneConditionListActivity extends BaseActivity {
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    static final int REQUEST_SELECT_CONDITION = 100;
    static final int REQUEST_SELECT_DEVICE = 101;
    static final String TAG = SelectSceneConditionListActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    ConditionListAdapter adapter;
    SceneConditonBean mConditonBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_scene_condition_list);
        initToolbar();
        initMenu();
        initViews();
        initParms();
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
                    Log.d(TAG, "getPosition=" + position);
                    adapter.setConditionDetail(position, value);
                    setResult(RESULT_OK, data);
                    finish();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }

            }
        } else if (requestCode == REQUEST_SELECT_DEVICE) {
            if (resultCode == RESULT_OK) {
                try {
                    setResult(RESULT_OK, data);
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

    protected void initParms() {
        mConditonBean = (SceneConditonBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
    }

    protected void getConditionList() {
        TuyaHomeSdk.getSceneManagerInstance().getConditionList(true, new ITuyaResultCallback<List<ConditionListBean>>() {
            @Override
            public void onSuccess(List<ConditionListBean> conditionListBeans) {
                Log.d(TAG, "onSuccess:" + conditionListBeans);
                ConditionListBean deviceBean = new ConditionListBean();
                deviceBean.setName(getString(R.string.device_condition));
                deviceBean.setType("device");
                deviceBean.setProperty(new BoolProperty());
                conditionListBeans.add(deviceBean);
                bindRecyclerView(conditionListBeans);
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }

    protected void bindRecyclerView(List<ConditionListBean> list) {
        adapter = new ConditionListAdapter(list);
        if (mConditonBean != null) {
            String type = mConditonBean.entitySubIds;
            String value = mConditonBean.exprDisplay;
//            if(mConditonBean.expr != null){
//                value = mConditonBean.expr.get(mConditonBean.expr.size() - 1).toString();
//            }

            if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(value)) {
                adapter.setConditionDetail(type, value);
            }
        }
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
                    if (item.getType().equals("device")) {
                        Intent intent = new Intent(SelectSceneConditionListActivity.this,
                                AddTaskActivity.class);
                        intent.putExtra(ConditionDetailActivity.INTENT_PARMS_CONDITION_BEAN, item);
                        intent.putExtras(getIntent());
                        startActivityForResult(intent, REQUEST_SELECT_DEVICE);
                    } else {
                        Intent intent = new Intent(SelectSceneConditionListActivity.this,
                                ConditionDetailActivity.class);
                        intent.putExtra(ConditionDetailActivity.INTENT_PARMS_CONDITION_BEAN, item);
                        intent.putExtras(getIntent());
                        //startActivity(intent);
                        startActivityForResult(intent, REQUEST_SELECT_CONDITION);
                    }
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
