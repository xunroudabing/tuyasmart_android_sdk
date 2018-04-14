package com.tuya.smart.android.demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;

import java.util.Map;
import java.util.Set;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-1-28.
 */

public class ConditionDetailRecyclerAdapter extends RecyclerView
        .Adapter<ConditionDetailRecyclerAdapter.ConditionDetailViewHolder> {
    Map<Object, String> mMap;
    int postion_checked = -1;

    public ConditionDetailRecyclerAdapter(Map<Object, String> map) {
        mMap = map;
    }

    public Map.Entry<Object, String> getChecked() {
        if (postion_checked < 0) {
            return null;
        }
        Set<Map.Entry<Object, String>> entrySet = mMap.entrySet();
        Map.Entry<Object, String>[] array = new Map.Entry[]{};
        array = entrySet.toArray(array);
        return array[postion_checked];
    }

    public void setChecked(int position) {
        if (position < mMap.size()) {
            postion_checked = position;
            notifyDataSetChanged();
        }
    }

    @Override
    public ConditionDetailViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ConditionDetailViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate
                (R.layout.recycler_condition_detail_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ConditionDetailViewHolder conditionDetailViewHolder, int i) {
        Set<Object> keys = mMap.keySet();
        Object[] array = new Object[]{};
        array = keys.toArray(array);
        String value = mMap.get(array[i]);
        conditionDetailViewHolder.txtName.setText(value);
        if (i == postion_checked) {
            conditionDetailViewHolder.imgChecked.setVisibility(View.VISIBLE);
        } else {
            conditionDetailViewHolder.imgChecked.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mMap.size();
    }

    public class ConditionDetailViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgChecked;

        public ConditionDetailViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.condition_detail_list_item_txtName);
            imgChecked = (ImageView) itemView.findViewById(R.id.condition_detail_list_item_imgChk);
        }
    }
}
