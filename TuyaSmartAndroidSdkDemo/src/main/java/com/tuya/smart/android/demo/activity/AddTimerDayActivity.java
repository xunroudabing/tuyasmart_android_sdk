package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.widget.DayChooseWidget;

/**
 * 选择日期，周日到周六
 * Created by HanZheng(305058709@qq.com) on 2018-5-19.
 */

public class AddTimerDayActivity extends BaseActivity {
    public static final String RESULT_LOOP = "RESULT_LOOP";
    public static final String RESULT_LOOP_STRING = "RESULT_LOOP_STRING";
    public static final String INTENT_TIMER_LOOP = "INTENT_TIMER_LOOP";
    static final String TAG = AddTimerActivity.class.getSimpleName();
    DayChooseWidget[] array;
    DayChooseWidget day0, day1, day2, day3, day4, day5, day6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_day);
        initToolbar();
        initMenu();
        initViews();
        bindData();
    }

    @Override
    public void onBackPressed() {
        String str = getSelectedValue();
        String txt = getSelectedText();
        Intent intent = new Intent();
        intent.putExtra(RESULT_LOOP, str);
        intent.putExtra(RESULT_LOOP_STRING, txt);
        setResult(RESULT_OK, intent);
        super.onBackPressed();

//        String str = getSelectedValue();
//        Log.d(TAG, "getSelectedValue=" + str);
//        CommonConfig.setChooseDay(getApplicationContext(), str);
//        String txt = getSelectedText();
//        Log.d(TAG, "getSelectedValue=" + txt);
//        CommonConfig.setChooseDayString(getApplicationContext(), txt);
    }

    protected void initMenu() {
        setTitle(R.string.txt_repeat);
        setDisplayHomeAsUpEnabled();
        setDisplayHomeAsUpEnabled(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str = getSelectedValue();
                String txt = getSelectedText();
                Intent intent = new Intent();
                intent.putExtra(RESULT_LOOP, str);
                intent.putExtra(RESULT_LOOP_STRING, txt);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    protected void initViews() {
        day0 = (DayChooseWidget) findViewById(R.id.choose_day_day0);
        day1 = (DayChooseWidget) findViewById(R.id.choose_day_day1);
        day2 = (DayChooseWidget) findViewById(R.id.choose_day_day2);
        day3 = (DayChooseWidget) findViewById(R.id.choose_day_day3);
        day4 = (DayChooseWidget) findViewById(R.id.choose_day_day4);
        day5 = (DayChooseWidget) findViewById(R.id.choose_day_day5);
        day6 = (DayChooseWidget) findViewById(R.id.choose_day_day6);
        array = new DayChooseWidget[]{day0, day1, day2, day3, day4, day5, day6};
        day0.setDay(getString(R.string.txt_day0));
        day1.setDay(getString(R.string.txt_day1));
        day2.setDay(getString(R.string.txt_day2));
        day3.setDay(getString(R.string.txt_day3));
        day4.setDay(getString(R.string.txt_day4));
        day5.setDay(getString(R.string.txt_day5));
        day6.setDay(getString(R.string.txt_day6));
    }

    protected String getSelectedValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String s = "0";
            if (array[i].getChecked()) {
                s = "1";
            }
            builder.append(s);
        }
        return builder.toString();
    }

    protected String getSelectedText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {

            if (array[i].getChecked()) {
                String s = array[i].getDay();
                builder.append(s + ",");
            }

        }
        if (builder.length() <= 0) {
            return getString(R.string.txt_onlyonce);
        }
        return builder.toString();
    }

    protected void bindData() {
        String str = getIntent().getStringExtra(INTENT_TIMER_LOOP);
        if (!TextUtils.isEmpty(str)) {
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '1') {
                    if (i < array.length) {
                        array[i].setChecked(true);
                    }
                }
            }
        }
    }
}
