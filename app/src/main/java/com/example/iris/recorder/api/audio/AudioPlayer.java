/*
 *  COPYRIGHT NOTICE
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *
 *  @license under the Apache License, Version 2.0
 *
 *  @file    AudioPlayer.java
 *
 *  @version 1.0
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.example.iris.recorder.api.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioPlayer {

    private static final String TAG = "AudioPlayer";

    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int DEFAULT_SAMPLE_RATE = 8000;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;

    private volatile boolean mIsPlayStarted = false;
    private AudioTrack mAudioTrack;

    //返回开始播放结果
    public boolean startPlayer() {
        return startPlayer(DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    //判断是否播放成功
    public boolean startPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat) {
        //已经开始播放
        if (mIsPlayStarted) {
            Log.e(TAG, "Player already started !");
            return false;
        }

        //最小缓冲区大小无效
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSizeInBytes == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.i(TAG, "getMinBufferSize = " + bufferSizeInBytes + " bytes !");

        //播放器初始化失败
        mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, DEFAULT_PLAY_MODE);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
            return false;
        }

        mIsPlayStarted = true;

        Log.i(TAG, "Start audio player success !");

        return true;
    }

    //停止播放
    public void stopPlayer() {
        if (!mIsPlayStarted) {
            return;
        }

        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }

        mAudioTrack.release();
        mIsPlayStarted = false;

        Log.i(TAG, "Stop audio player success !");
    }

    /**
     * 播放录音
     *
     * @param audioData     the array that holds the data to play.
     * @param offsetInBytes the offset expressed in bytes in audioData where the data to write
     *                      starts.
     *                      Must not be negative, or cause the data access to go out of bounds of the array.
     * @param sizeInBytes   the number of bytes to write in audioData after the offset.
     *                      Must not be negative, or cause the data access to go out of bounds of the array.
     * @return zero or the positive number of bytes that were written, or one of the following
     * error codes. The number of bytes will be a multiple of the frame size in bytes
     * not to exceed sizeInBytes.
     */
    public boolean play(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (!mIsPlayStarted) {
            Log.e(TAG, "Player not started !");
            return false;
        }

        if (mAudioTrack.write(audioData, offsetInBytes, sizeInBytes) != sizeInBytes) {
            Log.e(TAG, "Could not write all the samples to the audio device !");
        }

        mAudioTrack.play();

        Log.d(TAG, "OK, Played " + sizeInBytes + " bytes !");

        return true;
    }
}
