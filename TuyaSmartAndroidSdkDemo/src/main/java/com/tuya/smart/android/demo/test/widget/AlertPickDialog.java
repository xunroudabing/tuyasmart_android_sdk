package com.tuya.smart.android.demo.test.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.test.bean.AlertPickBean;


/**
 * Created by letian on 15/6/13.
 */
public class AlertPickDialog {

    private static final String TAG = "ChooseDialog";
    public static void showSeekBarPickDialog(Activity activity,int progress, final AlertPickCallBack2 callBack){
        final AlertDialog dialog = new AlertDialog.Builder(activity, R.style.dialog_alert).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.ty_dialog_seekbar_pick);
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.dialog_style);  //添加动画
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.getDecorView().setPadding(0, 0, 0, 0);

        SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.dialog_seekbar_bar);
        final TextView txtValue = (TextView) dialog.findViewById(R.id.dialog_seekbar_txtValue);
        TextView btnOK = (TextView) dialog.findViewById(R.id.dialog_seekbar_btnOk);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    int p = (progress * 100) / 255 ;
                    txtValue.setText(String.valueOf(p) + "%");
                    if(callBack != null){
                        callBack.confirm(String.valueOf(progress),true);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int p = (progress * 100) / 255 ;
        txtValue.setText(String.valueOf(p) + "%");
        seekBar.setProgress(progress);
    }
    public static void showTimePickAlertPickDialog(Activity activity, final AlertPickBean alertPickBean1,final AlertPickBean alertPickBean2, final AlertPickCallBack2 alertPickCallBack){
        final AlertDialog dialog = new AlertDialog.Builder(activity, R.style.dialog_alert).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.ty_dialog_time_pick);
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.dialog_style);  //添加动画
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.getDecorView().setPadding(0, 0, 0, 0);
        final NumberPicker numberPicker1 = (NumberPicker) dialog.findViewById(R.id.np_choose);
        final NumberPicker numberPicker2 = (NumberPicker) dialog.findViewById(R.id.np_choose2);
        TextView sureTV = (TextView) dialog.findViewById(R.id.tv_sure);
        TextView cancelTV = (TextView) dialog.findViewById(R.id.tv_cancel);
        TextView titleTV = (TextView) dialog.findViewById(R.id.tv_title);
        final Switch swView = (Switch) dialog.findViewById(R.id.action_item_switch);
        sureTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = String.format("%s:%s",alertPickBean1.getRangeKeys().get(numberPicker1.getValue()),alertPickBean2.getRangeKeys().get(numberPicker2.getValue()));
                alertPickCallBack.confirm(value,swView.isChecked());
                dialog.cancel();
            }
        });
        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertPickCallBack.cancel();
                dialog.cancel();
            }
        });
        sureTV.setText(alertPickBean1.getConfirmText());
        cancelTV.setText(alertPickBean1.getCancelText());
        titleTV.setText(alertPickBean1.getTitle());
        initData(numberPicker1, alertPickBean1);
        initData(numberPicker2,alertPickBean2);
    }
    public static void showAlertPickDialog(Activity activity, final AlertPickBean alertPickBean, final AlertPickCallBack2 alertPickCallBack) {
        final AlertDialog dialog = new AlertDialog.Builder(activity, R.style.dialog_alert).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_action_item);
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.dialog_style);  //添加动画
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.getDecorView().setPadding(0, 0, 0, 0);
        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.np_choose);
        TextView sureTV = (TextView) dialog.findViewById(R.id.tv_sure);
        TextView cancelTV = (TextView) dialog.findViewById(R.id.tv_cancel);
        TextView titleTV = (TextView) dialog.findViewById(R.id.tv_title);
        final Switch swView = (Switch) dialog.findViewById(R.id.action_item_switch);

        sureTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertPickCallBack.confirm(
                        alertPickBean.getRangeKeys().get(numberPicker.getValue()),swView.isChecked());
                dialog.cancel();
            }
        });
        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertPickCallBack.cancel();
                dialog.cancel();
            }
        });
        sureTV.setText(alertPickBean.getConfirmText());
        cancelTV.setText(alertPickBean.getCancelText());
        titleTV.setText(alertPickBean.getTitle());
        initData(numberPicker, alertPickBean);
    }

    private static void initData(NumberPicker numberPicker, AlertPickBean alertPickBean) {
        String[] value = new String[alertPickBean.getRangeValues().size()];
        value = alertPickBean.getRangeValues().toArray(value);
        numberPicker.setDisplayedValues(value);
        numberPicker.setMaxValue(value.length - 1);
        numberPicker.setValue(alertPickBean.getSelected());
        numberPicker.setWrapSelectorWheel(alertPickBean.isLoop());
    }

    public interface AlertPickCallBack2 {
        void confirm(String value,boolean sw);

        void cancel();
    }
}
