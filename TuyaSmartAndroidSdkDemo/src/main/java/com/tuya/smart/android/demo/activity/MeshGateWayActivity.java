package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.bean.DpLogBean;
import com.tuya.smart.android.demo.presenter.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.view.ICommonDeviceDebugView;

/**
 * 网关
 * Created by HanZheng(305058709@qq.com) on 2018-8-31.
 */

public class MeshGateWayActivity extends BaseActivity implements ICommonDeviceDebugView {
    private CommonDeviceDebugPresenter mPresenter;
    public static final String INTENT_DEVID = "intent_devId";
    public static final String INTNET_TITLE = "intent_title";
    static final String TAG = MeshGateWayActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initToolbar();
        initMenu();
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    protected void initViews(){
        setContentView(R.layout.activity_mesh_gateway);
    }

    protected void initMenu() {
        String title = getIntent().getStringExtra(INTNET_TITLE);
        if(!TextUtils.isEmpty(title)){
            setTitle(title);
        }
        setDisplayHomeAsUpEnabled();
        setMenu(R.menu.toolbar_gateway_menu, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_close:
                        finish();
                        break;
                    case R.id.action_check_update:
                        mPresenter.checkUpdate();
                        break;
                    case R.id.action_resume_factory_reset:
                        mPresenter.resetFactory();
                        break;
                    case R.id.action_unconnect:
                        mPresenter.removeDevice();
                        break;
                }
                return false;
            }
        });
    }

    private void initPresenter() {
        mPresenter = new CommonDeviceDebugPresenter(this, this);
    }

    @Override
    public void updateView(String dpStr) {

    }

    @Override
    public void logError(String error) {

    }

    @Override
    public void logSuccess(String dpStr) {

    }

    @Override
    public void logSuccess(DpLogBean logBean) {

    }

    @Override
    public void logError(DpLogBean logBean) {

    }

    @Override
    public void logDpReport(String dpStr) {

    }

    @Override
    public void deviceRemoved() {

    }

    @Override
    public void deviceOnlineStatusChanged(boolean online) {

    }

    @Override
    public void onNetworkStatusChanged(boolean status) {

    }

    @Override
    public void devInfoUpdate() {

    }

    @Override
    public void updateTitle(String titleName) {

    }
}
