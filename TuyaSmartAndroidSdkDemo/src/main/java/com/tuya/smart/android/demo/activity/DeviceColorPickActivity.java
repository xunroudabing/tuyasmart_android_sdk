package com.tuya.smart.android.demo.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.bean.DpLogBean;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.presenter.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.test.bean.AlertPickBean;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.test.widget.AlertPickDialog;
import com.tuya.smart.android.demo.utils.ProgressUtil;
import com.tuya.smart.android.demo.utils.ToastUtil;
import com.tuya.smart.android.demo.utils.TuyaUtils;
import com.tuya.smart.android.demo.utils.ViewUtils;
import com.tuya.smart.android.demo.view.ICommonDeviceDebugView;
import com.tuya.smart.bluemesh.mesh.device.ITuyaBlueMeshDevice;
import com.tuya.smart.home.interior.presenter.TuyaDevice;
import com.tuya.smart.home.interior.presenter.TuyaSmartDevice;
import com.tuya.smart.home.interior.presenter.TuyaTimerManager;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.GroupBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import colorpickerview.oden.com.colorpicker.ColorPickerView;

/**
 * 选择颜色
 * created by hanzheng(QQ:305058709) on 2018-2-8 10:14
 */

public class DeviceColorPickActivity extends BaseActivity implements View.OnClickListener, View
        .OnLongClickListener,
        SeekBar.OnSeekBarChangeListener, ICommonDeviceDebugView {
    public static final String INTENT_PRODUCTID = "INTENT_PRODUCTID";
    public static final String INTENT_DEVID = "intent_devId";
    public static final String INTENT_DPID = "intent_dpid";
    public static final String INTNET_TITLE = "intent_title";
    public static final String INTENT_ISGROUP = "INTENT_ISGROUP";
    public static final String INTENT_ISMESH = "INTENT_ISMESH";
    public static final String INTENT_MESH_NODEID = "INTENT_MESH_NODEID";
    public static final String INTENT_MESH_CATEGORY = "INTENT_MESH_CATEGORY";
    public static final String INTENT_GROUPID = "INTENT_GROUPID";
    static final String TAG = DeviceColorPickActivity.class.getSimpleName();
    int mValue;//明度
    int mSaturation;//饱和度
    TextView txtMode;
    boolean mWhiteMode = true;
    boolean mNightOn = false;
    boolean mSwitch = true;
    ImageButton btnScene, btnSwitch, btnNight, btnDelay, btnTimer;
    LinearLayout layoutLight, layoutTemp, layoutSu;
    ImageView imgPicker;
    ImageView imgColor1, imgColor2, imgColor3, imgColor4, imgColor5;
    TextView txtLightPer, txtTempPer, txtSuPer;
    SeekBar seekBarLight, seekBarColor, seekBarSu;
    ImageView btnAddColor;
    ColorPickerView colorPickerView;
    ColorPicker mColorPicker;
    int mCurrentColor = -8388864;
    Queue<Integer> mColorQueue = new LinkedBlockingQueue<Integer>(5);
    boolean sw = true;
    private boolean isGroup = false;
    private boolean isMesh = false;
    private long mGroupId;
    private String mDevId;
    private String mDpId;
    private String mProductId;
    private String mNodeId;
    private String mCategory;
    private TuyaDevice mTuyaDevice;
    private ITuyaGroup mTuyaGroup;
    private GroupBean mGroupBean;
    private ITuyaBlueMeshDevice mMeshDevice;
    private CommonDeviceDebugPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_color_pick);
        initToolbar();
        initViews();
        initMenu();
        initPresenter();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    private void initPresenter() {
        mPresenter = new CommonDeviceDebugPresenter(this, this);
    }

    protected void initMenu() {
        setDisplayHomeAsUpEnabled();
        String title = getIntent().getStringExtra(INTNET_TITLE);
        setTitle(title);
        if (isGroup) {
            setMenu(R.menu.toolbar_group_function, new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_edit_group:
                            renameGroup();
                            break;
                        case R.id.action_manage_group:
                            manageGroup();
                            break;
                        case R.id.action_del_group:
                            deleteGroup();
                            break;
                    }
                    return false;
                }
            });
        } else {
            setMenu(R.menu.toolbar_top_smart_device, new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
//                    case R.id.action_test_mode:
//                        mPresenter.testMode();
//                        break;
                        case R.id.action_rename:
                            mPresenter.renameDevice();
                            break;
                        case R.id.action_close:
                            finish();
                            break;
                        case R.id.action_check_update:
                            mPresenter.checkUpdate();
                            break;
                        case R.id.action_resume_factory_reset:
                            mPresenter.resetFactory();
                            break;
                        case R.id.action_add_group:
                            Intent intent = new Intent(DeviceColorPickActivity.this,
                                    AddGroupActivity

                                            .class);
                            intent.putExtras(getIntent());
                            startActivity(intent);
                            break;
                        case R.id.action_unconnect:
                            mPresenter.removeDevice();
                            break;
                    }
                    return false;
                }
            });
        }
    }

    protected void initViews() {
        isGroup = getIntent().getBooleanExtra(INTENT_ISGROUP, false);
        mGroupId = getIntent().getLongExtra(INTENT_GROUPID, 0);
        mDevId = getIntent().getStringExtra(INTENT_DEVID);
        mDpId = getIntent().getStringExtra(INTENT_DPID);
        mProductId = getIntent().getStringExtra(INTENT_PRODUCTID);
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
            mGroupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
        }
        if (isMesh) {
            mMeshDevice = TuyaHomeSdk.newBlueMeshDeviceInstance(CommonConfig.getMeshId
                    (getApplicationContext()));
        }
        layoutLight = (LinearLayout) findViewById(R.id.device_color_layoutLight);
        layoutTemp = (LinearLayout) findViewById(R.id.device_color_layoutTemp);
        layoutSu = (LinearLayout) findViewById(R.id.device_color_layoutSu);
        txtLightPer = (TextView) findViewById(R.id.device_color_txtLightPercent);
        txtTempPer = (TextView) findViewById(R.id.device_color_txtTempPercent);
        txtSuPer = (TextView) findViewById(R.id.device_color_txtSuPercent);
        mColorPicker = (ColorPicker) findViewById(R.id.holo_colorpicker);
        seekBarLight = (SeekBar) findViewById(R.id.device_color_seekbarLight);
        seekBarColor = (SeekBar) findViewById(R.id.device_color_seekbarTemp);
        seekBarSu = (SeekBar) findViewById(R.id.device_color_seekbarSu);
        seekBarLight.setOnSeekBarChangeListener(this);
        seekBarColor.setOnSeekBarChangeListener(this);
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
        imgColor1 = (ImageView) findViewById(R.id.device_colorpick_imgColor1);
        imgColor2 = (ImageView) findViewById(R.id.device_colorpick_imgColor2);
        imgColor3 = (ImageView) findViewById(R.id.device_colorpick_imgColor3);
        imgColor4 = (ImageView) findViewById(R.id.device_colorpick_imgColor4);
        imgColor5 = (ImageView) findViewById(R.id.device_colorpick_imgColor5);
        imgColor1.setOnClickListener(this);
        imgColor2.setOnClickListener(this);
        imgColor3.setOnClickListener(this);
        imgColor4.setOnClickListener(this);
        imgColor5.setOnClickListener(this);
        imgColor1.setOnLongClickListener(this);
        imgColor2.setOnLongClickListener(this);
        imgColor3.setOnLongClickListener(this);
        imgColor4.setOnLongClickListener(this);
        imgColor5.setOnLongClickListener(this);
        btnAddColor = (ImageView) findViewById(R.id.device_colorpick_btnAddColor);
        btnAddColor.setOnClickListener(this);
        txtMode = (TextView) findViewById(R.id.device_color_txtMode);
        txtMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLightMode();
            }
        });
        imgPicker = (ImageView) findViewById(R.id.img_picker);
        colorPickerView = (ColorPickerView) findViewById(R.id.color_picker);
        colorPickerView.setImgPicker(this, imgPicker, 10);
        colorPickerView.setColorChangedListener(new ColorPickerView.onColorChangedListener() {
            @Override
            public void colorChanged(int red, int blue, int green) {

                mCurrentColor = Color.rgb(red, green, blue);
                Log.d(TAG, "colorChanged:" + mCurrentColor + ",hex=" + Integer.toHexString
                        (mCurrentColor));
                txtMode.setTextColor(mCurrentColor);
                String color_hex = Integer.toHexString(mCurrentColor).toLowerCase().replace("ff",
                        "");
                String value = String.format("%s0000ff%s", color_hex, Integer.toHexString(255));
                Log.d(TAG, "value= " + value);
                Map<String, Object> map = new HashMap<>();
                map.put("2", "colour");
                //map.put("2","scene");
                map.put("5", value);
                sendDp(map);
                layoutSu.setVisibility(View.VISIBLE);
                layoutTemp.setVisibility(View.GONE);
            }

            @Override
            public void stopColorChanged(int red, int blue, int green) {

            }
        });

        mColorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                mCurrentColor = color;
                Log.d(TAG, "colorChanged:" + mCurrentColor + ",hex=" + Integer.toHexString
                        (mCurrentColor));
                if (!mWhiteMode) {
                    txtMode.setTextColor(mCurrentColor);
                    String color_hex = Integer.toHexString(mCurrentColor).toLowerCase().substring
                            (2);
                    if (isMesh) {
                        sendDp(TuyaUtils.getMeshLightColor(mCurrentColor));
                    } else {
                        String value = String.format("%s0000ff%s", color_hex, Integer.toHexString
                                (255));
                        Log.d(TAG, "value= " + value);
                        Map<String, Object> map = new HashMap<>();
                        map.put("2", "colour");
                        //map.put("2","scene");
                        map.put("5", value);
                        sendDp(map);
                    }
//                    layoutSu.setVisibility(View.VISIBLE);
//                    layoutTemp.setVisibility(View.GONE);
                }
            }
        });
    }

    protected void initData() {
        try {
            if (!isGroup) {
                Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
                ;
                if (map_dp != null) {
                    String color = "";
                    int value_light = 0;
                    int value_temp = 0;
                    boolean b = (boolean) map_dp.get("1");
                    if (isMesh) {
                        color = (String) map_dp.get("109");
                        int i3 = (int) map_dp.get("3");
                        value_light = 255 * i3 / 100;
                        value_temp = (int) map_dp.get("104");
                        int i101 = (int) map_dp.get("101");
                        int i102 = (int) map_dp.get("102");
                        int i103 = (int) map_dp.get("103");
                        if (color.equals("colour")) {
                            mCurrentColor = Color.rgb(i101, i102, i103);
                        }
                        Log.d(TAG, "[mesh]mCurrentColor=" + mCurrentColor);
                    } else {
                        color = (String) map_dp.get("2");
                        value_light = (int) map_dp.get("3");
                        value_temp = (int) map_dp.get("4");
                    }
                    if (map_dp.containsKey("5")) {
                        String dp5 = map_dp.get("5").toString();
                        String rgb_str = dp5.substring(0, 6);
                        mCurrentColor = Color.parseColor("#" + rgb_str);
                    }
                    if (b) {
                        btnSwitch.setImageResource(R.drawable.btn_switch_off);
                    } else {
                        btnSwitch.setImageResource(R.drawable.btn_switch);
                    }
                    seekBarLight.setProgress(value_light);
                    seekBarColor.setProgress(value_temp);

                    int light_per = value_light * 100 / 255;
                    int temp_per = value_temp * 100 / 255;
                    txtLightPer.setText(String.valueOf(light_per) + "%");
                    txtTempPer.setText(String.valueOf(temp_per) + "%");
                    txtSuPer.setText("100%");
                    if (color.equals("white")) {
                        mWhiteMode = true;
                        layoutTemp.setVisibility(View.VISIBLE);
                        layoutSu.setVisibility(View.GONE);
                    } else {
                        mWhiteMode = false;
                        txtMode.setTextColor(mCurrentColor);
                        mColorPicker.setColor(mCurrentColor);
                        layoutSu.setVisibility(View.VISIBLE);
                        layoutTemp.setVisibility(View.GONE);
                    }
                }
            } else {
                layoutTemp.setVisibility(View.VISIBLE);
                layoutSu.setVisibility(View.GONE);
            }

            //初始化颜色列表
            String json = CommonConfig.getBindColorList(getApplicationContext());
            if (!TextUtils.isEmpty(json)) {
                try {
                    JSONArray array = JSONArray.parseArray(json);
                    for (int i = 0; i < array.size(); i++) {
                        Integer v = array.getInteger(i);
                        mColorQueue.add(v);
                    }
                    bindColorList();
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.device_colorpick_imgColor1:
            case R.id.device_colorpick_imgColor2:
            case R.id.device_colorpick_imgColor3:
            case R.id.device_colorpick_imgColor4:
            case R.id.device_colorpick_imgColor5:
                try {
                    Object tag = v.getTag();
                    if (tag != null) {
                        Integer color = (Integer) tag;
                        setLightColor(color);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            //添加颜色
            case R.id.device_colorpick_btnAddColor:
                try {
                    if (mCurrentColor == -1) {
                        Toast.makeText(this, R.string.alert_colorisnull, Toast.LENGTH_SHORT).show();
                    } else {
                        addColor(mCurrentColor);
                        bindColorList();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
            //情景
            case R.id.device_color_btnScene:
                Intent intent = new Intent(this, DeviceModePickActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
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

    protected void addColor(int color) {
        if (mColorQueue.size() < 5) {
            Integer head = mColorQueue.peek();
            if (head == null || head != (Integer) color) {
                mColorQueue.add(color);
            }
        } else {
            mColorQueue.poll();
            mColorQueue.add(color);
        }
    }

    protected void bindColorList() {
        JSONArray array = new JSONArray();
        Iterator<Integer> iterator = mColorQueue.iterator();
        ImageView[] views = {imgColor1, imgColor2, imgColor3, imgColor4, imgColor5};
        int i = 0;
        while (iterator.hasNext()) {
            Integer color = iterator.next();
            Log.d(TAG, "bindColorList:" + color);
            Drawable drawable = createLayerDrawable(color);
            views[i].setImageDrawable(drawable);
            views[i].setTag(color);
            array.add(color.toString());
            i++;
        }

        for (int j = i; j < views.length; j++) {
            views[j].setImageResource(R.drawable.ic_colorpick_button);
        }

        String json = array.toJSONString();
        CommonConfig.setBindColorList(getApplicationContext(), json);
        Log.d(TAG, "bindColorList:" + json);
    }

    protected Drawable createLayerDrawable(int color) {
        Drawable bgDrawable = ViewUtils.getDrawable(this, R.drawable.ic_colorpick_button);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        Drawable[] array = {bgDrawable, shapeDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(array);
        layerDrawable.setLayerInset(1, 15, 15, 15, 15);
        return layerDrawable;
    }

    protected void renameGroup() {
        DialogUtil.simpleInputDialog(this, getString(R.string.rename), getToolBar().getTitle(),
                false, new DialogUtil.SimpleInputDialogInterface() {
                    @Override
                    public void onPositive(DialogInterface dialog, String inputText) {
                        int limit = getResources().getInteger(R.integer
                                .change_device_name_limit);
                        if (inputText.length() > limit) {
                            ToastUtil.showToast(DeviceColorPickActivity.this, R.string
                                    .ty_modify_device_name_length_limit);
                        } else {
                            renameGroupTitleToServer(inputText);
                        }
                    }

                    @Override
                    public void onNegative(DialogInterface dialog) {

                    }
                });
    }

    private void renameGroupTitleToServer(final String titleName) {
        ProgressUtil.showLoading(this, R.string.loading);
        mTuyaGroup.renameGroup(titleName, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                ToastUtil.showToast(DeviceColorPickActivity.this, s1);
            }

            @Override
            public void onSuccess() {
                ProgressUtil.hideLoading();
                setTitle(titleName);
            }
        });
    }

    protected void manageGroup() {
        Intent intent = new Intent(getApplicationContext(), ManageGroupActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    protected void deleteGroup() {
        DialogUtil.simpleConfirmDialog(DeviceColorPickActivity.this, getString(R.string
                .alert_confirm_delete_group), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mTuyaGroup.dismissGroup(new IResultCallback() {
                        @Override
                        public void onError(String s, String s1) {
                            String error = "解散群组失败:" + s1;
                            Toast.makeText(DeviceColorPickActivity.this, error,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            Toast.makeText(DeviceColorPickActivity.this, R.string
                                            .alert_del_group_success,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else {
                    dialog.dismiss();
                }
            }
        });

    }

    protected void setLightColor(int color) {
        String color_hex = Integer.toHexString(color).toLowerCase().substring(2);
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
        txtMode.setTextColor(getResources().getColor(R.color.red));
        layoutSu.setVisibility(View.VISIBLE);
        layoutTemp.setVisibility(View.GONE);
        Toast.makeText(DeviceColorPickActivity.this, R.string.alert_color_mode, Toast
                .LENGTH_SHORT).show();
    }

    //开关
    protected void setSwitch() {
        if (!isGroup) {
            Map<String, Object> map_dp = TuyaSmartDevice.getInstance().getDps(mDevId);
            boolean b = (boolean) map_dp.get("1");
            Map<String, Object> map = new HashMap<>();
            map.put("1", !b);
            if (!b) {
                btnSwitch.setImageResource(R.drawable.btn_switch_off);
            } else {
                btnSwitch.setImageResource(R.drawable.btn_switch);
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
                btnSwitch.setImageResource(R.drawable.btn_switch_off);
            } else {
                btnSwitch.setImageResource(R.drawable.btn_switch);
            }
        }

    }

    //白光模式 彩光模式
    protected void setLightMode() {
        mWhiteMode = !mWhiteMode;
        if (mWhiteMode) {
            setWhiteLight();
            txtMode.setTextColor(getResources().getColor(R.color.text_color));
            Toast.makeText(DeviceColorPickActivity.this, R.string.alert_white_mode, Toast
                    .LENGTH_SHORT).show();
        } else {
            txtMode.setTextColor(getResources().getColor(R.color.red));
            layoutSu.setVisibility(View.VISIBLE);
            layoutTemp.setVisibility(View.GONE);
            Toast.makeText(DeviceColorPickActivity.this, R.string.alert_color_mode, Toast
                    .LENGTH_SHORT).show();
            setLightColor(mCurrentColor);
        }
    }

    //白光
    protected void setWhiteLight() {
        if (isMesh) {
            Map<String, Object> map = new HashMap<>();
            map.put("109", "white");
            final String json = JSONObject.toJSONString(map);
            sendDp(json);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("2", "white");
            final String json = JSONObject.toJSONString(map);
            sendDp(json);
        }
        layoutTemp.setVisibility(View.VISIBLE);
        layoutSu.setVisibility(View.GONE);
    }

    //调光 改变亮度
    protected void setLight(int value) {
        if (mWhiteMode) {
            Map<String, Object> map = new HashMap<>();
            if (isMesh) {
                int v = value * 100 / 255;
                map.put("3", v);
            } else {
                map.put("3", value);
            }
            final String json = JSONObject.toJSONString(map);
            sendDp(json);


        } else {
            int color = mCurrentColor;
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] = (float) value / 100;
            int convertColor = Color.HSVToColor(hsv);

            String color_hex = Integer.toHexString(convertColor).toLowerCase().substring(2);
            Log.d(TAG, "setSu.color_hex=" + color_hex);
            String dp5_convert = String.format("%s0000ff%s", color_hex, Integer.toHexString(255));
            if (isMesh) {
                sendDp(TuyaUtils.getMeshLightColor(convertColor));
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("2", "colour");
                map.put("5", dp5_convert);
                sendDp(map);
            }
        }
        int per = value * 100 / 255;
        txtLightPer.setText(String.valueOf(per) + "%");
    }

    protected void setSu(int value) {
        try {
            int color = Color.WHITE;
            if (!isGroup) {
//                Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
//                String dp5 = map_dp.get("5").toString();
//                String rgb_str = dp5.substring(0, 6);
//                Log.d(TAG, "setSu.rgb_str=" + rgb_str + ",dp5=" + dp5 + ",value=" + value);
//                color = Color.parseColor("#" + rgb_str);
                color = mCurrentColor;
            } else {
                if (mCurrentColor != -1) {
                    color = mCurrentColor;
                }
            }

            int progress = seekBarLight.getProgress();
            int p = Math.min(progress, 230);
            //***********
//            int red = Color.red(color);
//            int green = Color.green(color);
//            int blue = Color.blue(color);
//            int newRed = 0;
//            int newGreen = 0;
//            int newBlue = 0;
//            if (red < 50) {
//                newRed = 0;
//            } else if (red > 250) {
//                newRed = 255;
//            }
//            if (green < 50) {
//                newGreen = 0;
//            } else if (green > 250) {
//                newGreen = 255;
//            }
//            if (blue < 50) {
//                newBlue = 0;
//            } else if (blue > 250) {
//                newBlue = 255;
//            }
//            int newColor = Color.rgb(newRed, newGreen, newBlue);
            //16进制颜色转int
            //int i = (int) Long.parseLong("FF" + rgb_str, 16);
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            float hsv1 = (float) (value / 100F);
            float hsv2 = (float) (p / 230F);
            float h1 = 0.6F + (0.4F * (value / 100F));
            hsv[1] = h1;
            hsv[2] = hsv2;
            Log.d(TAG, "hsv[1]=" + hsv[1] + ",hsv[2]=" + hsv[2]);
            int convertColor = Color.HSVToColor(hsv);

            String color_hex = Integer.toHexString(convertColor).toLowerCase().substring(2);
            Log.d(TAG, "setSu.color_hex=" + color_hex);
            String dp5_convert = String.format("%s0000ff%s", color_hex, Integer.toHexString(255));
            if (isMesh) {
                sendDp(TuyaUtils.getMeshLightColor(convertColor));
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("2", "colour");
                map.put("5", dp5_convert);
                sendDp(map);
            }
            int per = value * 100 / 100;
            txtSuPer.setText(String.valueOf(per) + "%");
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }


//        String s = String.format("%sffffff%s", "ffffff", Integer.toHexString(value));
//        Map<String, Object> map = new HashMap<>();
//        map.put("2", "colour");
//        //map.put("2","scene");
//        map.put("5", s);
//        map.put("3", value);
//        final String json = JSONObject.toJSONString(map);


    }

    //色温
    protected void setTemp(int value) {
        if (isMesh) {
            final String json = JSONObject.toJSONString(TuyaUtils.getMeshTemp(value));
            sendDp(json);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("4", value);
            final String json = JSONObject.toJSONString(map);
            sendDp(json);
        }


        int per = value * 100 / 255;
        txtTempPer.setText(String.valueOf(per) + "%");
    }

    protected void setTimer() {
        Intent intent = new Intent(this, TimerListActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int min = calendar.get(Calendar.MINUTE);
//        Log.d(TAG, "calendar.hour=" + hour + ",min=" + min);
//        ArrayList<String> minutes = new ArrayList<>();
//        ArrayList<String> hours = new ArrayList<>();
//        for (int i = 0; i <= 60; i++) {
//            minutes.add(String.format("%02d", i));
//        }
//        for (int i = 0; i < 24; i++) {
//            hours.add(String.format("%02d", i));
//        }
//        final AlertPickBean alertPickBean1 = new AlertPickBean();
//        alertPickBean1.setLoop(true);
//        alertPickBean1.setCancelText(getString(R.string.cancel));
//        alertPickBean1.setConfirmText(getString(R.string.confirm));
//        alertPickBean1.setRangeKeys(hours);
//        alertPickBean1.setRangeValues(hours);
//        alertPickBean1.setSelected(hour);
//        alertPickBean1.setTitle(getString(R.string.title_choose_timer));
//
//        final AlertPickBean alertPickBean2 = new AlertPickBean();
//        alertPickBean2.setLoop(true);
//        alertPickBean2.setCancelText(getString(R.string.cancel));
//        alertPickBean2.setConfirmText(getString(R.string.confirm));
//        alertPickBean2.setRangeKeys(minutes);
//        alertPickBean2.setRangeValues(minutes);
//        alertPickBean2.setTitle(getString(R.string.title_choose_timer));
//        alertPickBean2.setSelected(min);
//        AlertPickDialog.showTimePickAlertPickDialog(DeviceColorPickActivity.this, alertPickBean1,
//                alertPickBean2, new
//                        AlertPickDialog.AlertPickCallBack2() {
//                            @Override
//                            public void confirm(String value, boolean sw) {
//                                Log.d(TAG, "定时关闭：" + value);
//                                setTuyaTimer(value, sw);
//                            }
//
//                            @Override
//                            public void cancel() {
//
//                            }
//                        });
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
        AlertPickDialog.showAlertPickDialog(DeviceColorPickActivity.this, alertPickBean, new
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
        } else {
            final TuyaTimerManager timerManager = new TuyaTimerManager();
            Map<String, Object> map = new HashMap<>();
            map.put("1", sw);
            timerManager.addTimerWithTask("timer", String.valueOf(mGroupId), "0000000", map,
                    time, new
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
        int light = (int) (230 * 0.05);
        int temp = (int) (255 * 0.5);
        if (!mNightOn) {
            // 亮度5% 色温50%
            Map<String, Object> map = new HashMap<>();
            if (isMesh) {
                map.put("109", "white");
                map.put("3", 5);
                map.put("104", temp);
                map.put("105", 255 - temp);
            } else {
                map.put("2", "white");
                map.put("3", 25);
                map.put("4", temp);
            }

            final String json = JSONObject.toJSONString(map);
            sendDp(json);
            mNightOn = true;
        } else {
            light = 255;
            temp = (int) (255 * 0.5);//夜灯关上色温改为50%
            Map<String, Object> map = new HashMap<>();
            if (isMesh) {
                map.put("109", "white");
                map.put("3", 100);
                map.put("104", temp);
                map.put("105", 255 - temp);
            } else {
                map.put("2", "white");
                map.put("3", 255);
                map.put("4", temp);
            }
            final String json = JSONObject.toJSONString(map);
            sendDp(json);
            mNightOn = false;
        }
        int light_per = light * 100 / 255;
        int temp_per = temp * 100 / 255;
        seekBarLight.setProgress(light);
        seekBarColor.setProgress(temp);
        txtLightPer.setText(String.valueOf(light_per) + "%");
        txtTempPer.setText(String.valueOf(temp_per) + "%");
        if (mNightOn) {
            btnNight.setImageResource(R.drawable.btn_night_on);
        } else {
            btnNight.setImageResource(R.drawable.btn_night);
        }
        layoutTemp.setVisibility(View.VISIBLE);
        layoutSu.setVisibility(View.GONE);
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
            if (isMesh) {
                mMeshDevice.multicastDps(mGroupBean.getLocalId(), mGroupBean.getCategory(), json,
                        new IResultCallback() {

                            @Override
                            public void onError(String s, String s1) {
                                Log.d(TAG, "onError:" + s + "," + s1);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, " mMeshDevice.multicastDps onSuccess");
                            }
                        });
                return;
            }
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
                //色温
                case R.id.device_color_seekbarTemp:
                    try {
                        mSaturation = progress;
                        setTemp(mSaturation);
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

    @Override
    public void updateView(String dpStr) {

    }

    @Override
    public void logError(String error) {

    }

    @Override
    public void logSuccess(String dpStr) {

    }

    @Override
    public void logSuccess(DpLogBean logBean) {

    }

    @Override
    public void logError(DpLogBean logBean) {

    }

    @Override
    public void logDpReport(String dpStr) {

    }

    @Override
    public void deviceRemoved() {
        DialogUtil.simpleSmartDialog(this, R.string.device_has_unbinded, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    @Override
    public void deviceOnlineStatusChanged(boolean online) {

    }

    @Override
    public void onNetworkStatusChanged(boolean status) {

    }

    @Override
    public void devInfoUpdate() {

    }

    @Override
    public void updateTitle(String titleName) {

    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.device_colorpick_imgColor1:
            case R.id.device_colorpick_imgColor2:
            case R.id.device_colorpick_imgColor3:
            case R.id.device_colorpick_imgColor4:
            case R.id.device_colorpick_imgColor5:
                try {
                    final View view = v;
                    Object tag = view.getTag();
                    if (tag == null) {
                        break;
                    }
                    DialogUtil.simpleConfirmDialog(DeviceColorPickActivity.this, getString(R
                            .string.dialog_delcolor), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (which == Dialog.BUTTON_POSITIVE) {
                                Object tag = view.getTag();
                                if (tag != null) {
                                    Integer color = (Integer) tag;
                                    mColorQueue.remove(color);
                                    bindColorList();
                                }
                            }
                        }
                    });

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                break;
        }
        return true;
    }
}
