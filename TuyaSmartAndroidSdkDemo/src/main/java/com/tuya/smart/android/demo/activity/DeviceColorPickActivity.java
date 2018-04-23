package com.tuya.smart.android.demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.bean.DpLogBean;
import com.tuya.smart.android.demo.presenter.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.test.bean.AlertPickBean;
import com.tuya.smart.android.demo.test.utils.DialogUtil;
import com.tuya.smart.android.demo.test.widget.AlertPickDialog;
import com.tuya.smart.android.demo.utils.ViewUtils;
import com.tuya.smart.android.demo.view.ICommonDeviceDebugView;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaTimerManager;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class DeviceColorPickActivity extends BaseActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, ICommonDeviceDebugView {
    public static final String INTENT_PRODUCTID = "INTENT_PRODUCTID";
    public static final String INTENT_DEVID = "intent_devId";
    public static final String INTENT_DPID = "intent_dpid";
    public static final String INTNET_TITLE = "intent_title";
    static final String TAG = DeviceColorPickActivity.class.getSimpleName();
    int mValue;//明度
    int mSaturation;//饱和度
    TextView txtMode;
    boolean mSwitch = true;
    ImageButton btnScene, btnSwitch, btnNight, btnDelay, btnTimer;
    ImageView imgPicker;
    ImageView imgColor1, imgColor2, imgColor3, imgColor4, imgColor5;
    SeekBar seekBarLight, seekBarColor, seekBarSu;
    ImageView btnAddColor;
    ColorPickerView colorPickerView;
    int mCurrentColor = -1;
    Queue<Integer> mColorQueue = new LinkedBlockingQueue<Integer>(5);
    private String mDevId;
    private String mDpId;
    private String mProductId;
    private TuyaDevice mTuyaDevice;
    private CommonDeviceDebugPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_color_pick);
        initToolbar();
        initMenu();
        initViews();
        initPresenter();
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
                    case R.id.action_unconnect:
                        mPresenter.removeDevice();
                        break;
                }
                return false;
            }
        });
    }

    protected void initViews() {
        mDevId = getIntent().getStringExtra(INTENT_DEVID);
        mDpId = getIntent().getStringExtra(INTENT_DPID);
        mProductId = getIntent().getStringExtra(INTENT_PRODUCTID);
        mTuyaDevice = new TuyaDevice(mDevId);
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
        btnAddColor = (ImageView) findViewById(R.id.device_colorpick_btnAddColor);
        btnAddColor.setOnClickListener(this);
        txtMode = (TextView) findViewById(R.id.device_color_txtMode);
        txtMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWhiteLight();
            }
        });
        imgPicker = (ImageView) findViewById(R.id.img_picker);
        colorPickerView = (ColorPickerView) findViewById(R.id.color_picker);
        //colorPickerView.setImgPicker(this, imgPicker, 10);
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
                Map<String, String> map = new HashMap<>();
                map.put("2", "colour");
                //map.put("2","scene");
                map.put("5", value);
                sendDp(map);
            }

            @Override
            public void stopColorChanged(int red, int blue, int green) {

            }
        });
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
        Iterator<Integer> iterator = mColorQueue.iterator();
        ImageView[] views = {imgColor1, imgColor2, imgColor3, imgColor4, imgColor5};
        int i = 0;
        while (iterator.hasNext()) {
            Integer color = iterator.next();
            Drawable drawable = createLayerDrawable(color);
            views[i].setImageDrawable(drawable);
            i++;
        }
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

    //开关
    protected void setSwitch() {
        Map<String, Object> map_dp = TuyaUser.getDeviceInstance().getDev(mDevId).getDps();
        boolean b = (boolean) map_dp.get("1");
        Map<String, Object> map = new HashMap<>();
        map.put("1", !b);

        final String value = JSONObject.toJSONString(map);
        sendDp(value);
    }

    //白光
    protected void setWhiteLight() {
        Map<String, Object> map = new HashMap<>();
        map.put("2", "white");
        final String json = JSONObject.toJSONString(map);
        sendDp(json);
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
        AlertPickDialog.showTimePickAlertPickDialog(DeviceColorPickActivity.this, alertPickBean1,
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
        AlertPickDialog.showAlertPickDialog(DeviceColorPickActivity.this, alertPickBean, new
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
}
