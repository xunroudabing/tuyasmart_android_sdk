package com.tuya.smart.android.demo.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.test.bean.AlertPickBean;
import com.tuya.smart.android.demo.test.widget.AlertPickDialog;
import com.tuya.smart.android.demo.widget.mode.PanContainer;
import com.tuya.smart.android.demo.widget.mode.RotatePan;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaTimerManager;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IResultStatusCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceModePickActivity extends BaseActivity implements SeekBar
        .OnSeekBarChangeListener, View.OnClickListener {
    public static final String INTENT_DEVID = "intent_devid";
    public static final String INTENT_DPID = "intent_dpid";
    public static final String INTNET_TITLE = "intent_title";
    static final int ACTION_MODE_POSITION = 100;
    static final String TAG = DeviceModePickActivity.class.getSimpleName();
    boolean mSwitch = true;
    int mValue;//明度
    String mSpeed = "10";
    SeekBar seekBarLight, seekBarSu;
    ImageButton btnScene, btnSwitch, btnNight, btnDelay, btnTimer;
    PanContainer mPanContainer;
    RotatePan mPan;
    ImageView btnGo;
    List<Bitmap> mBackgrounds = new ArrayList<>();
    private String mDevId;
    private String mDpId;
    private TuyaDevice mTuyaDevice;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_MODE_POSITION:
                    try {
                        int positon = (int) msg.obj;
                        setMode(positon);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_modepick);
        initToolbar();
        initMenu();
        initViews();
    }

    protected void initToolbar() {
        if (mToolBar == null) {
            mToolBar = (Toolbar) findViewById(R.id.toolbar_top_view);
            if (mToolBar == null) {
            } else {
                TypedArray a = obtainStyledAttributes(new int[]{
                        R.attr.status_font_color});
                int titleColor = a.getInt(0, Color.WHITE);
                mToolBar.setTitleTextColor(titleColor);
            }
        }
    }

    protected void initMenu() {
        setDisplayHomeAsUpEnabled();
        String title = getIntent().getStringExtra(INTNET_TITLE);
        setTitle(title);
    }

    protected void initViews() {
        mDevId = getIntent().getStringExtra(INTENT_DEVID);
        mDpId = getIntent().getStringExtra(INTENT_DPID);
        mTuyaDevice = new TuyaDevice(mDevId);
        seekBarLight = (SeekBar) findViewById(R.id.device_color_seekbarLight);
        seekBarSu = (SeekBar) findViewById(R.id.device_color_seekbarSu);
        seekBarLight.setOnSeekBarChangeListener(this);
        seekBarSu.setOnSeekBarChangeListener(this);
        btnDelay = (ImageButton) findViewById(R.id.device_color_btnDelay);
        btnTimer = (ImageButton) findViewById(R.id.device_color_btnTimer);
        btnDelay.setOnClickListener(this);
        btnTimer.setOnClickListener(this);
        btnScene = (ImageButton) findViewById(R.id.device_color_btnScene);
        btnScene.setOnClickListener(this);
        btnSwitch = (ImageButton) findViewById(R.id.device_color_btnSwitch);
        btnNight = (ImageButton) findViewById(R.id.device_color_btnNight);
        btnSwitch.setOnClickListener(this);
        btnNight.setOnClickListener(this);
        btnGo = (ImageView) findViewById(R.id.device_mode_go);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPanContainer.rotate(-1, 1000);
            }
        });
        mPanContainer = (PanContainer) findViewById(R.id.device_mode_panContainer);
        mPanContainer.setAnimationEndListener(new PanContainer.AnimationEndListener() {
            @Override
            public void endAnimation(int position) {
                Log.d(TAG, "endAnimation:pos=" + position);
                setMode(position);
            }
        });
        mPan = (RotatePan) findViewById(R.id.device_mode_pan);
        mPan.setOnRotatePanListener(new RotatePan.OnRotatePanListener() {
            @Override
            public void onComplete(int position) {
                Log.d(TAG, "onComplete.pos = " + position);
                mHandler.removeMessages(ACTION_MODE_POSITION);
                mHandler.sendMessageDelayed(Message.obtain(mHandler, ACTION_MODE_POSITION,
                        position), 1000);
            }
        });
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_1));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_2));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_3));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_4));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_5));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_6));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_7));
        mBackgrounds.add(BitmapFactory.decodeResource(getResources(), R.mipmap.paint_style_8));
        mPan.setBackgrounds(mBackgrounds);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                //调光
                case R.id.device_color_seekbarLight:
                    try {
                        int value = progress + 25;
                        mValue = Math.min(value, 255);
                        setLight(mValue);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    break;

                case R.id.device_color_seekbarSu:
                    try {
                        setSu(progress);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    break;
            }
        }
    }

    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seekbar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seekbar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //情景
            case R.id.device_color_btnScene:
                Intent intent = new Intent(this, DeviceModePickActivity.class);
                startActivity(intent);
                //开关
            case R.id.device_color_btnSwitch:
                try {
                    setSwitch();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            //夜灯
            case R.id.device_color_btnNight:
                try {
                    setNight();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            case R.id.device_color_btnDelay:
                try {
                    setDelay();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            case R.id.device_color_btnTimer:
                try {
                    setTimer();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
        }
    }

    protected void setMode(int postion) {
        switch (postion) {
            case 0:
                setMode1();
                break;
            case 1:
                setMode2();
                break;
            case 2:
                setMode3();
                break;
            case 3:
                setMode4();
                break;
            case 4:
                setMode5();
                break;
            case 5:
                setMode6();
                break;
            case 6:
                setMode7();
                break;
            case 7:
                setMode8();
                break;
        }
    }

    protected void setMode1() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s06f30000f200ab0000f31ffab224fb10f4dc05", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_2");
            map.put("8", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    protected void setMode2() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s060000f5f4cb06f100ec25fb10f1000aa699b8", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_2");
            map.put("8", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    protected void setMode3() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s04fb00060200ff240003fedd0a", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_4");
            map.put("10", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    protected void setMode4() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s060000f5f4cb06f1000927fb101be1f4e500f5", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_4");
            map.put("10", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }
    //红闪
    protected void setMode8() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s01ff0000", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_3");
            map.put("9", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }
    //蓝闪
    protected void setMode6() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s010000ff", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_3");
            map.put("9", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }
    //绿闪
    protected void setMode7() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s0100ff00", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_3");
            map.put("9", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }
    //白闪
    protected void setMode5() {
        try {
            Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
            int v3 = (int) map_dp.get("3");
            int v4 = (int) map_dp.get("4");
            String v = String.format("%s%s%s01000000", Integer
                    .toHexString(v3), Integer.toHexString(v4), mSpeed);
            Map<String, Object> map = new HashMap<>();
            map.put("2", "scene_1");
            map.put("7", v);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }
    //开关
    protected void setSwitch() {
        mSwitch = !mSwitch;
        Map<String, Object> map = new HashMap<>();
        map.put("1", mSwitch);
        map.put("2", "white");
        final String value = JSONObject.toJSONString(map);
        sendDp(value);
    }

    //调光 改变亮度
    protected void setLight(int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("3", value);
        final String json = JSONObject.toJSONString(map);
        sendDp(json);
    }

    protected void setSu(int value) {
        String s = String.format("%sffffff%s", "ffffff", Integer.toHexString(value));
        Map<String, Object> map = new HashMap<>();
        map.put("2", "colour");
        //map.put("2","scene");
        map.put("5", s);
        map.put("3", value);
        final String json = JSONObject.toJSONString(map);
        sendDp(json);

    }

    //色温
    protected void setTemp(int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("4", value);
        final String json = JSONObject.toJSONString(map);
        sendDp(json);
    }

    protected void setTimer() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        Log.d(TAG, "calendar.hour=" + hour + ",min=" + min);
        ArrayList<String> minutes = new ArrayList<>();
        ArrayList<String> hours = new ArrayList<>();
        for (int i = 0; i <= 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        final AlertPickBean alertPickBean1 = new AlertPickBean();
        alertPickBean1.setLoop(true);
        alertPickBean1.setCancelText(getString(R.string.cancel));
        alertPickBean1.setConfirmText(getString(R.string.confirm));
        alertPickBean1.setRangeKeys(hours);
        alertPickBean1.setRangeValues(hours);
        alertPickBean1.setSelected(hour);
        alertPickBean1.setTitle(getString(R.string.title_choose_timer));

        final AlertPickBean alertPickBean2 = new AlertPickBean();
        alertPickBean2.setLoop(true);
        alertPickBean2.setCancelText(getString(R.string.cancel));
        alertPickBean2.setConfirmText(getString(R.string.confirm));
        alertPickBean2.setRangeKeys(minutes);
        alertPickBean2.setRangeValues(minutes);
        alertPickBean2.setTitle(getString(R.string.title_choose_timer));
        alertPickBean2.setSelected(min);
        AlertPickDialog.showTimePickAlertPickDialog(DeviceModePickActivity.this, alertPickBean1,
                alertPickBean2, new
                        AlertPickDialog.AlertPickCallBack() {
                            @Override
                            public void confirm(String value) {
                                Log.d(TAG, "定时关闭：" + value);
                                setTuyaTimer(value);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
    }

    protected void setDelay() {
        ArrayList<String> rangesKey = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            rangesKey.add(String.valueOf(i));
        }
        final AlertPickBean alertPickBean = new AlertPickBean();
        alertPickBean.setLoop(true);
        alertPickBean.setCancelText(getString(R.string.cancel));
        alertPickBean.setConfirmText(getString(R.string.confirm));
        alertPickBean.setRangeKeys(rangesKey);
        alertPickBean.setRangeValues(rangesKey);
        alertPickBean.setTitle(getString(R.string.title_choose_delay));
        AlertPickDialog.showAlertPickDialog(DeviceModePickActivity.this, alertPickBean, new
                AlertPickDialog.AlertPickCallBack() {
                    @Override
                    public void confirm(String value) {
                        setdelay(value);
                    }

                    @Override
                    public void cancel() {

                    }
                });
//        DialogUtil.NumberPickDialog(DeviceColorPickActivity.this, "延时关闭", "", getString(R.string
//                .ty_confirm), getString(R.string.ty_cancel), new DialogInterface
// .OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which) {
//                    case DialogInterface.BUTTON_POSITIVE:
//                        break;
//                    case DialogInterface.BUTTON_NEGATIVE:
//                        break;
//                }
//            }
//        });
    }

    protected void setdelay(String delay) {
        long currentTime = System.currentTimeMillis();
        long executeTime = currentTime + Integer.parseInt(delay) * 60 * 1000;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String time = format.format(new Date(executeTime));
        Log.d(TAG, "setdelay:" + time);
        setTuyaTimer(time);
    }

    protected void setTuyaTimer(final String time) {
        final TuyaTimerManager timerManager = new TuyaTimerManager();
        Map<String, Object> map = new HashMap<>();
        map.put("1", false);
        timerManager.addTimerWithTask("timer", mDevId, "0000000", map, time, new
                IResultStatusCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "设置定时器成功：" + time);
                    }

                    @Override
                    public void onError(String s, String s1) {

                    }
                });
    }

    //夜灯
    protected void setNight() {
        // 亮度5% 色温50%
        int light = (int) (255 * 0.05);
        int temp = (int) (255 * 0.5);
        Map<String, Object> map = new HashMap<>();
        map.put("3", light);
        map.put("4", temp);
        final String json = JSONObject.toJSONString(map);
        sendDp(json);
    }

    protected void sendDp(String json) {
        Log.d(TAG, "sendDp:" + json);
        mTuyaDevice.publishDps(json, new IControlCallback() {
            @Override
            public void onError(String s, String s1) {
                //mView.showMessage("send command failure");
                Log.d(TAG, "onError:" + s + "," + s1);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess");
            }
        });
    }

    protected void sendDp(Map<String, String> dp) {
        final String value = JSONObject.toJSONString(dp);
        Log.d(TAG, "sendDp:" + value);
        sendDp(value);
    }
}
