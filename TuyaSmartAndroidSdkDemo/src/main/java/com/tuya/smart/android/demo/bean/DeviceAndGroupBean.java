package com.tuya.smart.android.demo.bean;

import com.tuya.smart.sdk.TuyaUser;
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
     * 1 - device, 2 - group
     */
    public int type;

    public boolean getIsOnline() {
        if (type == 1) {
            return device.getIsOnline();
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
        return TuyaUser.getDeviceInstance().getDev(devIds.get
                (0)).getDps();
    }

    public String getProductId(){
        if(type == 1){
            return device.getProductId();
        }
        return group.getProductId();
    }

    public String getDevId(){
        if(type == 1){
            return  device.getDevId();
        }
        List<String> devIds = group.getDevIds();
        if (devIds.size() <= 0) {
            return null;
        }
        return devIds.get(0);
    }
}
