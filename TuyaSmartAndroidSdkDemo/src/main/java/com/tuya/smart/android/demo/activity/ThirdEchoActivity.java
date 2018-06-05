package com.tuya.smart.android.demo.activity;

import android.os.Bundle;

import com.tuya.smart.android.demo.R;

/**
 * 第三方接入
 * Created by HanZheng(305058709@qq.com) on 2018-6-2.
 */

public class ThirdEchoActivity extends BaseActivity {
    static final String TAG = ThirdEchoActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_guide);
        initToolbar();
        initMenu();
    }
    protected void initMenu() {
        setTitle(R.string.txt_third);
        setDisplayHomeAsUpEnabled();

    }
}
