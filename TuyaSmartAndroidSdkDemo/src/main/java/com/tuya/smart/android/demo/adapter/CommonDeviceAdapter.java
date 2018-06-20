package com.tuya.smart.android.demo.adapter;

import android.app.Activity;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.squareup.picasso.Picasso;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.test.widget.AlertPickDialog;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by letian on 16/7/18.
 */
public class CommonDeviceAdapter extends BaseAdapter {
    static final String TAG = CommonDeviceAdapter.class.getSimpleName();
    private final List<DeviceBean> mDevs;
    private final LayoutInflater mInflater;
    private Context mContext;

    public CommonDeviceAdapter(Context context) {
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
    public DeviceBean getItem(int position) {
        return mDevs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceViewHolder holder;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.list_common_device_item, null);
            holder = new DeviceViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DeviceViewHolder) convertView.getTag();
        }
        holder.initData(mDevs.get(position));
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
        ImageView btnOff;
        TextView txtLight, txtTemp;
        TextView txtFunction;
        LinearLayout functionLayout, lightLayout, tempLayout,btnOffLayout;
        DeviceViewHolder(final View contentView) {
            super(contentView);
            connect = (ImageView) contentView.findViewById(R.id.iv_device_list_dot);
            deviceIcon = (ImageView) contentView.findViewById(R.id.iv_device_icon);
            device = (TextView) contentView.findViewById(R.id.tv_device);
            btnOffLayout = (LinearLayout) contentView.findViewById(R.id.common_device_btnOffLayout);
            btnOff = (ImageView) contentView.findViewById(R.id.common_device_btnOff);
            txtLight = (TextView) contentView.findViewById(R.id.common_device_txtLight);
            txtTemp = (TextView) contentView.findViewById(R.id.common_device_txtTemp);
            functionLayout = (LinearLayout) contentView.findViewById(R.id
                    .common_device_functionLayout);
            lightLayout = (LinearLayout) contentView.findViewById(R.id.common_device_lightLayout);
            tempLayout = (LinearLayout) contentView.findViewById(R.id.common_device_tempLayout);
            txtFunction = (TextView) contentView.findViewById(R.id.common_device_item_txtFunction);
            txtFunction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int visibility = functionLayout.getVisibility();
                    if (visibility == View.GONE) {
                        visibility = View.VISIBLE;
                        txtFunction.setText(R.string.btn_commonuse_hide);
                    } else {
                        visibility = View.GONE;
                        txtFunction.setText(R.string.btn_commonuse);
                    }
                    functionLayout.setVisibility(visibility);
                    btnOffLayout.setVisibility(visibility);
                }
            });

        }

        @Override
        public void initData(final DeviceBean deviceBean) {
            Picasso.with(TuyaSdk.getApplication()).load(deviceBean.getIconUrl()).into(deviceIcon);
            final int resId;
            if (deviceBean.getIsOnline()) {
                if (deviceBean.getIsShare() != null && deviceBean.getIsShare()) {
                    resId = R.drawable.ty_devicelist_share_green;
                } else {
                    resId = R.drawable.ty_devicelist_dot_green;
                }
            } else {
                if (deviceBean.getIsShare() != null && deviceBean.getIsShare()) {
                    resId = R.drawable.ty_devicelist_share_gray;
                } else {
                    resId = R.drawable.ty_devicelist_dot_gray;
                }
            }

            try {
                Map<String, Object> dps = deviceBean.getDps();
                final int value_light = (int) dps.get("3");
                final int value_temp = (int) dps.get("4");
                final boolean on = (boolean) dps.get("1");
                int switch_resid = R.drawable.ty_device_power_on;
                if(!on){
                    switch_resid =R.drawable.ty_device_power_off;
                }
                int per_light = value_light * 100 / 255;
                int per_temp = value_temp * 100 / 255;
                txtLight.setText(String.valueOf(per_light) + "%");
                txtTemp.setText(String.valueOf(per_temp) + "%");
                connect.setImageResource(resId);
                device.setText(deviceBean.getName());
                btnOff.setImageResource(switch_resid);
                btnOffLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(deviceBean.getDevId()).getDps();
                            final boolean b = (boolean) map_dp.get("1");
                            Map<String, Object> map = new HashMap<>();
                            map.put("1", !b);
                            String json = JSONObject.toJSONString(map);
                            TuyaDevice device = new TuyaDevice(deviceBean
                                    .getDevId());
                            device.publishDps(json, new IControlCallback() {
                                @Override
                                public void onError(String s, String s1) {

                                }

                                @Override
                                public void onSuccess() {
                                    int switch_resid = R.drawable.ty_device_power_on;
                                    if(!b){
                                        switch_resid = R.drawable.ty_device_power_off;
                                    }
                                    btnOff.setImageResource(switch_resid);
                                }
                            });
                        }catch (Exception ex){
                            Log.e(TAG,ex.toString());
                        }
                    }
                });
                lightLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertPickDialog.showSeekBarPickDialog((Activity) contentView.getContext()
                                , value_light,
                                new AlertPickDialog.AlertPickCallBack2() {
                                    @Override
                                    public void confirm(String value,boolean sw) {
                                        Log.d("CommonDevice", "confirm:" + value);
                                        if (!deviceBean.getIsOnline()) {
                                            return;
                                        }
                                        try {
                                            int v = Integer.valueOf(value);
                                            int percent = v * 100 / 255;
                                            txtLight.setText(String.valueOf(percent) + "%");
                                            if (v <= 0) {
                                                v = 25;
                                            }
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("3", v);
                                            String json = JSONObject.toJSONString(map);
                                            TuyaDevice device = new TuyaDevice(deviceBean
                                                    .getDevId());
                                            device.publishDps(json, new IControlCallback() {
                                                @Override
                                                public void onError(String s, String s1) {

                                                }

                                                @Override
                                                public void onSuccess() {

                                                }
                                            });
                                        } catch (Exception ex) {
                                            Log.e(TAG, ex.toString());
                                        }

                                    }

                                    @Override
                                    public void cancel() {

                                    }
                                });
                    }

                });
                tempLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertPickDialog.showSeekBarPickDialog((Activity) contentView.getContext(),value_temp,
                                new AlertPickDialog.AlertPickCallBack2() {
                                    @Override
                                    public void confirm(String value,boolean sw) {
                                        try {
                                            Log.d("CommonDeviceAdapter", "confirm:" + value);
                                            if (!deviceBean.getIsOnline()) {
                                                return;
                                            }
                                            int p = (int) Integer.valueOf(value);
                                            int percent = p * 100 / 255;
                                            txtTemp.setText(String.valueOf(percent) + "%");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("4", p);
                                            String json = JSONObject.toJSONString(map);
                                            TuyaDevice device = new TuyaDevice(deviceBean
                                                    .getDevId());
                                            device.publishDps(json, new IControlCallback() {
                                                @Override
                                                public void onError(String s, String s1) {

                                                }

                                                @Override
                                                public void onSuccess() {

                                                }
                                            });
                                        } catch (Exception ex) {
                                            Log.e(TAG, ex.toString());
                                        }

                                    }

                                    @Override
                                    public void cancel() {

                                    }
                                });
                    }
                });
            } catch (Exception ex) {
                Log.e("CommonDeviceAdapter", ex.toString());
            }

        }
    }
}
