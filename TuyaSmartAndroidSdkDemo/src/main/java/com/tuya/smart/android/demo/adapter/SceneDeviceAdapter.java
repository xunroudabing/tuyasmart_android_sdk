package com.tuya.smart.android.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-3-25.
 */

public class SceneDeviceAdapter extends BaseAdapter {
    static final String TAG = SceneDeviceAdapter.class.getSimpleName();
    private final List<DeviceBean> mDevs;
    private final LayoutInflater mInflater;
    private Context mContext;
    private String devId;
    private String devAction;
    public SceneDeviceAdapter(Context context) {
        mDevs = new ArrayList<>();
        mContext = context;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    public void setDevAction(String devid,String action){
        devId = devid;
        devAction = action;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mDevs.size();
    }

    @Override
    public DeviceBean getItem(int position) {
        return mDevs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SceneDeviceAdapter.DeviceViewHolder holder;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.list_scene_device_item, null);
            holder = new SceneDeviceAdapter.DeviceViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SceneDeviceAdapter.DeviceViewHolder) convertView.getTag();
        }
        holder.initData(mDevs.get(position));
        if(mDevs.get(position).getDevId().equals(devId)){
            holder.setAction(devAction);
        }
        return convertView;
    }

    public void setData(List<DeviceBean> myDevices) {
        mDevs.clear();
        if (myDevices != null) {
            mDevs.addAll(myDevices);
        }
        notifyDataSetChanged();
    }

    private static class DeviceViewHolder extends ViewHolder<DeviceBean> {
        ImageView connect;
        ImageView deviceIcon;
        TextView device;
        TextView action;
        DeviceViewHolder(final View contentView) {
            super(contentView);
            connect = (ImageView) contentView.findViewById(R.id.iv_device_list_dot);
            deviceIcon = (ImageView) contentView.findViewById(R.id.iv_device_icon);
            device = (TextView) contentView.findViewById(R.id.tv_device);
            action = (TextView) contentView.findViewById(R.id.tv_actions);
        }

        @Override
        public void initData(final DeviceBean deviceBean) {
            Picasso.with(TuyaSdk.getApplication()).load(deviceBean.getIconUrl()).into(deviceIcon);
            device.setText(deviceBean.getName());
            final int resId;
            if (deviceBean.getIsOnline()) {

                if (deviceBean.getIsShare()) {
                    resId = R.drawable.ty_devicelist_share_green;
                } else {
                    resId = R.drawable.ty_devicelist_dot_green;
                }
            } else {
                if (deviceBean.getIsShare()) {
                    resId = R.drawable.ty_devicelist_share_gray;
                } else {
                    resId = R.drawable.ty_devicelist_dot_gray;
                }
            }
            action.setText(null);
            action.setVisibility(View.GONE);
            connect.setImageResource(resId);
        }
        public void setAction(String str){
            action.setText(str);
            action.setVisibility(View.VISIBLE);
        }
    }
}
