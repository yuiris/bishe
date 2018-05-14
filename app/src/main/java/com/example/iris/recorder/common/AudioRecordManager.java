package com.example.iris.recorder.common;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iris on 2018/5/3.
 */

public class AudioRecordManager {


    public static final String TAG = AudioRecordManager.class.getSimpleName();
    private static AudioRecordManager mInstance;

    // 音频源：音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 44100;
    // 音频通道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // 音频格式：PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区大小：缓冲区字节大小
    private int bufferSize= 0;
    // 录音对象
    private AudioRecord mRecord;
    // 文件名
    private String fileName;
    // 录音文件集合
    private List<String> filesName = new ArrayList<>();

    private DataOutputStream dos;
    private Thread recordThread;
    private boolean isStart = false;


    public AudioRecordManager() {

        //获得最小的缓冲区大小
        bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
        //创建AudioRecord实例
        mRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSize * 2);
    }

    /**
     * AudioRecordManager 单例获取
     */

    public static AudioRecordManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioRecordManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioRecordManager();
                }
            }
        }
        return mInstance;
    }




    /**
     * 销毁线程
     */
    private void destroyThread() {
        try {
            isStart = false;
            if (recordThread != null && recordThread.getState() == Thread.State.RUNNABLE) {
                try {
                    Thread.sleep(500);
                    recordThread.interrupt();
                } catch (Exception e) {
                    recordThread = null;
                }
            }
            recordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recordThread = null;
        }
    }


    /**
     * 启动录音线程
     */
    private void startThread() {
        destroyThread();
        isStart = true;
        if (recordThread == null) {
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }

    /**
     * 录音线程
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                int bytesRecord;
                byte[] tempBuffer = new byte[bufferSize];
                if (mRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    stopRecord();
                    return;
                }
                mRecord.startRecording();
                //writeToFileHead()
                while (isStart) {
                    if (mRecord != null) {
                        bytesRecord = mRecord.read(tempBuffer, 0, bufferSize);
                        if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION || bytesRecord == AudioRecord.ERROR_BAD_VALUE) {
                            continue;
                        }
                        if (bytesRecord != 0 && bytesRecord != AudioRecord.ERROR) {
                            //TODO 对音频数据二次处理 变声，压缩，降噪，增益等
                            dos.write(tempBuffer, 0, bytesRecord);
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 保存文件
     *
     * @param path
     * @throws Exception
     */
    private void setPath(String path) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            //TODO 弹出替换覆盖
            file.delete();
        }
        file.createNewFile();
        dos = new DataOutputStream(new FileOutputStream(file, true));
    }

    /**
     * 启动录音
     *
     * @param path
     */
    public void startRecord(String path) {
        try {
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        try {
            destroyThread();
            if (mRecord != null) {
                mRecord.stop();
                mRecord.release();
//                if (mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
//                    mRecord.stop();
//                }
//                if (mRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
//                    mRecord.release();
//                }
            }
            if (dos != null) {
                dos.flush();
                dos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
