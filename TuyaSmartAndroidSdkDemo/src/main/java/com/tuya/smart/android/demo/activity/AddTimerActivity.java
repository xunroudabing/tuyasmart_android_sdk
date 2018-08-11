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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.test.widget.NumberPicker;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaGroup;
import com.tuya.smart.sdk.TuyaTimerManager;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时
 * Created by HanZheng(305058709@qq.com) on 2018-5-19.
 */

public class AddTimerActivity extends BaseActivity {
    public static final String INTENT_TIMER_UPDATE = "INTENT_TIMER_UPDATE";
    static final int REQUEST_ADD_DAY = 100;
    static final String TAG = AddTimerActivity.class.getSimpleName();
    String[] mDays;
    ArrayList<String> rangesMinKey, rangesHourKey;
    TextView txtRepeat, txtSwitch;
    NumberPicker pickerHouer, pickerMin;
    LinearLayout repeatLayout, swtichLayout;
    private boolean mSW = true;
    private String mLoops = "0000000";
    private Timer mTimer;
    private boolean isGroup = false;
    private long mGroupId;
    private String mDpId;
    private String mProductId;
    private TuyaDevice mTuyaDevice;
    private String mDevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        initToolbar();
        initMenu();
        initViews();
        initData();
        bindData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //showdata();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_DAY) {
            String loop = data.getStringExtra(AddTimerDayActivity.RESULT_LOOP);
            String loop_string = data.getStringExtra(AddTimerDayActivity.RESULT_LOOP_STRING);
            Log.d(TAG, "onActivityResult:" + loop + "," + loop_string);
            mLoops = loop;
            txtRepeat.setText(loop_string);
        }
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
                if (mTimer != null) {
                    intent.putExtra(AddTimerDayActivity.INTENT_TIMER_LOOP, mTimer.getLoops());
                }
                startActivityForResult(intent, REQUEST_ADD_DAY);
            }
        });
        swtichLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwtichDialog();
            }
        });
        mDays = new String[]{getString(R.string.txt_day0), getString(R.string.txt_day1),
                getString(R.string.txt_day2), getString(R.string.txt_day3), getString(R.string
                .txt_day4), getString(R.string.txt_day5), getString(R.string.txt_day6)};
    }

    protected void bindData() {
        String json = getIntent().getStringExtra(INTENT_TIMER_UPDATE);
        if (!TextUtils.isEmpty(json)) {
            try {
                mTimer = JSONObject.parseObject(json, Timer.class);
                String time = mTimer.getTime();
                String[] array = time.split(":");
                int hour = rangesHourKey.indexOf(array[0]);
                int min = rangesMinKey.indexOf(array[1]);
                pickerHouer.setValue(hour);
                pickerMin.setValue(min);

                String value = mTimer.getValue();
                JSONObject object = JSONObject.parseObject(value);
                if (object.containsKey("1")) {
                    boolean b = object.getBoolean("1");
                    String s = b ? getString(R.string.swtich_open) : getString(R.string
                            .swtich_close);
                    mSW = b;
                    txtSwitch.setText(s);
                }

                mLoops = mTimer.getLoops();
                String loop_string = convertLoopString(mLoops);
                txtRepeat.setText(loop_string);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }

    protected String convertLoopString(String loop) {
        char[] chars = loop.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '1') {
                sb.append(mDays[i] + ",");
            }
        }
        String ret = "";
        if (sb.length() <= 0) {
            ret = getString(R.string.txt_onlyonce);
        } else {
            ret = sb.substring(0, sb.length() - 1);
        }
        return ret;
    }

    protected void save() {
        try {
            int houer_index = pickerHouer.getValue();
            int min_index = pickerMin.getValue();
            final String time = String.format("%s:%s", rangesHourKey.get(houer_index),
                    rangesMinKey.get
                            (min_index));
            //String loop = CommonConfig.getChooseDay(getApplicationContext());
            String loop = mLoops;
            if (TextUtils.isEmpty(time)) {
                return;
            }
            final TuyaTimerManager timerManager = new TuyaTimerManager();
            //更新
            if (mTimer != null) {
                String devid = mDevId;
                if (isGroup) {
                    devid = String.valueOf(mGroupId);
                }
                JSONObject instruct = new JSONObject();
                JSONObject dpsObj = new JSONObject();
                dpsObj.put("1", mSW);
                instruct.put("time", time);
                instruct.put("dps", dpsObj);
                JSONArray array = new JSONArray();
                array.add(instruct);
                Log.d(TAG, "instruct=" + array.toJSONString());
                timerManager.updateTimerWithTask("timer", loop, devid, mTimer.getTimerId(), array.toJSONString(), new IResultStatusCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "updateTimerStatus success");
                        Toast.makeText(AddTimerActivity.this, "设置定时成功", Toast
                                .LENGTH_SHORT)
                                .show();
                        finish();
                    }

                    @Override
                    public void onError(String s, String s1) {
                        Log.d(TAG, "updateTimerStatus error," + s + "," + s1);
                        Toast.makeText(AddTimerActivity.this, s1, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
            //新建
            else {
                if (!isGroup) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("1", mSW);
                    timerManager.addTimerWithTask("timer", mDevId, loop, map, time, new
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
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("1", mSW);
                    timerManager.addTimerWithTask("timer", String.valueOf(mGroupId), loop, map,
                            time,
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
                                            Toast.makeText(AddTimerActivity.this, msg, Toast
                                                    .LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                }
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
            txtRepeat.setText(R.string.txt_onlyonce);
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
                    mSW = true;
                    txtSwitch.setText("开");
                } else {
                    mSW = false;
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
