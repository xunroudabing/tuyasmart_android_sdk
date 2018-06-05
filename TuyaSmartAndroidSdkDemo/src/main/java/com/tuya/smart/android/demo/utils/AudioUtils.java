package com.tuya.smart.android.demo.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by HanZheng(305058709@qq.com) on 2018-5-6.
 */

public class AudioUtils {
    public interface  ICallback{
        void onVolume(double v);
    }
    private final String TAG = "MediaRecord";
    ICallback mCallback;
    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;
    private String filePath;

    public AudioUtils(){
        this.filePath = "/dev/null";
    }

    public AudioUtils(File file) {
        this.filePath = file.getAbsolutePath();
    }
    public void setCallBack(ICallback callBack){
        mCallback = callBack;
    }
    private long startTime;
    private long endTime;

    /**
     * 開始录音 使用amr格式
     *
     *            录音文件
     * @return
     */
    public void startRecord() {
        // 開始录音
		/* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
			/* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
			/* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的採样 */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            			/*
			 * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
			 * 。H263视频/ARM音频编码)、MPEG-4、RAW_AMR(仅仅支持音频且音频编码要求为AMR_NB)
			 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			/* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
			/* ④開始 */
            mMediaRecorder.start();
            // AudioRecord audioRecord.
			/* 获取開始时间* */
            startTime = System.currentTimeMillis();
            updateMicStatus();
            Log.i("ACTION_START", "startTime" + startTime);
        } catch (IllegalStateException e) {
            Log.i(TAG,
                    "call startAmr(File mRecAudioFile) failed!"
                            + e.getMessage());
        } catch (IOException e) {
            Log.i(TAG,
                    "call startAmr(File mRecAudioFile) failed!"
                            + e.getMessage());
        }
    }

    /**
     * 停止录音
     *
     */
    public long stopRecord() {
        if (mMediaRecorder == null)
            return 0L;
        endTime = System.currentTimeMillis();
        Log.i("ACTION_END", "endTime" + endTime);
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        Log.i("ACTION_LENGTH", "Time" + (endTime - startTime));
        return endTime - startTime;
    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    /**
     * 更新话筒状态
     *
     */
    private int BASE = 1;
    private int SPACE = 500;// 间隔取样时间

    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double)mMediaRecorder.getMaxAmplitude() /BASE;
            double db = 0;// 分贝
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            Log.d(TAG,"分贝值："+db);
            if(mCallback != null){
                mCallback.onVolume(db);
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }
}
