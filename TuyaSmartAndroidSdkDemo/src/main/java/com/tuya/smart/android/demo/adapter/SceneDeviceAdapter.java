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
import com.tuya.smart.sdk.bean.scene.dev.SceneDevBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-3-25.
 */

public class SceneDeviceAdapter extends BaseAdapter {
    static final String TAG = SceneDeviceAdapter.class.getSimpleName();
    private final List<SceneDevBean> mDevs;
    private final LayoutInflater mInflater;
    private Context mContext;

    public SceneDeviceAdapter(Context context) {
        mDevs = new ArrayList<>();
        mContext = context;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mDevs.size();
    }

    @Override
    public SceneDevBean getItem(int position) {
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
        return convertView;
    }

    public void setData(List<SceneDevBean> myDevices) {
        mDevs.clear();
        if (myDevices != null) {
            mDevs.addAll(myDevices);
        }
        notifyDataSetChanged();
    }

    private static class DeviceViewHolder extends ViewHolder<SceneDevBean> {
        ImageView connect;
        ImageView deviceIcon;
        TextView device;

        DeviceViewHolder(final View contentView) {
            super(contentView);
            connect = (ImageView) contentView.findViewById(R.id.iv_device_list_dot);
            deviceIcon = (ImageView) contentView.findViewById(R.id.iv_device_icon);
            device = (TextView) contentView.findViewById(R.id.tv_device);

        }

        @Override
        public void initData(final SceneDevBean deviceBean) {
            Picasso.with(TuyaSdk.getApplication()).load(deviceBean.getIconUrl()).into(deviceIcon);
            device.setText(deviceBean.getName());
            final int resId;
            if (deviceBean.getIsOnline()) {
                if (deviceBean.isShare()) {
                    resId = R.drawable.ty_devicelist_share_green;
                } else {
                    resId = R.drawable.ty_devicelist_dot_green;
                }
            } else {
                if (deviceBean.isShare()) {
                    resId = R.drawable.ty_devicelist_share_gray;
                } else {
                    resId = R.drawable.ty_devicelist_dot_gray;
                }
            }
            connect.setImageResource(resId);
        }
    }
}
