package com.tuya.smart.android.demo.debug;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.tuya.smart.android.demo.activity.DeviceColorPickActivity;
import com.tuya.smart.android.demo.activity.DeviceModePickActivity;

/**
 * Created by HanZheng(305058709@qq.com) on 2017-12-9.
 */

public class MainListActivity extends ListActivity {
    static final String[] NAMES = {"DeviceModePickActivity","DeviceColorPickActivity"};
    static final Class<?>[] CLASS = {DeviceModePickActivity.class, DeviceColorPickActivity.class};
    static final String TAG = MainListActivity.class.getSimpleName();
    BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bind();
    }

    protected void Bind() {
        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, NAMES);

        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(), CLASS[arg2]);
                startActivity(intent);
            }
        });
    }
}
