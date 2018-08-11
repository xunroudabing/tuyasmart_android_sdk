package com.tuya.smart.android.demo.bean;

import android.content.Context;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;


import java.util.ArrayList;
import java.util.List;

/**
 * 场景
 * Created by HanZheng(305058709@qq.com) on 2018-1-22.
 */

public class SceneListItemBean extends SceneBean {
    public int icon;
    public static List<SceneListItemBean> addAll(Context context,List<SceneBean> list){
        List<SceneListItemBean> itemList = new ArrayList<>();
        List<String> sceneArray = new ArrayList<>();
        for(SceneBean bean : list){
            sceneArray.add(bean.getName());
            int icon = R.drawable.icon_default_scene2;
            if(bean.getName().equals(context.getString(R.string.scene_item1))){
                icon = R.drawable.ty_index_get_home;
            }else if(bean.getName().equals(context.getString(R.string.scene_item2))){
                icon = R.drawable.ty_index_leave_home;
            }else if(bean.getName().equals(context.getString(R.string.scene_item3))){
                icon = R.drawable.ty_index_get_up;
            }else if(bean.getName().equals(context.getString(R.string.scene_item4))){
                icon = R.drawable.ty_index_rest;
            }
            SceneListItemBean item = new SceneListItemBean();
            item.setName(bean.getName());
            item.setActions(bean.getActions());
            item.setCode(bean.getCode());
            item.setConditions(bean.getConditions());
            item.setId(bean.getId());
            item.icon = icon;
            itemList.add(item);
        }
        int index = 0;
        if(!sceneArray.contains(context.getString(R.string.scene_item1))){
            SceneListItemBean item1 = new SceneListItemBean();
            item1.setName(context.getString(R.string.scene_item1));
            item1.icon = R.drawable.ty_index_get_home;
            itemList.add(index++,item1);
        }
        if(!sceneArray.contains(context.getString(R.string.scene_item2))){
            SceneListItemBean item2 = new SceneListItemBean();
            item2.setName(context.getString(R.string.scene_item2));
            item2.icon = R.drawable.ty_index_leave_home;
            itemList.add(index++,item2);
        }
        if(!sceneArray.contains(context.getString(R.string.scene_item3))){
            SceneListItemBean item3 = new SceneListItemBean();
            item3.setName(context.getString(R.string.scene_item3));
            item3.icon = R.drawable.ty_index_get_up;
            itemList.add(index++,item3);
        }
        if(!sceneArray.contains(context.getString(R.string.scene_item4))){
            SceneListItemBean item4 = new SceneListItemBean();
            item4.setName(context.getString(R.string.scene_item4));
            item4.icon = R.drawable.ty_index_rest;
            itemList.add(index++,item4);
        }
        return  itemList;
    }
    public static List<SceneListItemBean> getList(Context context){
        List<SceneListItemBean> list = new ArrayList<>();
        SceneListItemBean item1 = new SceneListItemBean();
        item1.setName(context.getString(R.string.scene_item1));
        item1.icon = R.drawable.ty_index_get_home;

        SceneListItemBean item2 = new SceneListItemBean();
        item2.setName(context.getString(R.string.scene_item2));
        item2.icon = R.drawable.ty_index_leave_home;

        SceneListItemBean item3 = new SceneListItemBean();
        item3.setName(context.getString(R.string.scene_item3));
        item3.icon = R.drawable.ty_index_get_up;

        SceneListItemBean item4 = new SceneListItemBean();
        item4.setName(context.getString(R.string.scene_item4));
        item4.icon = R.drawable.ty_index_rest;

        list.add(item1);
        list.add(item2);
        list.add(item3);
        list.add(item4);
        return  list;
    }
}
