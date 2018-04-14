package com.tuya.smart.android.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.utils.ActivityUtils;

/**
 * 选择设备类型
 */
public class SelectDeviceTypeActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = SelectDeviceTypeActivity.class.getSimpleName();
    LinearLayout btn1, btn2, btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_type);
        initToolbar();
        initMenu();
        initViews();
    }

    protected void initMenu() {
        setDisplayHomeAsUpEnabled();
        setTitle(R.string.title_select_device_type);
    }

    protected void initViews() {
        btn1 = (LinearLayout) findViewById(R.id.select_device_btn1);
        btn2 = (LinearLayout) findViewById(R.id.select_device_btn2);
        btn3 = (LinearLayout) findViewById(R.id.select_device_btn3);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_device_btn1:
                ActivityUtils.gotoActivity(this, AddDeviceTipActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
            case R.id.select_device_btn2:
                ActivityUtils.gotoActivity(this, AddDeviceTipActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
            case R.id.select_device_btn3:
                ActivityUtils.gotoActivity(this, AddDeviceTipActivity.class, ActivityUtils
                        .ANIMATE_SLIDE_TOP_FROM_BOTTOM, true);
                break;
        }
    }
}
