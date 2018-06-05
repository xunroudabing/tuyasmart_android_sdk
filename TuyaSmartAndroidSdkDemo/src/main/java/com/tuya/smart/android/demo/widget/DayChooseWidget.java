package com.tuya.smart.android.demo.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-5-19.
 */

public class DayChooseWidget extends LinearLayout implements View.OnClickListener{
    boolean checked;
    TextView txtView;
    ImageView imgChk;
    static final String TAG = DayChooseWidget.class.getSimpleName();
    public DayChooseWidget(Context context){
        this(context,null);
    }
    public DayChooseWidget(Context context, @Nullable AttributeSet attrs){
        this(context,attrs,0);
    }
    public DayChooseWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.widget_day_choose,this,true);
        txtView = (TextView) findViewById(R.id.day_choose_txtDay);
        imgChk = (ImageView) findViewById(R.id.day_choose_imgChk);
        setOnClickListener(this);
    }

    public void setDay(CharSequence charSequence){
        txtView.setText(charSequence);
    }
    public String getDay(){
        return  txtView.getText().toString();
    }
    public void setChecked(boolean b){
        checked = b;
        if(checked){
            imgChk.setVisibility(View.VISIBLE);
        }else {
            imgChk.setVisibility(View.INVISIBLE);
        }
    }
    public boolean getChecked(){
        return  checked;
    }
    private void toggle(){
        checked = !checked;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        toggle();
        if(checked){
            imgChk.setVisibility(View.VISIBLE);
        }else {
            imgChk.setVisibility(View.INVISIBLE);
        }
    }
}
