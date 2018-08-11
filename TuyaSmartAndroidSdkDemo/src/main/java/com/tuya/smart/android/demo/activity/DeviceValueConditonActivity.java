package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.bean.SceneActionBean;
import com.tuya.smart.android.demo.bean.SceneConditonBean;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.ValueRule;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

/**
 * 设备条件
 * Created by HanZheng(305058709@qq.com) on 2018-7-3.
 */

public class DeviceValueConditonActivity extends BaseActivity {
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
    public static final String INTENT_SCENE_ACTION = "INTENT_SCENE_ACTION";
    public static final String INTENT_POSITION = "INTENT_POSITION";
    public static final String INTENT_PARMS_DATA = "INTENT_PARMS_DATA";
    public static final String INTENT_DPID = "INTENT_DPID";//3-亮度 4-冷暖
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    static final String TAG = DeviceValueConditonActivity.class.getSimpleName();
    static final String[] TEXT1_ARRAY = {"小于", "等于", "大于"};
    static final String[] VALUE1_ARRAY = {"<", "==", ">"};
    String[] VALUE2_ARRAY;
    String[] TEXT2_ARRAY;
    NumberPickerView picker1, picker2;
    String mDpId = "3";
    String mCode = "bright_value";
    int mMin = 0;
    int mMax = 255;
    TaskListBean mTaskListBean;
    SceneActionBean mActionBean;
    //用于设备条件
    SceneConditonBean mConditonBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_condition_value);
        initToolbar();
        initMenu();
        initViews();
        initData();
        initPicker();
    }

    protected void initToolbar() {
        if (mToolBar == null) {
            mToolBar = (Toolbar) findViewById(R.id.toolbar_top_view);
            if (mToolBar == null) {
            } else {
                TypedArray a = obtainStyledAttributes(new int[]{
                        R.attr.status_font_color});
                int titleColor = a.getInt(0, Color.WHITE);
                mToolBar.setTitleTextColor(titleColor);
            }
        }
    }

    protected void initViews() {
        picker1 = (NumberPickerView) findViewById(R.id.condition_detail_numberpick1);
        picker2 = (NumberPickerView) findViewById(R.id.condition_detail_numberpick2);
    }

    protected void initData() {
        mConditonBean = (SceneConditonBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
        mActionBean = (SceneActionBean) getIntent().getSerializableExtra(INTENT_SCENE_ACTION);
        mDpId = getIntent().getStringExtra(INTENT_DPID);
        //亮度
        if (mDpId.equals("3")) {
            mCode = "dp3";
            mMin = 25;
            mMax = 255;
        } else if (mDpId.equals("4")) {
            mCode = "dp4";
            mMin = 0;
            mMax = 255;
        }

        mTaskListBean = (TaskListBean) getIntent().getSerializableExtra(INTENT_PARMS_DATA);
        if (mTaskListBean != null) {
            String name = mTaskListBean.getName();
            mToolBar.setTitle(name);
        }
    }

    protected void initPicker() {

        picker1.setDisplayedValues(TEXT1_ARRAY);
        picker1.setMinValue(0);
        picker1.setMaxValue(TEXT1_ARRAY.length - 1);
        picker1.setValue(0);
        int length = mMax - mMin + 1;
        TEXT2_ARRAY = new String[length];
        VALUE2_ARRAY = new String[length];
        for (int i = 0; i < length; i++) {
            int v = i + mMin;
            TEXT2_ARRAY[i] = String.valueOf(v);
            VALUE2_ARRAY[i] = String.valueOf(v);
        }
        picker2.setDisplayedValues(TEXT2_ARRAY);
        picker2.setMinValue(mMin);
        picker2.setMaxValue(TEXT2_ARRAY.length - 1);
        picker2.setValue(mMin);
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
                            value = property.get(key).toString();
                        }
                        if (mDpId.equals(key)) {
                            int index = getIndexByValue(VALUE2_ARRAY, value);
                            if (index > 0) {
                                picker2.setPickedIndexRelativeToRaw(index);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        } else if (mConditonBean != null) {
            try {
                if (mDpId.equals(mConditonBean.entitySubIds)) {
                    List<Object> expr = mConditonBean.expr;
                    String v = expr.get(expr.size() - 1).toString();
                    int index = getIndexByValue(VALUE2_ARRAY, v);
                    if (index > 0) {
                        picker2.setPickedIndexRelativeToRaw(index);
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }

    protected int getIndexByValue(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            String s = array[i];
            if (s.equals(value)) {
                return i;
            }
        }
        return -1;
    }

    protected void initMenu() {
        setMenu(R.menu.toolbar_condition_detail_save, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_condition_detail_save:
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
        String operator = VALUE1_ARRAY[picker1.getPickedIndexRelativeToRaw()];
        String operator_des = TEXT1_ARRAY[picker1.getPickedIndexRelativeToRaw()];
        String value = VALUE2_ARRAY[picker2.getPickedIndexRelativeToRaw()];
        ValueRule valueRule = ValueRule.newInstance(mCode, operator, Integer.valueOf(value));
        String rule_json = JSONObject.toJSONString(valueRule);
        //条件描述
        String task_des = String.format("%s:%s%s", mTaskListBean.getName(), operator_des, value);
        Map<String, Object> map = new HashMap<>();
        map.put(mDpId, Integer.valueOf(value));
        if (mDpId.equals("3")) {
            map.put("2", "white");
        }
        String json = JSONObject.toJSONString(map);
        Intent intent = new Intent();
        intent.putExtra(BUNDLE_DPID, mDpId);
        intent.putExtra(BUNDLE_RULE_TYPE, "value");
        intent.putExtra(BUNDLE_RULE, rule_json);
        intent.putExtra(BUNDLE_POSITION, getIntent().getIntExtra(INTENT_POSITION, 0));
        intent.putExtra(BUNDLE_DPNAME, operator_des + value);
        intent.putExtra(BUNDLE_TASK_DES, task_des);
        intent.putExtra(BUNDLE_TASK, json);
        if (mActionBean == null) {
            mActionBean = new SceneActionBean();
        }
        mActionBean.executorProperty = map;
        mActionBean.actionDisplay = task_des;
        intent.putExtra(BUNDLE_SCENE_ACTION, mActionBean);
        //设备条件绑定
        if (mConditonBean == null) {
            mConditonBean = new SceneConditonBean();
        }
        List<Object> list = new ArrayList<>();
        list.add("dp" + mDpId);
        String compare = "==";
        int index = picker1.getValue();
        if (index == 0) {
            compare = "<";
        } else if (index == 1) {
            compare = "==";
        } else if (index == 2) {
            compare = ">";
        }
        list.add(compare);
        list.add(value);
        mConditonBean.expr = list;
        mConditonBean.entitySubIds = mDpId;
        mConditonBean.exprDisplay = task_des;
        intent.putExtra(RESULT_SCENECONDITIONBEAN, mConditonBean);
        setResult(RESULT_OK, intent);
        finish();
    }
}
