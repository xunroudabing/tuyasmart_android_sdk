package com.tuya.smart.android.demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.sdk.bean.scene.PlaceFacadeBean;

import java.util.List;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-1-28.
 */

public class CityListRecyclerAdapter extends RecyclerView.Adapter<CityListRecyclerAdapter
        .CityViewHolder> {
    List<PlaceFacadeBean> mList;
    int position_chk = -1;
    String city_selected = "";

    public CityListRecyclerAdapter(List<PlaceFacadeBean> list) {
        mList = list;
    }

    public PlaceFacadeBean getChecked() {
        if (position_chk > 0) {
            return mList.get(position_chk);
        }
        return null;
    }

    public void setChecked(String cityname) {
        city_selected = cityname;
    }

    public void setChecked(int position) {
        if (position < mList.size()) {
            position_chk = position;
            notifyDataSetChanged();
        }
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CityViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                .recycler_city_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(CityViewHolder cityViewHolder, int i) {
        PlaceFacadeBean bean = mList.get(i);
        cityViewHolder.txtName.setText(bean.getCity());
        cityViewHolder.imgChk.setVisibility(View.INVISIBLE);

        if (position_chk > 0) {
            if (position_chk == i) {
                cityViewHolder.imgChk.setVisibility(View.VISIBLE);
            }
        } else if (!TextUtils.isEmpty(city_selected)) {
            if (bean.getCity().equals(city_selected)) {
                position_chk = cityViewHolder.getAdapterPosition();
                cityViewHolder.imgChk.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public ImageView imgChk;

        public CityViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.city_list_item_txtName);
            imgChk = (ImageView) itemView.findViewById(R.id.city_list_item_imgChk);
        }
    }
}
