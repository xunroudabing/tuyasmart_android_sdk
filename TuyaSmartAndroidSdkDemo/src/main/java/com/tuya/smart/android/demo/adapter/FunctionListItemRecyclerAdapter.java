package com.tuya.smart.android.demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.sdk.bean.scene.dev.TaskListBean;

import java.util.List;

/**
 * 选择功能列表
 * Created by HanZheng(305058709@qq.com) on 2018-4-1.
 */

public class FunctionListItemRecyclerAdapter extends RecyclerView
        .Adapter<FunctionListItemRecyclerAdapter.FunctionListItemViewHolder> {
    List<TaskListBean> mData;
    String mSelectedValue;

    public FunctionListItemRecyclerAdapter(List<TaskListBean> list) {
        mData = list;
    }

    public void setSelectedValue(String value) {
        mSelectedValue = value;
        notifyDataSetChanged();
    }

    @Override
    public FunctionListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new FunctionListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate
                (R.layout.list_function_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(FunctionListItemViewHolder functionListItemViewHolder, int i) {
        TaskListBean bean = mData.get(i);
        functionListItemViewHolder.txtView.setText(bean.getName());
        if (!TextUtils.isEmpty(mSelectedValue)) {
            functionListItemViewHolder.txtSelected.setText(mSelectedValue);
            functionListItemViewHolder.txtSelected.setVisibility(View.VISIBLE);
        }

    }

    public TaskListBean getTaskListBean(int i) {
        return mData.get(i);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class FunctionListItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtView;
        TextView txtSelected;

        public FunctionListItemViewHolder(View itemView) {
            super(itemView);
            txtView = (TextView) itemView.findViewById(R.id.tv_function_name);
            txtSelected = (TextView) itemView.findViewById(R.id.tv_function_selected);
        }
    }
}
