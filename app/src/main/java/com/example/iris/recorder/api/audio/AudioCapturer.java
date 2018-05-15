/*
 *  COPYRIGHT NOTICE
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *
 *  @license under the Apache License, Version 2.0
 *
 *  @file    AudioCapturer.java
 *
 *  @version 1.0
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.example.iris.recorder.api.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.iris.recorder.common.utils.Processor;

public class AudioCapturer {

    private static final String TAG = "AudioCapturer";


    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    //采样频率
    private static final int DEFAULT_SAMPLE_RATE = 8000;
    //单双声道
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //采样位数
    private static final int DEFAULT_DATA_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Make sure the sample size is the same in different devices
//    private static final int SAMPLES_PER_FRAME = 1024;

    private int minBufferSize;

    private AudioRecord mAudioRecord;
    //处理管理类
    private Processor mProcessor;

    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;
    private boolean isProcessing = true;

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    //录音捕获接口
    public interface OnAudioFrameCapturedListener {
        void onAudioFrameCaptured(byte[] audioData);
    }

    public boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    //返回声音录制开始结果
    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_DATA_FORMAT);
    }

    //判断是否开始获取
    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        //已经在开始状态
        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        //最小缓冲区大小无效
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }

        //录制初始化失败
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, minBufferSize * 4);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        //开始录制
        mAudioRecord.startRecording();

        //进入Loop，开始录制线程
        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        //录制状态设置为true
        mIsCaptureStarted = true;

        //初始化处理程序
        mProcessor = new Processor(DEFAULT_SAMPLE_RATE);
        mProcessor.initProcessor();

        Log.i(TAG, "Start audio capture success !");

        return true;
    }

    public int getMinBufferSize(){
        return minBufferSize;
    }

    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }

        //退出Loop
        mIsLoopExit = true;
        try {
            //中断线程
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();
        mProcessor.releaseProcessor();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;

        Log.i(TAG, "Stop audio capture success !");
    }

    private class AudioCaptureRunnable implements Runnable {
        @Override
        public void run() {
            while (!mIsLoopExit) {
                byte[] buffer = new byte[minBufferSize];
                //读取缓存区数据
                int ret = mAudioRecord.read(buffer, 0, buffer.length);
//                byte[] tempBuffer = new byte[ret];
//                if (isProcessing) {
//                    mProcessor.processData(tempBuffer);
//                }

                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {
                    //正在录音，监听缓存区
                    Log.d("TAG", "Audio captured: " + buffer.length);
//                    Log.d("TAG", "Processor buffer: " + tempBuffer.length);
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                }
            }
        }
    }
}
