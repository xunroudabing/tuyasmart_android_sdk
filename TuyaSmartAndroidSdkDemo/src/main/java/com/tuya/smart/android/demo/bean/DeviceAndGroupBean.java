package com.tuya.smart.android.demo.bean;

import android.text.TextUtils;

import com.tuya.smart.home.interior.presenter.TuyaSmartDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.List;
import java.util.Map;

/**
 * 设备群组混合类
 * Created by HanZheng(305058709@qq.com) on 2018-5-5.
 */

public class DeviceAndGroupBean {
    public GroupBean group;
    public DeviceBean device;
    /**
     * 1 - device, 2 - group 3 - mesh
     */
    public int type;

    public boolean getIsOnline() {
        if (type == 1) {
            return device.getIsOnline();
        } else if (group != null) {
            if (!TextUtils.isEmpty(group.getMeshId())) {
                List<DeviceBean> list = group.getDeviceBeans();
                if (list != null) {
                    for (DeviceBean bean : list) {
                        if (bean.getIsOnline()) {
                            return true;
                        }
                    }
                }
            }
        }
        return group.getIsOnline();
    }

    public Boolean getIsShare() {
        if (type == 1) {
            return device.getIsShare();
        }
        return group.isShare();
    }

    public String getIconUrl() {
        if (type == 1) {
            return device.getIconUrl();
        }
        return group.getIconUrl();
    }

    public String getName() {
        if (type == 1) {
            return device.getName();
        }
        return group.getName();
    }

    public Map<String, Object> getDps() {
        if (type == 1) {
            return device.getDps();
        }
        List<String> devIds = group.getDevIds();
        if (devIds.size() <= 0) {
            return null;
        }
        return TuyaSmartDevice.getInstance().getDev(devIds.get
                (0)).getDps();
    }

    public String getProductId() {
        if (type == 1) {
            return device.getProductId();
        }
        return group.getProductId();
    }

    public String getDevId() {
        if (type == 1) {
            return device.getDevId();
        }
        List<String> devIds = group.getDevIds();
        List<DeviceBean> beans = group.getDeviceBeans();
        if(devIds != null) {
            if (devIds.size() <= 0) {
                return null;
            }
            return devIds.get(0);
        }else if(beans != null){
            if (beans.size() <= 0) {
                return null;
            }
            return beans.get(0).getDevId();
        }
        return  null;
    }
}
