package com.tuya.smart.android.demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.api.scene.IDeleteSceneCallback;
import com.tuya.smart.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.sdk.bean.scene.SceneBean;
import com.tuya.smart.sdk.bean.scene.SceneCondition;
import com.tuya.smart.sdk.bean.scene.SceneTask;
import com.tuya.smart.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.sdk.bean.scene.condition.property.BoolProperty;
import com.tuya.smart.sdk.bean.scene.condition.property.EnumProperty;
import com.tuya.smart.sdk.bean.scene.condition.property.ValueProperty;
import com.tuya.smart.sdk.bean.scene.condition.rule.BoolRule;
import com.tuya.smart.sdk.bean.scene.condition.rule.EnumRule;
import com.tuya.smart.sdk.bean.scene.condition.rule.ValueRule;
import com.tuya.smart.sdk.bean.scene.dev.SceneDevBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 新建场景
 */
public class AddSceneActivity extends BaseActivity implements View.OnClickListener {
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    static final int REQUEST_ADD_CONDITION = 100;
    static final int REQUEST_ADD_TASK = 101;
    static final String TAG = AddSceneActivity.class.getSimpleName();
    boolean mIsTrue = true;
    ValueRule mValueRule;
    SceneBean mSceneBean;
    String mConditionDevId;
    ConditionListBean mConditionListBean;
    PlaceFacadeBean mPlaceFacadeBean;
    SceneTask mSceneTask;
    SceneDevBean mSceneDevBean;
    SceneCondition mCondition;
    View conditionView, taskView;
    String mEnumValue;
    TextView txtCity, txtCondition;
    TextView txtConditionTip, txtTaskTip;
    EditText editSceneName;
    FrameLayout addCondition, addTask;
    TextView txtDevice, txtTask;
    TextView btnDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scene);
        initToolbar();
        initMenu();
        initViews();
        initParms();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_CONDITION) {
            if (resultCode == RESULT_OK) {
                mEnumValue = data.getStringExtra(ConditionDetailActivity.RESULT_KEY);
                String value = data.getStringExtra(ConditionDetailActivity.RESULT_VALUE);
                Log.d(TAG, "onActivityResult:value=" + value + ",key=" + mEnumValue);
                mConditionListBean = (ConditionListBean) data.getSerializableExtra
                        (ConditionDetailActivity.RESULT_CONDITIONLISTBEAN);

                mPlaceFacadeBean = (PlaceFacadeBean) data.getSerializableExtra
                        (ConditionDetailActivity.RESULT_CITYBEAN);
                String des = data.getStringExtra(ConditionDetailActivity.RESULT_DES);
                Log.d(TAG, "bean:" + mConditionListBean.getType() + "," + mConditionListBean
                        .getName());
                String city_name = mPlaceFacadeBean == null ? "" : mPlaceFacadeBean.getCity();
                String str = String.format("%s：%s", mConditionListBean.getName(), value);
                showCondition(des, city_name);
                if (mConditionListBean.getProperty() instanceof ValueProperty) {
                    String type = mConditionListBean.getType();
                    String operator = data.getStringExtra(ConditionDetailActivity.RESULT_COMPARE);
                    mValueRule = ValueRule.newInstance(type, operator, Integer.valueOf(value));
                } else if (mConditionListBean.getProperty() instanceof BoolProperty) {
                    String json = data.getStringExtra(TaskDetailActivity.BUNDLE_DATA);
                    mConditionDevId = data.getStringExtra(TaskDetailActivity.BUNDLE_DEVICEID);
                    String devicename = data.getStringExtra(TaskDetailActivity.BUNDLE_DEVICENAME);
                    String taskdes = data.getStringExtra(TaskDetailActivity.BUNDLE_TASKDES);
//                    mSceneDevBean = (SceneDevBean) data.getSerializableExtra(TaskDetailActivity
//                            .INTENT_SCENE_BEAN);
                    String devbean_json = data.getStringExtra(TaskDetailActivity.INTENT_SCENE_BEAN);
                    Log.d(TAG, "mSceneDevBean=" + devbean_json);
                    mSceneDevBean = JSONObject.parseObject(devbean_json, SceneDevBean.class);
                    showCondition(taskdes, devicename);
                    if (!TextUtils.isEmpty(json)) {
                        HashMap<String, Object> map = JSONObject.parseObject(json, HashMap.class);
                        if (map.containsKey("1")) {
                            mIsTrue = (boolean) map.get("1");
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_ADD_TASK) {
            if (resultCode == RESULT_OK) {
                String json = data.getStringExtra(TaskDetailActivity.BUNDLE_DATA);
                String devid = data.getStringExtra(TaskDetailActivity.BUNDLE_DEVICEID);
                String devicename = data.getStringExtra(TaskDetailActivity.BUNDLE_DEVICENAME);
                String taskdes = data.getStringExtra(TaskDetailActivity.BUNDLE_TASKDES);
                showTask(devicename, taskdes);
                Log.d(TAG, "json:" + json);
                if (!TextUtils.isEmpty(json) && !TextUtils.isEmpty(devid)) {
                    HashMap<String, Object> map = JSONObject.parseObject(json, HashMap.class);
                    mSceneTask = SceneTask.createDpTask(devid, map);
                }
            }
        }
    }

    protected void initMenu() {
        setTitle(R.string.add_scene);
        setDisplayHomeAsUpEnabled();
        setMenu(R.menu.toolbar_save_scene, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_scene_save) {
                    String sceneId = null;
                    if (mSceneBean != null) {
                        sceneId = mSceneBean.getId();
                    }
                    if (!TextUtils.isEmpty(sceneId)) {
                        modifyScene();
                    } else {
                        createScene();
                    }
                }
                return false;
            }
        });
    }

    protected void initViews() {
        btnDel = (TextView) findViewById(R.id.tv_scene_delete);
        txtDevice = (TextView) findViewById(R.id.scene_task_txtDevice);
        txtTask = (TextView) findViewById(R.id.scene_task_txtTask);
        taskView = findViewById(R.id.scene_task_include);
        editSceneName = (EditText) findViewById(R.id.et_scene_name);
        conditionView = findViewById(R.id.scene_condition_include);
        txtCity = (TextView) findViewById(R.id.scene_condition_txtCity);
        txtCondition = (TextView) findViewById(R.id.scene_condition_txtCondition);
        txtTaskTip = (TextView) findViewById(R.id.tv_add_task_tip);
        txtConditionTip = (TextView) findViewById(R.id.tv_add_condition_tip);
        addCondition = (FrameLayout) findViewById(R.id.fl_add_condition);
        addCondition.setOnClickListener(this);
        addTask = (FrameLayout) findViewById(R.id.fl_add_task);
        addTask.setOnClickListener(this);
        btnDel.setOnClickListener(this);
    }

    protected void initParms() {
        mSceneBean = (SceneBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
        if (mSceneBean != null) {
            List<SceneTask> tasks = mSceneBean.getActions();
            List<SceneCondition> conditions = mSceneBean.getConditions();
            if (tasks != null && tasks.size() > 0) {
                mSceneTask = tasks.get(0);
                showTask(mSceneTask.getEntityName(), mSceneTask.getActionDisplay());
            }
            if (conditions != null && conditions.size() > 0) {
                mCondition = conditions.get(0);
                showCondition(mCondition.getExprDisplay(), mCondition.getEntityName());
            }
            editSceneName.setText(mSceneBean.getName());
        }
    }

    //删除场景
    protected void deleteScene() {
        if (mSceneBean != null) {
            DialogUtil.simpleConfirmDialog(AddSceneActivity.this, getString(R.string
                    .dialog_title_delScene), getString(R.string.dialog_delScene_content), new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                TuyaScene.getTuyaSmartScene(mSceneBean.getId()).deleteScene(new IDeleteSceneCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "del scene success");
                                        setResult(RESULT_OK);
                                        finish();
                                    }

                                    @Override
                                    public void onError(String s, String s1) {
                                        Log.d(TAG, "del scene error:" + s1);
                                    }
                                });
                            } else if (which == DialogInterface.BUTTON_NEGATIVE) {

                            }
                        }
                    });

        }
    }

    protected void showTask(String device, String task) {
        txtDevice.setText(device);
        txtTask.setText(task);
        taskView.setVisibility(View.VISIBLE);
        txtTaskTip.setVisibility(View.GONE);
    }

    protected void showCondition(String value, String city) {
        txtCondition.setText(value);
        txtCity.setText(city);
        conditionView.setVisibility(View.VISIBLE);
        txtConditionTip.setVisibility(View.GONE);
    }

    //修改场景
    protected void modifyScene() {
        String sceneName = editSceneName.getText().toString();
        if (TextUtils.isEmpty(sceneName)) {
            Toast.makeText(AddSceneActivity.this, R.string.alert_input_scene_name, Toast
                    .LENGTH_SHORT).show();
            return;
        }
        if (mConditionListBean != null) {
            if (mConditionListBean.getProperty() instanceof EnumProperty) {
                EnumProperty weatherProperty = (EnumProperty) mConditionListBean.getProperty();
                HashMap<Object, String> enums = weatherProperty.getEnums();

                EnumRule enumRule = EnumRule.newInstance(
                        mConditionListBean.getType(),  //类别
                        mEnumValue);        //选定的枚举值
                mCondition = SceneCondition.createWeatherCondition(
                        mPlaceFacadeBean,    //城市
                        mConditionListBean.getType(),        //类别
                        enumRule            //规则
                );
            } else if (mConditionListBean.getProperty() instanceof ValueProperty) {
                mCondition = SceneCondition.createWeatherCondition(mPlaceFacadeBean,
                        mConditionListBean.getType(), mValueRule);
            } else {


            }
        }

        mSceneBean.setName(sceneName);  //更改场景名称
        mSceneBean.setConditions(Collections.singletonList(mCondition)); //更改场景条件
        mSceneBean.setActions(Collections.singletonList(mSceneTask)); //更改场景任务
        String sceneId = mSceneBean.getId();  //获取场景id以初始化TuyaSmartScene类
        TuyaScene.getTuyaSmartScene(sceneId).modifyScene(
                mSceneBean,  //修改后的场景数据类
                new ITuyaDataCallback<SceneBean>() {
                    @Override
                    public void onSuccess(SceneBean sceneBean) {
                        Log.d(TAG, "Modify Scene Success");
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                });
    }

    protected void createScene() {
        String sceneName = editSceneName.getText().toString();
        if (TextUtils.isEmpty(sceneName)) {
            Toast.makeText(AddSceneActivity.this, R.string.alert_input_scene_name, Toast
                    .LENGTH_SHORT).show();
            return;
        } else if (mSceneTask == null) {
            Toast.makeText(AddSceneActivity.this, R.string.alert_input_task, Toast
                    .LENGTH_SHORT).show();
            return;
        }
        SceneCondition condition = null;
        if (mConditionListBean != null && mSceneTask != null) {
            if (mConditionListBean.getProperty() instanceof EnumProperty) {
                EnumProperty weatherProperty = (EnumProperty) mConditionListBean.getProperty();
                HashMap<Object, String> enums = weatherProperty.getEnums();

                EnumRule enumRule = EnumRule.newInstance(
                        mConditionListBean.getType(),  //类别
                        mEnumValue);        //选定的枚举值
                condition = SceneCondition.createWeatherCondition(
                        mPlaceFacadeBean,    //城市
                        mConditionListBean.getType(),        //类别
                        enumRule            //规则
                );
            } else if (mConditionListBean.getProperty() instanceof ValueProperty) {
                condition = SceneCondition.createWeatherCondition(mPlaceFacadeBean,
                        mConditionListBean.getType(), mValueRule);
            } else if (mConditionListBean.getProperty() instanceof BoolProperty) {
                //为了避免循环控制，同一台设备无法同时作为条件和任务。
                if (mSceneTask.getEntityId().equals(mConditionDevId)) {
                    Toast.makeText(AddSceneActivity.this, R.string.alert_condition_task_equal,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                BoolProperty devProperty = (BoolProperty) mConditionListBean.getProperty();
                HashMap<Boolean, String> boolMap = devProperty.getBoolMap();
                BoolRule boolRule = BoolRule.newInstance(
                        "dp1",    //"dp" + dpId
                        mIsTrue    //触发条件的bool
                );
                condition = SceneCondition.createDevCondition(
                        mSceneDevBean,    //设备
                        "1",        //dpId
                        boolRule    //规则
                );
            }
        }

        TuyaScene.getTuyaSceneManager().createScene(sceneName, condition, Collections
                .singletonList(mSceneTask), new
                ITuyaDataCallback<SceneBean>() {
                    @Override
                    public void onSuccess(SceneBean sceneBean) {
                        Log.d(TAG, "create scene success");
                        Toast.makeText(AddSceneActivity.this, R.string
                                .alert_create_scene_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String s, String s1) {
                        Log.e(TAG, "create scene fail," + s + "," + s1);
                    }
                });

    }

    protected void getConditionList() {
        TuyaScene.getTuyaSceneManager().getConditionList(new ITuyaDataCallback<List<ConditionListBean>>() {
            @Override
            public void onSuccess(List<ConditionListBean> conditionListBeans) {
                for (ConditionListBean bean : conditionListBeans) {
                    Log.d(TAG, "ConditionListBean:" + bean.getName() + "," + bean.getType());
                }
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_add_condition:
                Intent intent = new Intent(this, SelectSceneConditionListActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_ADD_CONDITION);
                break;
            case R.id.fl_add_task:
                Intent intent1 = new Intent(this, AddTaskActivity.class);
                //startActivity(intent);
                startActivityForResult(intent1, REQUEST_ADD_TASK);
                break;
            case R.id.tv_scene_delete:
                try {
                    deleteScene();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
        }
    }
}
