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
import com.tuya.smart.android.demo.bean.SceneActionBean;
import com.tuya.smart.android.demo.bean.SceneConditonBean;
import com.tuya.smart.sdk.bean.scene.condition.rule.BoolRule;
import com.tuya.smart.sdk.bean.scene.condition.rule.EnumRule;
import com.tuya.smart.sdk.bean.scene.dev.TaskListBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 选择功能-二级页面
 * Created by HanZheng(305058709@qq.com) on 2018-4-3.
 */

public class TaskSecondDetailActivity extends BaseActivity {
    public static final String RESULT_SCENECONDITIONBEAN = "RESULT_SCENECONDITIONBEAN";
    public static final String BUNDLE_SCENE_ACTION = "BUNDLE_SCENE_ACTION";
    /**
     * value enum bool
     */
    public static final String BUNDLE_RULE_TYPE = "BUNDLE_RULE_TYPE";
    public static final String BUNDLE_RULE = "BUNDLE_RULE";
    public static final String BUNDLE_DPID = "BUNDLE_DPID";
    public static final String BUNDLE_POSITION = "BUNDLE_POSITION";
    public static final String BUNDLE_DPNAME = "BUNDLE_DPNAME";
    public static final String BUNDLE_TASK_DES = "BUNDLE_TASK_DES";
    public static final String BUNDLE_DEVICE_NAME = "BUNDLE_DEVICE_NAME";
    public static final String BUNDLE_TASK = "BUNDLE_TASK";
    public static final String INTENT_DPID = "INTENT_DPID";//1-开关 2-模式
    public static final String INTENT_SCENE_ACTION = "INTENT_SCENE_ACTION";
    public static final String INTENT_POSITION = "INTENT_POSITION";
    public static final String INTENT_PARMS_DATA = "INTENT_PARMS_DATA";
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    static final String TAG = TaskSecondDetailActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    FunctionSecondListItemRecyclerAdapter mAdapter;
    TaskListBean mBean;
    String mDpId = "1";
    SceneActionBean mActionBean;
    //用于设备条件
    SceneConditonBean mConditonBean;

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
        mDpId = getIntent().getStringExtra(INTENT_DPID);
        mActionBean = (SceneActionBean) getIntent().getSerializableExtra(INTENT_SCENE_ACTION);
        mConditonBean = (SceneConditonBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
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
        if (mActionBean != null) {
            try {
                Map<String, Object> property = mActionBean.executorProperty;
                if (property != null) {
                    if (property.size() > 0) {
                        Set<String> keys = property.keySet();
                        String[] array = new String[keys.size()];
                        keys.toArray(array);
                        String key = array[0];
                        String value = property.get(key).toString();
                        mAdapter.setSelected(value);
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        } else if (mConditonBean != null) {
            try {
                List<Object> expr = mConditonBean.expr;
                String v = expr.get(expr.size() - 1).toString();
                mAdapter.setSelected(v);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
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
                    //模式
                    if (mBean.getDpId() == 2L) {
                        String o = object.toString();
                        //柔光
                        if (o.equals("scene_1")) {
                            String s1 = String.format("ffff%d01ffffff", 20);
                            map.put("7", s1);
                        }
                        //缤纷
                        else if (o.equals("scene_2")) {
                            String s2 = String.format
                                    ("ffff%d060000f5f4cb06f100ec25fb10f1000aa699b8", 20);
                            map.put("8", s2);
                        }
                        //炫彩
                        else if (o.equals("scene_3")) {
                            String s3 = String.format("ffff%d01ff0000", 20);
                            map.put("9", s3);
                        }
                        //斑斓
                        else if (o.equals("scene_4")) {
                            String s4 = "ffff30060000f5f4cb06f1000927fb101be1f4e500f5";
                            map.put("10", s4);
                        } else if (o.equals("colour")) {
                            map.put("5", "ff00000000ffff");
                        }
                    } else if (mBean.getDpId() == 3L) {
                        map.put("2", "white");
                    }
                    final String value = JSONObject.toJSONString(map);
                    Log.d(TAG, "value=" + value);
                    String task_des = String.format("%s:%s", mBean.getName(), des);
                    Log.d(TAG, "result=" + value + " task_des=" + task_des);
                    String ruleType = "bool";
                    String rule = "";
                    //开关
                    if (mDpId.equals("1")) {
                        ruleType = "bool";
                        BoolRule br = BoolRule.newInstance("dp1", Boolean.valueOf(object.toString
                                ()));
                        rule = JSONObject.toJSONString(br);
                    }
                    //模式
                    else if (mDpId.equals("2")) {
                        ruleType = "enum";
                        EnumRule er = EnumRule.newInstance("dp2", object.toString());
                        rule = JSONObject.toJSONString(er);
                    }
                    Intent intent = new Intent();
                    intent.putExtra(BUNDLE_DPID, mDpId);
                    intent.putExtra(BUNDLE_RULE, rule);
                    intent.putExtra(BUNDLE_RULE_TYPE, ruleType);
                    intent.putExtra(BUNDLE_POSITION, getIntent().getIntExtra(INTENT_POSITION, 0));
                    intent.putExtra(BUNDLE_DPNAME, des);
                    intent.putExtra(BUNDLE_TASK_DES, task_des);
                    intent.putExtra(BUNDLE_TASK, value);
                    if (mActionBean == null) {
                        mActionBean = new SceneActionBean();
                    }
                    mActionBean.executorProperty = map;
                    mActionBean.actionDisplay = task_des;
                    intent.putExtra(BUNDLE_SCENE_ACTION, mActionBean);
                    if(mConditonBean == null){
                        mConditonBean = new SceneConditonBean();
                    }
                    mConditonBean.entitySubIds = mDpId;
                    List<Object> list = new ArrayList<>();
                    list.add("dp" + mDpId);
                    list.add("==");
                    list.add(object.toString());
                    mConditonBean.expr = list;
                    mConditonBean.exprDisplay = task_des;
                    intent.putExtra(RESULT_SCENECONDITIONBEAN, mConditonBean);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }
}
