package com.tuya.smart.android.demo.adapter;

import android.icu.text.RelativeDateTimeFormatter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.sdk.bean.scene.condition.ConditionListBean;

import java.util.List;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-1-27.
 */

public class ConditionListAdapter extends RecyclerView.Adapter<ConditionListAdapter
        .ConditionListViewHolder> {
    List<ConditionListBean> mList;
    int position_show = -1;
    String value = "";
    public ConditionListAdapter(List<ConditionListBean> list) {
        mList = list;
    }
    public void setConditionDetail(int positon,String v){
        position_show = positon;
        value = v;
        notifyDataSetChanged();
    }
    public int getPosition(ConditionListBean bean) {
//        if (mList.contains(bean)) {
//            return mList.indexOf(bean);
//        }
        for(ConditionListBean item : mList){
            if(item.getType().equals(bean.getType())){
                return mList.indexOf(item);
            }
        }
        return -1;
    }

    public ConditionListBean getData(int position) {
        return mList.get(position);
    }

    @Override
    public ConditionListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ConditionListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R
                .layout.recycler_condition_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ConditionListViewHolder conditionListViewHolder, int i) {
        ConditionListBean item = mList.get(i);
        conditionListViewHolder.txtName.setText(item.getName());
        conditionListViewHolder.txtValue.setVisibility(View.INVISIBLE);
        if(position_show > 0){
            if(i == position_show){
                if(!TextUtils.isEmpty(value)) {
                    conditionListViewHolder.txtValue.setText(value);
                    conditionListViewHolder.txtValue.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ConditionListViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public TextView txtValue;

        public ConditionListViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.tv_condition_name);
            txtValue = (TextView) itemView.findViewById(R.id.tv_condition_value);
        }
    }
}
