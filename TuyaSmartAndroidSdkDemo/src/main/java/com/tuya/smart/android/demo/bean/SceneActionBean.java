package com.tuya.smart.android.demo.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * 用于场景任务数据保存
 * Created by HanZheng(305058709@qq.com) on 2018-7-13.
 */
public class SceneActionBean implements Serializable {
    public String id;
    public String entityId;
    public Map<String, Object> executorProperty;
    public String actionDisplay;

    public String getShortActionDisplay() {
        if (actionDisplay.contains("/")) {
            String[] temp = actionDisplay.split("/");
            if (temp.length > 0) {
                String[] array = temp[1].split(":");
                if (array.length > 1) {
                    String ret = array[1];
                    if(!TextUtils.isEmpty(ret) && !ret.equals(" ")) {
                        return array[1];
                    }else {
                        String[] a = temp[0].split(":");
                        if(a.length > 1){
                            return a[1];
                        }
                    }
                }
            }
        } else {
            String[] array = actionDisplay.split(":");
            if (array.length > 0) {
                return array[1];
            }
        }
        return "";
    }
}
