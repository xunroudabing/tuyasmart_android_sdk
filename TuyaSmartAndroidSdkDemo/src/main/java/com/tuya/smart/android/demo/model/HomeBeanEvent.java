package com.tuya.smart.android.demo.model;

import com.tuya.smart.home.sdk.bean.HomeBean;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-8-28.
 */

public class HomeBeanEvent {
    public HomeBean homeBean;
    public HomeBeanEvent(HomeBean bean){
        homeBean = bean;
    }
}
