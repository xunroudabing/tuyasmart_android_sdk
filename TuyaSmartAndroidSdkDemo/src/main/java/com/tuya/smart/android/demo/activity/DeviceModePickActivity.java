package com.tuya.smart.android.demo.activity;

import android.Manifest;
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
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.test.bean.AlertPickBean;
import com.tuya.smart.android.demo.test.widget.AlertPickDialog;
import com.tuya.smart.android.demo.utils.AudioUtils;
import com.tuya.smart.android.demo.utils.CheckPermissionUtils;
import com.tuya.smart.android.demo.utils.TuyaUtils;
import com.tuya.smart.android.demo.widget.mode.PanContainer;
import com.tuya.smart.android.demo.widget.mode.RotatePan;
import com.tuya.smart.bluemesh.mesh.device.ITuyaBlueMeshDevice;
import com.tuya.smart.home.interior.presenter.TuyaDevice;
import com.tuya.smart.home.interior.presenter.TuyaSmartDevice;
import com.tuya.smart.home.interior.presenter.TuyaTimerManager;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

public class DeviceModePickActivity extends BaseActivity implements SeekBar
        .OnSeekBarChangeListener, View.OnClickListener {
    public static final String INTENT_DEVID = "intent_devId";
    public static final String INTENT_DPID = "intent_dpid";
    public static final String INTNET_TITLE = "intent_title";
    public static final String INTENT_ISGROUP = "INTENT_ISGROUP";
    public static final String INTENT_GROUPID = "INTENT_GROUPID";
    public static final int REQUEST_RECODE_AUDIO = 101;
    public static final String INTENT_ISMESH = "INTENT_ISMESH";
    public static final String INTENT_MESH_NODEID = "INTENT_MESH_NODEID";
    public static final String INTENT_MESH_CATEGORY = "INTENT_MESH_CATEGORY";
    static final int ACTION_MODE_POSITION = 100;
    static final int ACTION_MUSIC = 102;
    static final String TAG = DeviceModePickActivity.class.getSimpleName();
    CheckPermissionUtils checkPermission;
    boolean mSwitch = true;
    boolean mMusicStart = false;
    int mCurrentPostion = 0;
    int mValue;//明度
    int mSpeed = 10;
    SeekBar seekBarLight, seekBarSu;
    ImageButton btnScene, btnSwitch, btnNight, btnDelay, btnTimer;
    ImageButton btnSpeedMin, btnSpeedPlus;
    PanContainer mPanContainer;
    RotatePan mPan;
    ImageView btnGo;
    List<Bitmap> mBackgrounds = new ArrayList<>();
    ImageButton btnMusic;
    ScheduledExecutorService mScheduleService;
    AudioUtils mAudioUtils;
    boolean sw = true;
    private boolean isGroup = false;
    private boolean isMesh = false;
    private long mGroupId;
    private String mDevId;
    private String mDpId;
    private String mNodeId;
    private String mCategory;
    private TuyaDevice mTuyaDevice;
    private ITuyaGroup mTuyaGroup;
    private ITuyaBlueMeshDevice mMeshDevice;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_MODE_POSITION:
                    try {
                        mCurrentPostion = (int) msg.obj;
                        setMode(mCurrentPostion);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    break;
                case ACTION_MUSIC:
                    try {
                        int v = (int) msg.obj;
                        setColorByMusic(v);
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
        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioUtils.stopRecord();
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

    protected void checkPermission() {
        checkPermission = new CheckPermissionUtils(this);
        checkPermission.checkSiglePermission(Manifest.permission.RECORD_AUDIO,
                REQUEST_RECODE_AUDIO);
    }

    protected void initMenu() {
        setDisplayHomeAsUpEnabled();
        String title = getIntent().getStringExtra(INTNET_TITLE);
        setTitle(title);
    }

    protected void initData() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                if (map_dp != null) {
                    boolean b = (boolean) map_dp.get("1");
                    final int value_light = (int) map_dp.get("3");
                    final int value_temp = (int) map_dp.get("4");
                    if (b) {
                        btnSwitch.setImageResource(R.drawable.btn_switch);
                    } else {
                        btnSwitch.setImageResource(R.drawable.btn_switch_off);
                    }
                    seekBarLight.setProgress(value_light);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }


    }

    protected void initViews() {
        mAudioUtils = new AudioUtils();
        isGroup = getIntent().getBooleanExtra(INTENT_ISGROUP, false);
        isMesh = getIntent().getBooleanExtra(INTENT_ISMESH, false);
        mGroupId = getIntent().getLongExtra(INTENT_GROUPID, 0);
        mDevId = getIntent().getStringExtra(INTENT_DEVID);
        mDpId = getIntent().getStringExtra(INTENT_DPID);
        isMesh = getIntent().getBooleanExtra(INTENT_ISMESH, false);
        mNodeId = getIntent().getStringExtra(INTENT_MESH_NODEID);
        mCategory = getIntent().getStringExtra(INTENT_MESH_CATEGORY);
        mTuyaDevice = new TuyaDevice(mDevId);
        if (isGroup && mGroupId != 0L) {
            if (isMesh) {
                mTuyaGroup = TuyaHomeSdk.newBlueMeshGroupInstance(mGroupId);
            } else {
                mTuyaGroup = TuyaHomeSdk.newGroupInstance(mGroupId);
            }
        }
        if (isMesh) {
            mMeshDevice = TuyaHomeSdk.newBlueMeshDeviceInstance(CommonConfig.getMeshId
                    (getApplicationContext()));
        }
        btnSpeedMin = (ImageButton) findViewById(R.id.device_mode_btnSpeedDown);
        btnSpeedPlus = (ImageButton) findViewById(R.id.device_mode_btnSpeedUp);
        btnSpeedMin.setOnClickListener(this);
        btnSpeedPlus.setOnClickListener(this);
        seekBarLight = (SeekBar) findViewById(R.id.device_color_seekbarLight);
        seekBarSu = (SeekBar) findViewById(R.id.device_color_seekbarSu);
        seekBarLight.setOnSeekBarChangeListener(this);
        seekBarSu.setOnSeekBarChangeListener(this);
        btnMusic = (ImageButton) findViewById(R.id.device_mode_btnMusic);
        btnMusic.setOnClickListener(this);
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
            case R.id.device_mode_btnSpeedDown:
                try {
                    mSpeed = mSpeed + 10;
                    mSpeed = Math.min(99, mSpeed);
                    setMode(mCurrentPostion);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            case R.id.device_mode_btnSpeedUp:
                try {
                    mSpeed = mSpeed - 10;
                    mSpeed = Math.max(0, mSpeed);
                    setMode(mCurrentPostion);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            case R.id.device_mode_btnMusic:
                startMusicListen();
                break;
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
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                int v3 = (int) map_dp.get("3");
                int v4 = (int) map_dp.get("4");
                String v = String.format("%s%s%d06f30000f200ab0000f31ffab224fb10f4dc05", Integer
                        .toHexString(v3), Integer.toHexString(v4), mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_2");
                map.put("8", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d06f30000f200ab0000f31ffab224fb10f4dc05", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_2");
                map.put("8", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    protected void setMode2() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                int v3 = (int) map_dp.get("3");
                int v4 = (int) map_dp.get("4");
                String v = String.format("%s%s%d060000f5f4cb06f100ec25fb10f1000aa699b8", Integer
                        .toHexString(v3), Integer.toHexString(v4), mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_2");
                map.put("8", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d060000f5f4cb06f100ec25fb10f1000aa699b8", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_2");
                map.put("8", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    protected void setMode3() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                int v3 = (int) map_dp.get("3");
                int v4 = (int) map_dp.get("4");
                String v = String.format("%s%s%d04fb00060200ff240003fedd0a", Integer
                        .toHexString(v3), Integer.toHexString(v4), mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_4");
                map.put("10", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d04fb00060200ff240003fedd0a", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_4");
                map.put("10", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    protected void setMode4() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                int v3 = (int) map_dp.get("3");
                int v4 = (int) map_dp.get("4");
                String v = String.format("%s%s%d060000f5f4cb06f1000927fb101be1f4e500f5", Integer
                        .toHexString(v3), Integer.toHexString(v4), mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_4");
                map.put("10", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d04fb00060200ff240003fedd0a", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_4");
                map.put("10", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    //红闪
    protected void setMode8() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                Map<String, Object> map = new HashMap<>();
                if (isMesh) {
                    int i3 = (int) map_dp.get("3");
                    int value_light = 255 * i3 / 100;
                    int value_temp = (int) map_dp.get("104");
                    String w = Integer.toHexString(value_temp);
                    String c = Integer.toHexString(255 - value_temp);
                    String l = Integer.toHexString(value_light);
                    map.put("109", "scene_3");
                    //00000ffff005900E0
                    map.put("113", String.format("0ff0000%02x%02x%02x00E0", value_temp, 255 -
                            value_temp, value_light));
                } else {
                    int v3 = (int) map_dp.get("3");
                    int v4 = (int) map_dp.get("4");
                    String v = String.format("%s%s%d01ff0000", Integer
                            .toHexString(v3), Integer.toHexString(v4), mSpeed);
                    map.put("2", "scene_3");
                    map.put("9", v);
                }
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d01ff0000", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_3");
                map.put("9", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    //蓝闪
    protected void setMode6() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                Map<String, Object> map = new HashMap<>();
                if (isMesh) {
                    int i3 = (int) map_dp.get("3");
                    int value_light = 255 * i3 / 100;
                    int value_temp = (int) map_dp.get("104");
                    String w = Integer.toHexString(value_temp);
                    String c = Integer.toHexString(255 - value_temp);
                    String l = Integer.toHexString(value_light);
                    map.put("109", "scene_3");
                    //00000ffff005900E0
                    map.put("113", String.format("00000ff%02x%02x%02x00E0", value_temp, 255 -
                            value_temp, value_light));
                } else {
                    int v3 = (int) map_dp.get("3");
                    int v4 = (int) map_dp.get("4");
                    String v = String.format("%s%s%d010000ff", Integer
                            .toHexString(v3), Integer.toHexString(v4), mSpeed);
                    map.put("2", "scene_3");
                    map.put("9", v);
                }
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d010000ff", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_3");
                map.put("9", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    //绿闪
    protected void setMode7() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                Map<String, Object> map = new HashMap<>();
                if (isMesh) {
                    int i3 = (int) map_dp.get("3");
                    int value_light = 255 * i3 / 100;
                    int value_temp = (int) map_dp.get("104");
                    String w = Integer.toHexString(value_temp);
                    String c = Integer.toHexString(255 - value_temp);
                    String l = Integer.toHexString(value_light);
                    map.put("109", "scene_3");
                    //00000ffff005900E0
                    map.put("113", String.format("000ff00%02x%02x%02x00E0", value_temp, 255 -
                            value_temp, value_light));
                } else {
                    int v3 = (int) map_dp.get("3");
                    int v4 = (int) map_dp.get("4");
                    String v = String.format("%s%s%d0100ff00", Integer
                            .toHexString(v3), Integer.toHexString(v4), mSpeed);
                    map.put("2", "scene_3");
                    map.put("9", v);
                }

                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d0100ff00", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_3");
                map.put("9", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    //白闪
    protected void setMode5() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                Map<String, Object> map = new HashMap<>();
                if (isMesh) {
                    int i3 = (int) map_dp.get("3");
                    int value_light = 255 * i3 / 100;
                    int value_temp = (int) map_dp.get("104");
                    String w = Integer.toHexString(value_temp);
                    String c = Integer.toHexString(255 - value_temp);
                    String l = Integer.toHexString(value_light);
                    map.put("109", "scene_1");
                    //00000ffff005900E0
                    map.put("111", String.format("0ffffff%02x%02x%02x00E0", value_temp, 255 -
                            value_temp, value_light));
                } else {
                    int v3 = (int) map_dp.get("3");
                    int v4 = (int) map_dp.get("4");
                    String v = String.format("%s%s%d01ffffff", Integer
                            .toHexString(v3), Integer.toHexString(v4), mSpeed);
                    map.put("2", "scene_1");
                    map.put("7", v);
                }
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            } else {
                String v = String.format("ffff%d01ffffff", mSpeed);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "scene_1");
                map.put("7", v);
                final String value = JSONObject.toJSONString(map);
                sendDp(value);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    //开关
    protected void setSwitch() {
        if (!isGroup) {
            Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
            boolean b = (boolean) map_dp.get("1");
            Map<String, Object> map = new HashMap<>();
            map.put("1", !b);
            if (!b) {
                btnSwitch.setImageResource(R.drawable.btn_switch);
            } else {
                btnSwitch.setImageResource(R.drawable.btn_switch_off);
            }
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
        } else {
            sw = !sw;
            Map<String, Object> map = new HashMap<>();
            map.put("1", sw);
            final String value = JSONObject.toJSONString(map);
            sendDp(value);
            if (!sw) {
                btnSwitch.setImageResource(R.drawable.btn_switch);
            } else {
                btnSwitch.setImageResource(R.drawable.btn_switch_off);
            }
        }
    }

    protected void startMusicListen() {
        mMusicStart = !mMusicStart;
        if (mMusicStart) {
            mAudioUtils.setCallBack(new AudioUtils.ICallback() {
                @Override
                public void onVolume(double v) {
                    if (v > 80D) {
                        Log.d(TAG, "onVolume:" + v);
                        mHandler.sendMessage(Message.obtain(mHandler, ACTION_MUSIC, (int) v));
                    }
                }
            });
            mAudioUtils.startRecord();
            btnMusic.setImageResource(R.drawable.btn_music_pressed);
            Toast.makeText(this, R.string.alert_mode_startmusic, Toast.LENGTH_SHORT).show();
        } else {
            mAudioUtils.stopRecord();
            btnMusic.setImageResource(R.drawable.btn_music_nor);
            Toast.makeText(this, R.string.alert_mode_endmusic, Toast.LENGTH_SHORT).show();
        }
    }

    protected void setColorByMusic(int volumn) {
        Random random = new Random();
        int seed = random.nextInt(volumn);
        int a = random.nextInt(255 - seed);
        seed = random.nextInt(volumn);
        int r = random.nextInt(255 - seed);
        seed = random.nextInt(volumn);
        int g = random.nextInt(255 - seed);
        seed = random.nextInt(volumn);
        int b = random.nextInt(255 - seed);
        int color = Color.argb(a, r, g, b);
        setLightColor(color);
    }

    protected void setLightColor(int color) {
        String color_hex = Integer.toHexString(color).toLowerCase().replace("ff",
                "");
        String value = String.format("%s0000ff%s", color_hex, Integer.toHexString(255));
        Log.d(TAG, "value= " + value);
        if (isMesh) {
            sendDp(TuyaUtils.getMeshLightColor(color));
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("2", "colour");
            //map.put("2","scene");
            map.put("5", value);
            sendDp(map);
        }
    }

    //调光 改变亮度
    protected void setLight(int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("3", value);
        final String json = JSONObject.toJSONString(map);
        sendDp(json);
    }

    protected void setSu(int value) {
        try {
            int color = Color.WHITE;
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                String dp5 = map_dp.get("5").toString();
                String rgb_str = dp5.substring(0, 6);
                color = Color.parseColor("#" + rgb_str);
            } else {
//                if (mCurrentColor != -1) {
//                    color = mCurrentColor;
//                }
            }


            //16进制颜色转int
            //int i = (int) Long.parseLong("FF" + rgb_str, 16);
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[1] = value / 100;
            int convertColor = Color.HSVToColor(hsv);

            String color_hex = Integer.toHexString(convertColor).toLowerCase().replace("ff",
                    "");
            String dp5_convert = String.format("%s0000ff%s", color_hex, Integer.toHexString(255));
            Map<String, Object> map = new HashMap<>();
            map.put("2", "colour");
            map.put("5", dp5_convert);
            sendDp(map);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }


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
                        AlertPickDialog.AlertPickCallBack2() {
                            @Override
                            public void confirm(String value, boolean sw) {
                                Log.d(TAG, "定时关闭：" + value);
                                setTuyaTimer(value, sw);
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
                AlertPickDialog.AlertPickCallBack2() {
                    @Override
                    public void confirm(String value, boolean sw) {
                        setdelay(value, sw);
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

    protected void setdelay(String delay, boolean sw) {
        long currentTime = System.currentTimeMillis();
        long executeTime = currentTime + Integer.parseInt(delay) * 60 * 1000;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String time = format.format(new Date(executeTime));
        Log.d(TAG, "setdelay:" + time);
        setTuyaTimer(time, sw);
    }

    protected void setTuyaTimer(final String time, boolean sw) {
        if (!isGroup) {
            final TuyaTimerManager timerManager = new TuyaTimerManager();
            Map<String, Object> map = new HashMap<>();
            map.put("1", sw);
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
    }

    //夜灯
    protected void setNight() {
        // 亮度5% 色温50%
        int light = (int) (255 * 0.05);
        int temp = (int) (255 * 0.5);
        Map<String, Object> map = new HashMap<>();
        map.put("2", "white");
        map.put("3", 25);
        map.put("4", temp);
        final String json = JSONObject.toJSONString(map);
        sendDp(json);
    }

    protected void sendDp(String json) {
        Log.d(TAG, "sendDp:" + json);
        if (!isGroup) {
            if (isMesh) {
                mMeshDevice.publishDps(mNodeId, mCategory, json, new IResultCallback() {
                    @Override
                    public void onError(String s, String s1) {
                        Log.e(TAG, "onError:" + s + "," + s1);
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess");
                    }
                });

                return;
            }
            mTuyaDevice.publishDps(json, new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    Log.d(TAG, "onError:" + s + "," + s1);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess");
                }
            });
        } else {
            mTuyaGroup.publishDps(json, new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    Log.d(TAG, "onError:" + s + "," + s1);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess");
                }
            });
        }
    }

    protected void sendDp(Map<String, Object> dp) {
        final String value = JSONObject.toJSONString(dp);
        Log.d(TAG, "sendDp:" + value);
        sendDp(value);
    }

    class ListenMusic implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
//                int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
//                int current = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

                //Log.d(TAG, "Muisc Listen:current=" + current + ",max=" + max);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }
}
