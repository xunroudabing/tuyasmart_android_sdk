package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.TuyaSmartApp;
import com.tuya.smart.android.demo.adapter.CityListRecyclerAdapter;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.PlaceFacadeBean;

import java.util.List;

/**
 * 选择城市
 */
public class SelectCityActivity extends BaseActivity {
    String city_selected = "";
    public static final String INTENT_PARMS_CITY_SELECTED = "INTENT_PARMS_CITY_SELECTED";
    public static final String REUSLT_PLACE = "REUSLT_PLACE";
    static final String TAG = SelectCityActivity.class.getSimpleName();
    TextView txtLocationCity;
    RecyclerView mRecyclerView;
    CityListRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        initToolbar();
        initMenu();
        initViews();
        initData();
        initCity();
        requestCityList();
    }

    protected void initMenu() {
        setTitle(R.string.title_select_city);
        setDisplayHomeAsUpEnabled();
    }
    protected void initData(){
        city_selected = getIntent().getStringExtra(INTENT_PARMS_CITY_SELECTED);
    }
    protected void initViews() {
        txtLocationCity = (TextView) findViewById(R.id.select_city_txtCurrentCity);
        mRecyclerView = (RecyclerView) findViewById(R.id.select_city_recyclerView);
    }
    protected void initCity() {
        final BDLocation location = TuyaSmartApp.getInstance().getLocation();
        if (location != null) {
            txtLocationCity.setText(location.getCity());
            TuyaScene.getTuyaSceneManager().getCityByLatLng(String.valueOf(location.getLongitude
                    ()), String.valueOf(location.getLatitude()), new
                    ITuyaDataCallback<PlaceFacadeBean>() {

                        @Override
                        public void onSuccess(PlaceFacadeBean placeFacadeBean) {
                            Log.d(TAG, "onSuccess:" + placeFacadeBean.getCity() + "," + location.getCity());

                        }

                        @Override
                        public void onError(String s, String s1) {

                        }
                    });
        }
    }
    protected void bindCityList(List<PlaceFacadeBean> list) {
        adapter = new CityListRecyclerAdapter(list);
        adapter.setChecked(city_selected);
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
                    CityListRecyclerAdapter adapter = (CityListRecyclerAdapter) recyclerView
                            .getAdapter();
                    adapter.setChecked(position);
                    PlaceFacadeBean bean = adapter.getChecked();
                    if (bean != null) {
                        Intent intent = new Intent();
                        intent.putExtra(REUSLT_PLACE, bean);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }

    protected void requestCityList() {
        TuyaScene.getTuyaSceneManager().getCityListByCountryCode("CN", new
                ITuyaDataCallback<List<PlaceFacadeBean>>() {
            @Override
            public void onSuccess(List<PlaceFacadeBean> placeFacadeBeans) {
                bindCityList(placeFacadeBeans);
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }
}
