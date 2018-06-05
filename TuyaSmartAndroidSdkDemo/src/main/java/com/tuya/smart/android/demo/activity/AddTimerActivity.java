package com.tuya.smart.android.demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.test.widget.NumberPicker;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaGroup;
import com.tuya.smart.sdk.TuyaTimerManager;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时
 * Created by HanZheng(305058709@qq.com) on 2018-5-19.
 */

public class AddTimerActivity extends BaseActivity {
    static final String TAG = AddTimerActivity.class.getSimpleName();
    ArrayList<String> rangesMinKey, rangesHourKey;
    TextView txtRepeat, txtSwitch;
    NumberPicker pickerHouer, pickerMin;
    LinearLayout repeatLayout, swtichLayout;
    private boolean isGroup = false;
    private long mGroupId;
    private String mDpId;
    private String mProductId;
    private TuyaDevice mTuyaDevice;
    private ITuyaGroup mTuyaGroup;
    private String mDevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        initToolbar();
        initMenu();
        initViews();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showdata();
    }

    protected void initMenu() {
        setTitle(R.string.title_add_timer);
        setDisplayHomeAsUpEnabled();
        setMenu(R.menu.toolbar_add_timer, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_add_timer_save) {
                    save();
                }
                return false;
            }
        });
    }

    protected void initViews() {
        isGroup = getIntent().getBooleanExtra(DeviceColorPickActivity.INTENT_ISGROUP, false);
        mGroupId = getIntent().getLongExtra(DeviceColorPickActivity.INTENT_GROUPID, 0);
        mDevId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_DEVID);
        mDpId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_DPID);
        mProductId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_PRODUCTID);
        if (isGroup && mGroupId != 0L) {
            mTuyaGroup = TuyaGroup.newGroupInstance(mGroupId);
        }
        mTuyaDevice = new TuyaDevice(mDevId);
        txtRepeat = (TextView) findViewById(R.id.timer_txtRepeat);
        txtSwitch = (TextView) findViewById(R.id.timer_txtSwitch);
        pickerHouer = (NumberPicker) findViewById(R.id.timer_pickerHour);
        pickerMin = (NumberPicker) findViewById(R.id.timer_pickerMin);
        repeatLayout = (LinearLayout) findViewById(R.id.layout_timer_repeat);
        swtichLayout = (LinearLayout) findViewById(R.id.layout_timer_switch);
        repeatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddTimerActivity.this, AddTimerDayActivity.class);
                startActivity(intent);
            }
        });
        swtichLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwtichDialog();
            }
        });
    }

    protected void save() {
        try {
            int houer_index = pickerHouer.getValue();
            int min_index = pickerMin.getValue();
            final String time = String.format("%s:%s", rangesHourKey.get(houer_index),
                    rangesMinKey.get
                            (min_index));
            CommonConfig.setTimer(getApplicationContext(), time);
            boolean sw = CommonConfig.getSwitch(getApplicationContext());
            String loop = CommonConfig.getChooseDay(getApplicationContext());
            if (TextUtils.isEmpty(time)) {
                return;
            }
            if (!isGroup) {
                final TuyaTimerManager timerManager = new TuyaTimerManager();
                Map<String, Object> map = new HashMap<>();
                map.put("1", sw);

                timerManager.addTimerWithTask("timer", mDevId, loop, map, time, new
                        IResultStatusCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "设置定时器成功：" + time);
                                Toast.makeText(AddTimerActivity.this, "设置定时成功", Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            }

                            @Override
                            public void onError(String s, String s1) {
                                Log.e(TAG, s + "," + s1);
                                String msg = "设置定时器失败," + s + "," + s1;
                                Toast.makeText(AddTimerActivity.this, msg, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
            } else {
                final TuyaTimerManager timerManager = new TuyaTimerManager();
                Map<String, Object> map = new HashMap<>();
                map.put("1", sw);

                timerManager.addTimerWithTask("timer", String.valueOf(mGroupId), loop, map, time,
                        new
                                IResultStatusCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "设置定时器成功：" + time);
                                        Toast.makeText(AddTimerActivity.this, "设置定时成功", Toast
                                                .LENGTH_SHORT)
                                                .show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(String s, String s1) {
                                        Log.e(TAG, s + "," + s1);
                                        String msg = "设置定时器失败," + s + "," + s1;
                                        Toast.makeText(AddTimerActivity.this, msg, Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    protected void initData() {
        rangesMinKey = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            rangesMinKey.add(String.format("%02d", i));
        }
        String[] value = new String[rangesMinKey.size()];
        value = rangesMinKey.toArray(value);
        pickerMin.setDisplayedValues(value);
        pickerMin.setMaxValue(value.length - 1);
        pickerMin.setValue(0);
        pickerMin.setWrapSelectorWheel(true);

        rangesHourKey = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            rangesHourKey.add(String.format("%02d", i));
        }
        value = new String[rangesHourKey.size()];
        value = rangesHourKey.toArray(value);
        pickerHouer.setDisplayedValues(value);
        pickerHouer.setMaxValue(value.length - 1);
        pickerHouer.setValue(0);
        pickerHouer.setWrapSelectorWheel(true);


    }

    protected void showdata() {
        String str = CommonConfig.getChooseDay(getApplicationContext());
        String txt = CommonConfig.getChooseDayString(getApplicationContext());
        if (str.equals("0000000")) {
            txtRepeat.setText("仅限一次");
        } else {
            txtRepeat.setText(txt);
        }
        boolean enable = CommonConfig.getSwitch(getApplicationContext());
        if (enable) {
            txtSwitch.setText("开");
        } else {
            txtSwitch.setText("关");
        }
    }

    protected void showSwtichDialog() {
        String[] items = {"开", "关"};
        DialogUtil.singleChoiceDialog(AddTimerActivity.this, "开关", items, 0, new DialogUtil
                .SingleChoiceDialogInterface() {

            @Override
            public void onPositive(DialogInterface dialog, int checkedItem) {
                if (checkedItem == 0) {
                    CommonConfig.setSwitch(getApplicationContext(), true);
                    txtSwitch.setText("开");
                } else {
                    CommonConfig.setSwitch(getApplicationContext(), false);
                    txtSwitch.setText("关");
                }
                dialog.dismiss();
            }

            @Override
            public void onNegative(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }
}
