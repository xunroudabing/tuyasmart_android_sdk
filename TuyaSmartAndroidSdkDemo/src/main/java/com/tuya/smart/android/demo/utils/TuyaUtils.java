package com.tuya.smart.android.demo.utils;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-8-19.
 */

public class TuyaUtils {
    /**
     * 获取彩灯
     * @param color
     * @return
     */
    public static Map<String, Object> getLightColor(int color){
        String color_hex = Integer.toHexString(color).toLowerCase().substring(2);
        String value = String.format("%s0000ff%s", color_hex, Integer.toHexString(255));
        Map<String, Object> map = new HashMap<>();
        map.put("2", "colour");
        //map.put("2","scene");
        map.put("5", value);
        return  map;
    }
    /**
     * 获取彩灯
     * @param color
     * @return
     */
    public static Map<String, Object> getMeshLightColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        Map<String, Object> map = new HashMap<>();
        //map.put("3",100);
        map.put("109", "colour");
        //map.put("5", value);
        map.put("101", r);
        map.put("102", g);
        map.put("103", b);
        return map;
    }

    public static Map<String, Object> getMeshTemp(int value){
        Map<String, Object> map = new HashMap<>();
        map.put("104", value);
        map.put("105",255 - value);
        return  map;
    }

    public static Map<String, Object> getMeshSu(int value){
        Map<String, Object> map = new HashMap<>();
        map.put("104", value);
        map.put("105",255 - value);
        return  map;
    }
}
