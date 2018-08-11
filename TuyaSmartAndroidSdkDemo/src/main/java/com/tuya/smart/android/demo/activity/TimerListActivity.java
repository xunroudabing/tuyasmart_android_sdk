package com.tuya.smart.android.demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.kyleduo.switchbutton.SwitchButton;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaGroup;
import com.tuya.smart.sdk.TuyaTimerManager;
import com.tuya.smart.sdk.api.IGetAllTimerWithDevIdCallback;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.Timer;
import com.tuya.smart.sdk.bean.TimerTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时列表
 * Created by HanZheng(305058709@qq.com) on 2018-6-23.
 */

public class TimerListActivity extends BaseActivity {

    static final String TAG = TimerListActivity.class.getSimpleName();
    String[] mDays;
    LinearLayout mEmptyLayout;
    ListView mListview;
    TimerTaskAdapter mTaskAdapter;
    Button mBtnAdd;
    private boolean isGroup = false;
    private long mGroupId;
    private String mDpId;
    private String mProductId;
    private TuyaDevice mTuyaDevice;
    private ITuyaGroup mTuyaGroup;
    private String mDevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_list);
        initToolbar();
        initMenu();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTimerTaskStatusList();
    }

    protected void initMenu() {
        setTitle(R.string.title_add_timer);
        setDisplayHomeAsUpEnabled();
    }

    protected void initViews() {
        isGroup = getIntent().getBooleanExtra(DeviceColorPickActivity.INTENT_ISGROUP, false);
        mGroupId = getIntent().getLongExtra(DeviceColorPickActivity.INTENT_GROUPID, 0);
        mDevId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_DEVID);
        mDpId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_DPID);
        mProductId = getIntent().getStringExtra(DeviceColorPickActivity.INTENT_PRODUCTID);
//        if (isGroup && mGroupId != 0L) {
//            mTuyaGroup = TuyaGroup.newGroupInstance(mGroupId);
//        }
        mTuyaDevice = new TuyaDevice(mDevId);
        mEmptyLayout = (LinearLayout) findViewById(R.id.timer_list_emptyLayout);
        mListview = (ListView) findViewById(R.id.timer_listview);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long
                    id) {
                Timer timer = mTaskAdapter.getData(position);
                gotoUpdateTimerActivity(timer);
            }
        });
        mListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int
                    position, long id) {
                Timer timer = mTaskAdapter.getData(position);
                deleteTimer(timer);
                return true;
            }
        });
        mBtnAdd = (Button) findViewById(R.id.timer_btnAdd);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddTimerActivity();
            }
        });
        mDays = new String[]{getString(R.string.txt_day0), getString(R.string.txt_day1),
                getString(R.string.txt_day2), getString(R.string.txt_day3), getString(R.string
                .txt_day4), getString(R.string.txt_day5), getString(R.string.txt_day6)};
    }

    //请求定时列表
    protected void getTimerTaskStatusList() {
        String devId = mDevId;
        if (isGroup) {
            devId = String.valueOf(mGroupId);
        }
        final TuyaTimerManager timerManager = new TuyaTimerManager();
        timerManager.getAllTimerWithDeviceId(devId, new IGetAllTimerWithDevIdCallback() {
            @Override
            public void onSuccess(ArrayList<TimerTask> arrayList) {
                bindListView(arrayList);
            }


            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "getTimerTaskStatusList onError=" + s + "," + s1);
            }
        });
    }

    protected void bindListView(ArrayList<TimerTask> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            TimerTask timerTask = arrayList.get(0);
            if (timerTask.getTimerList() != null && timerTask.getTimerList().size() > 0) {
                mTaskAdapter = new TimerTaskAdapter(timerTask.getTimerList());
                mListview.setAdapter(mTaskAdapter);
                mListview.setVisibility(View.VISIBLE);
                mEmptyLayout.setVisibility(View.GONE);

            } else {
                mEmptyLayout.setVisibility(View.VISIBLE);
                mListview.setVisibility(View.GONE);
            }
        } else {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mListview.setVisibility(View.GONE);
        }

    }

    protected void deleteTimer(final Timer timer) {
        DialogUtil.simpleConfirmDialog(TimerListActivity.this, getString(R.string
                .alert_confirm_delete_group), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    String devid = mDevId;
                    if (isGroup) {
                        devid = String.valueOf(mGroupId);
                    }
                    final TuyaTimerManager timerManager = new TuyaTimerManager();
                    timerManager.removeTimerWithTask("timer", devid, timer.getTimerId(), new
                            IResultStatusCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "removeTimer success");
                                    getTimerTaskStatusList();
                                }

                                @Override
                                public void onError(String s, String s1) {
                                    Log.d(TAG, "removeTimer error," + s + "," + s1);
                                }
                            });
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    protected void gotoUpdateTimerActivity(Timer timer) {
        String json = JSONObject.toJSONString(timer);
        Intent intent = new Intent(getApplicationContext(), AddTimerActivity.class);
        intent.putExtra(AddTimerActivity.INTENT_TIMER_UPDATE, json);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    protected void gotoAddTimerActivity() {
        //ActivityUtils.gotoActivity(TimerListActivity.this,AddTimerActivity.class,ActivityUtils
        // .ANIMATE_FORWARD,false);
        Intent intent = new Intent(getApplicationContext(), AddTimerActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    class TimerTaskAdapter extends BaseAdapter {
        List<Timer> mList;

        public TimerTaskAdapter(List<Timer> list) {
            mList = list;
        }

        public Timer getData(int position) {
            return mList.get(position);
        }

        protected View createView(int position, View convertView, ViewGroup parent) {
            View rootView = null;
            if (convertView == null) {
                rootView = LayoutInflater.from(TimerListActivity.this).inflate(R.layout
                        .list_item_timer, parent, false);
            } else {
                rootView = convertView;
            }
            TextView titleview = (TextView) rootView.findViewById(R.id.timer_item_title);
            TextView subtitleview = (TextView) rootView.findViewById(R.id.timer_item_subtitle);
            SwitchButton swbutton = (SwitchButton) rootView.findViewById(R.id.timer_item_switch);
            final Timer timer = mList.get(position);
            int status = timer.getStatus();
            final String time = timer.getTime();
            String value = timer.getValue();
            JSONObject object = JSONObject.parseObject(value);
            String loop = timer.getLoops();
            String loop_string = convertLoopString(loop);
            titleview.setText(time);
            subtitleview.setText(loop_string);
            String sw_enable_str = "";
            if (object.containsKey("1")) {
                boolean b = object.getBoolean("1");
                String s = b ? getString(R.string.swtich_open) : getString(R.string.swtich_close);
                sw_enable_str = getString(R.string.swtich_string) + ":" + s;
                subtitleview.setText(loop_string + "\r\n" + sw_enable_str);
            }
            if (status == 1) {
                swbutton.setChecked(true);
            } else {
                swbutton.setChecked(false);
            }
            swbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String devid = mDevId;
                    if (isGroup) {
                        devid = String.valueOf(mGroupId);
                    }
                    final TuyaTimerManager timerManager = new TuyaTimerManager();
                    timerManager.updateTimerStatusWithTask("timer", devid, timer.getTimerId(),
                            isChecked, new IResultStatusCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "operateTimer success");
                        }

                        @Override
                        public void onError(String s, String s1) {
                            Log.d(TAG, "operateTimer error," + s + "," + s1);
                        }
                    });
                }
            });
            return rootView;
        }

        protected String convertLoopString(String loop) {
            char[] chars = loop.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '1') {
                    sb.append(mDays[i] + ",");
                }
            }
            String ret = "";
            if (sb.length() <= 0) {
                ret = getString(R.string.txt_onlyonce);
            } else {
                ret = sb.substring(0, sb.length() - 1);
            }
            return ret;
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return mList.size();
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
            return position;
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we
         *                 want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can
         * either
         * create a View manually or inflate it from an XML layout file. When the View is
         * inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item
         *                    whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that
         *                    this view
         *                    is non-null and of an appropriate type before using. If it is not
         *                    possible to convert
         *                    this view to display the correct data, this method can create a new
         *                    view.
         *                    Heterogeneous lists can specify their number of view types, so that
         *                    this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }
    }
}
