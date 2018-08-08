package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.TuyaSmartApp;
import com.tuya.smart.android.demo.adapter.ConditionDetailRecyclerAdapter;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.android.demo.bean.SceneConditonBean;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.sdk.bean.scene.condition.property.EnumProperty;
import com.tuya.smart.sdk.bean.scene.condition.property.ValueProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

/**
 * 条件详情-选择条件二及页面
 */
public class ConditionDetailActivity extends BaseActivity {
    public static final String RESULT_SCENECONDITIONBEAN = "RESULT_SCENECONDITIONBEAN";
    public static final String RESULT_CITYBEAN = "RESULT_CITYBEAN";
    public static final String RESULT_CONDITIONLISTBEAN = "RESULT_CONDITIONLISTBEAN";
    public static final String RESULT_KEY = "RESULT_KEY";
    public static final String RESULT_DES = "RESULT_DES";
    public static final String RESULT_COMPARE = "RESULT_COMPARE";
    public static final String RESULT_VALUE = "RESULT_VALUE";
    public static final String INTENT_SCENEBEAN = "INTENT_SCENEBEAN";
    public static final String INTENT_PARMS_CONDITION_BEAN = "INTENT_PARMS_CONDITION_BEAN";
    public static final int REQUEST_SELECT_CITY = 100;
    static final String[] TEXT1_ARRAY = {"小于", "等于", "大于"};
    static final String[] VALUE1_ARRAY = {"<", "==", ">"};
    static final String TAG = ConditionDetailActivity.class.getSimpleName();
    String[] TEXT2_ARRAY;
    String[] VALUE2_ARRAY;
    PlaceFacadeBean mCityBean;
    LinearLayout cityLayout;
    ConditionListBean mConditionListBean;
    Map<Object, String> mItemChecked;
    RecyclerView mRecyclerView;
    TextView txtCity;
    String city_selected = "";
    ConditionDetailRecyclerAdapter adapter;
    NumberPickerView picker1, picker2;
    LinearLayout pickerLayout;
    SceneConditonBean mConditonBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_detail);
        initToolbar();
        initMenu();
        initViews();
        initPicker();
        initData();
        initCity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_CITY) {
            if (resultCode == RESULT_OK) {
                try {
                    mCityBean = (PlaceFacadeBean) data.getSerializableExtra(SelectCityActivity
                            .REUSLT_PLACE);
                    city_selected = mCityBean.getCity();
                    txtCity.setText(mCityBean.getCity());
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        }
    }

    protected void initViews() {
        pickerLayout = (LinearLayout) findViewById(R.id.condition_detail_pickerLayout);
        picker1 = (NumberPickerView) findViewById(R.id.condition_detail_numberpick1);
        picker2 = (NumberPickerView) findViewById(R.id.condition_detail_numberpick2);
        mRecyclerView = (RecyclerView) findViewById(R.id.condition_detail_recyclerView);
        txtCity = (TextView) findViewById(R.id.condition_detail_txtCity);
        cityLayout = (LinearLayout) findViewById(R.id.condition_detail_cityLayout);
        cityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConditionDetailActivity.this, SelectCityActivity.class);
                intent.putExtra(SelectCityActivity.INTENT_PARMS_CITY_SELECTED, city_selected);
                startActivityForResult(intent, REQUEST_SELECT_CITY);
            }
        });
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

    protected void initData() {
        try {
            mConditonBean = (SceneConditonBean) getIntent().getSerializableExtra(INTENT_SCENEBEAN);
            mConditionListBean = (ConditionListBean) getIntent()
                    .getSerializableExtra(INTENT_PARMS_CONDITION_BEAN);
            Log.d(TAG, "ConditionListBean.type=" + mConditionListBean.getType() + ",name=" +
                    mConditionListBean.getName());
            setTitle(mConditionListBean.getName());
            String type = mConditionListBean.getType();
            if (type.equals("temp")) {
                pickerLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                pickerLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            if (mConditionListBean.getProperty() instanceof EnumProperty) {
                EnumProperty property = (EnumProperty) mConditionListBean.getProperty();
                bindRecycleView(property.getEnums());
            } else if (mConditionListBean.getProperty() instanceof ValueProperty) {
                ValueProperty property = (ValueProperty) mConditionListBean.getProperty();

            }

            //绑定数据
            if (mConditonBean != null) {
                String entitySubIds = mConditonBean.entitySubIds;
                if (entitySubIds.equals(type)) {
                    if (entitySubIds.equals("temp")) {
                        bindPicker(mConditonBean.expr);
                    } else {
                        //枚举类型绑定
                        String value = mConditonBean.expr.get(mConditonBean.expr.size() - 1)
                                .toString();
                        if (adapter != null) {
                            adapter.setChecked(value);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    protected void initCity() {
        final BDLocation location = TuyaSmartApp.getInstance().getLocation();
        if (location != null) {
            txtCity.setText(location.getCity());
            TuyaScene.getTuyaSceneManager().getCityByLatLng(String.valueOf(location.getLongitude
                    ()), String.valueOf(location.getLatitude()), new
                    ITuyaDataCallback<PlaceFacadeBean>() {

                        @Override
                        public void onSuccess(PlaceFacadeBean placeFacadeBean) {
                            Log.d(TAG, "onSuccess:" + placeFacadeBean.getCity() + "," + location
                                    .getCity());
                            placeFacadeBean.setArea(placeFacadeBean.getCity());
                            mCityBean = placeFacadeBean;
                        }

                        @Override
                        public void onError(String s, String s1) {

                        }
                    });
        }
    }

    protected void initPicker() {
        String[] array = {"小于", "等于", "大于"};
        picker1.setDisplayedValues(array);
        picker1.setMinValue(0);
        picker1.setMaxValue(array.length - 1);
        picker1.setValue(0);
        TEXT2_ARRAY = new String[41];
        VALUE2_ARRAY = new String[41];
        for (int i = 0; i < 41; i++) {
            TEXT2_ARRAY[i] = String.valueOf(i) + "℃";
            VALUE2_ARRAY[i] = String.valueOf(i);
        }
        picker2.setDisplayedValues(TEXT2_ARRAY);
        picker2.setMinValue(0);
        picker2.setMaxValue(TEXT2_ARRAY.length - 1);
        picker2.setValue(0);

    }

    protected void bindPicker(List<Object> expr) {
        try {
            if (expr.size() == 3) {
                String compare = expr.get(1).toString();
                String value = expr.get(2).toString();
                int cIndex = -1;
                if (compare.equals("<")) {
                    cIndex = 0;
                } else if (compare.equals("==")) {
                    cIndex = 1;
                } else if (compare.equals(">")) {
                    cIndex = 2;
                }
                if (cIndex > 0) {
                    picker1.setValue(cIndex);
                }
                int v = Integer.valueOf(value);
                if (v < VALUE2_ARRAY.length) {
                    picker2.setValue(v);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    //保存
    protected void save() {
        String type = mConditionListBean.getType();
        Intent data = new Intent();
        if (type.equals("temp")) {
            String compare_value = VALUE1_ARRAY[picker1.getValue()];
            String temp_value = VALUE2_ARRAY[picker2.getValue()];
            String des = String.format("温度%s%s", TEXT1_ARRAY[picker1.getValue()],
                    TEXT2_ARRAY[picker2.getValue()]);
            Log.d(TAG, "save compare=" + compare_value + ",temp_value=" + temp_value);
            data.putExtra(RESULT_KEY, "temp");
            data.putExtra(RESULT_COMPARE, compare_value);
            data.putExtra(RESULT_VALUE, temp_value);
            data.putExtra(RESULT_DES, des);

        } else {
            Map.Entry<Object, String> item = adapter.getChecked();
            Log.d(TAG, "save=" + item.toString());
            data.putExtra(RESULT_KEY, item.getKey().toString());
            data.putExtra(RESULT_VALUE, item.getValue());
            String des = String.format("%s：%s", mConditionListBean.getName(), item.getValue());
            data.putExtra(RESULT_DES, des);
        }

        data.putExtra(RESULT_CONDITIONLISTBEAN, mConditionListBean);
        data.putExtra(RESULT_CITYBEAN, mCityBean);
        //构造绑定数据
        if (mConditonBean == null) {
            mConditonBean = new SceneConditonBean();
        }
        mConditonBean.entitySubIds = type;
        List<Object> list = new ArrayList<>();
        list.add("$" + type);
        String expr = "";
        String compare = "==";
        String value = "";
        if (type.equals("temp")) {
            int index = picker1.getValue();
            if (index == 0) {
                compare = "<";
            } else if (index == 1) {
                compare = "==";
            } else if (index == 2) {
                compare = ">";
            }
            value = VALUE2_ARRAY[picker2.getValue()];
            expr = String.format("%s%s", TEXT1_ARRAY[picker1.getValue()],
                    TEXT2_ARRAY[picker2.getValue()]);
        }else {
            Map.Entry<Object, String> item = adapter.getChecked();
            value = item.getKey().toString();
            expr = adapter.getChecked().getValue();
        }
        list.add(compare);
        list.add(value);
        mConditonBean.exprDisplay = expr;
        mConditonBean.expr = list;
        //构造结束
        data.putExtra(RESULT_SCENECONDITIONBEAN, mConditonBean);
        setResult(RESULT_OK, data);
        finish();
    }

    //设置当前城市
    protected void setCurrentCity() {

    }

    protected void bindRecycleView(Map<Object, String> map) {
        adapter = new ConditionDetailRecyclerAdapter(map);
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
                    ConditionDetailRecyclerAdapter adapter = (ConditionDetailRecyclerAdapter)
                            recyclerView.getAdapter();
                    adapter.setChecked(position);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }
}
