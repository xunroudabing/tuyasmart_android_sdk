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
 * Created by HanZheng(305058709@qq.com) on 2018-4-3.
 */

public class FunctionSecondListItemRecyclerAdapter extends RecyclerView
        .Adapter<FunctionSecondListItemRecyclerAdapter.FunctionSecondListItemViewHolder> {
    Map<Object, String> mData;
    String mValue;
    public FunctionSecondListItemRecyclerAdapter(Map<Object, String> map) {
        mData = map;
    }

    @Override
    public FunctionSecondListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new FunctionSecondListItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_function_second_item, viewGroup, false));
    }
    public void setSelected(String value){
        mValue = value;
        notifyDataSetChanged();
    }
    public String getDpName(int i){
        Set<Object> sets = mData.keySet();
        Object[] array = new Object[sets.size()];
        array = sets.toArray(array);
        for (int j = 0; j < array.length; j++) {
            if (j == i) {
                Object key = array[j];
                String value = mData.get(key);
                return value;
            }
        }
        return null;
    }
    public Object getDpValue(int i) {
        Set<Object> sets = mData.keySet();
        Object[] array = new Object[sets.size()];
        array = sets.toArray(array);
        for (int j = 0; j < array.length; j++) {
            if (j == i) {
                Object key = array[j];
                return key;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(FunctionSecondListItemViewHolder
                                         functionSecondListItemViewHolder, int i) {
        Set<Object> sets = mData.keySet();
        Object[] array = new Object[sets.size()];
        array = sets.toArray(array);
        for (int j = 0; j < array.length; j++) {
            if (j == i) {
                Object key = array[j];
                String value = mData.get(key);
                functionSecondListItemViewHolder.txtView.setText(value);
                if(key.toString().equals(mValue)){
                    functionSecondListItemViewHolder.imgChk.setVisibility(View.VISIBLE);
                }else {
                    functionSecondListItemViewHolder.imgChk.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class FunctionSecondListItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtView;
        ImageView imgChk;
        public FunctionSecondListItemViewHolder(View itemView) {
            super(itemView);
            txtView = (TextView) itemView.findViewById(R.id.tv_function_name);
            imgChk = (ImageView) itemView.findViewById(R.id.tv_item_imgChk);
        }
    }
}
