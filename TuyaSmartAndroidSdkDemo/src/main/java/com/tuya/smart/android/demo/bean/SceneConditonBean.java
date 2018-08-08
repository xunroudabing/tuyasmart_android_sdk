package com.tuya.smart.android.demo.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 用于场景条件数据保存
 * Created by HanZheng(305058709@qq.com) on 2018-7-11.
 */

public class SceneConditonBean implements Serializable {
    public String entitySubIds;
    public List<Object> expr;
    public String exprDisplay;
    public String entityId;

    public String getShortDisplay() {
        if(exprDisplay.contains(":")) {
            String[] temp = exprDisplay.split(":");
            if (temp.length > 1) {
                return temp[1];
            }
        }
        return exprDisplay;
    }
}
