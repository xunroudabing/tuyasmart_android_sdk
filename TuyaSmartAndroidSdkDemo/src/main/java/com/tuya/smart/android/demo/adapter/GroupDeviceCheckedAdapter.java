package com.tuya.smart.android.demo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-4-23.
 */

public class GroupDeviceCheckedAdapter extends BaseAdapter {
    static final String TAG = GroupDeviceCheckedAdapter.class.getSimpleName();
    Context mContext;
    List<GroupDeviceBean> mList;
    boolean mShowChk = true;
    GroupDeviceCheckedAdapter otherAdapter;
    public GroupDeviceCheckedAdapter(Context context, List<GroupDeviceBean> list, boolean showChk) {
        mContext = context;
        mList = list;
        mShowChk = showChk;
    }
    public  void attach(GroupDeviceCheckedAdapter adapter){
        otherAdapter = adapter;
    }
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        int size = 0;
        for (GroupDeviceBean bean : mList) {
            if (bean.isChecked() == mShowChk) {
                size++;
            }
        }
        return size;
    }

    public GroupDeviceBean getBean(int position) {
        List<GroupDeviceBean> list = new ArrayList<>();
        for (GroupDeviceBean bean : mList) {
            if (bean.isChecked() == mShowChk) {
                list.add(bean);
            }
        }
        if (position < list.size()) {
            return list.get(position);
        }
        return null;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item
     *                    whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not
     *                    possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that
     *                    this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupDeviceViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_group_device_item,
                    parent, false);
            holder = new GroupDeviceViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (GroupDeviceViewHolder) convertView.getTag();
        }
        holder.initData(getBean(position));
        return convertView;
    }

    class GroupDeviceViewHolder extends ViewHolder<GroupDeviceBean> {
        ImageView mIcon;
        TextView mName;
        CheckBox mChk;
        RelativeLayout mParent;
        public GroupDeviceViewHolder(View contentView) {
            super(contentView);
            mIcon = (ImageView) contentView.findViewById(R.id.iv_device_icon);
            mName = (TextView) contentView.findViewById(R.id.tv_device);
            mChk = (CheckBox) contentView.findViewById(R.id.list_group_device_item_chk);
            mParent = (RelativeLayout) contentView.findViewById(R.id.list_group_device_item_parent);
            mParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"toggle");
                    mChk.toggle();
                }
            });
        }

        @Override
        public void initData(final GroupDeviceBean data) {
            DeviceBean deviceBean = data.getDeviceBean();
            Picasso.with(TuyaSdk.getApplication()).load(deviceBean.getIconUrl()).into(mIcon);
            mName.setText(deviceBean.getName());
            mChk.setOnCheckedChangeListener(null);
            mChk.setChecked(data.isChecked());
            mChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    data.setChecked(isChecked);
                    notifyDataSetChanged();
                    if(otherAdapter != null){
                        otherAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
