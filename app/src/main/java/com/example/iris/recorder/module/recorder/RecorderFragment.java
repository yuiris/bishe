package com.example.iris.recorder.module.recorder;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.example.iris.recorder.R;
import com.example.iris.recorder.api.audio.AudioCapturer;
import com.example.iris.recorder.api.audio.AudioPlayer;
import com.example.iris.recorder.api.wav.WavFileReader;
import com.example.iris.recorder.api.wav.WavFileWriter;
import com.example.iris.recorder.common.utils.Processor;
import com.example.iris.recorder.databinding.FragmentRecorderBinding;

import java.io.IOException;

/**
 * Created by iris on 2018/4/17.
 */

public class RecorderFragment extends Fragment implements View.OnClickListener, AudioCapturer.OnAudioFrameCapturedListener, CompoundButton.OnCheckedChangeListener {
    private static final String DEFAULT_TEST_FILE = Environment.getExternalStorageDirectory() + "/test.wav";

    private AudioCapturer mAudioCapturer;
    private WavFileWriter mWavFileWriter;

    //    private static final int SAMPLES_PER_FRAME = 1024;
    private AudioPlayer mAudioPlayer;
    private WavFileReader mWavFileReader;
    private volatile boolean mIsTestingExit = false;

    private Processor mProcessor = new Processor(8000);


    private View mRoot;
    private FragmentRecorderBinding binding;
    private ImageButton mRecorderButton, mReplayButton, mSaveButton;
    private VoiceLineView mVoiceLineView;
    private Chronometer mChronometer;
    private CheckBox mCbNoiseProcess;
    private boolean hasRecorderStart = false;
    private boolean hasReplayStart = false;
    private boolean hasnoiseProcess = false;

    public static RecorderFragment getNewInstance() {
        return new RecorderFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_recorder, null);
        binding = DataBindingUtil.bind(mRoot);
        binding.setOnClickListener(this);
        mRecorderButton = mRoot.findViewById(R.id.bt_recorder);
        mReplayButton = mRoot.findViewById(R.id.bt_replay);
        mSaveButton = mRoot.findViewById(R.id.bt_save);
        mVoiceLineView = mRoot.findViewById(R.id.voicLine);
        mChronometer = mRoot.findViewById(R.id.timer);
        mCbNoiseProcess = mRoot.findViewById(R.id.cb_noiseprocess);
        mCbNoiseProcess.setOnCheckedChangeListener(this);

        return mRoot;
    }

    /**
     * 选框监听
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            hasnoiseProcess = true;
        } else {
            hasnoiseProcess = false;
        }

    }

    /**
     * 点击监听
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_recorder:
                hasRecorderStart = !hasRecorderStart;
                if (hasRecorderStart) {
                    startRecord();

                    mVoiceLineView.run();

                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();

                    mRecorderButton.setBackgroundResource(R.drawable.ic_pause_circle_outline_black_24dp);
                    mReplayButton.setEnabled(false);
                } else {
                    stopRecord();
                    mChronometer.stop();
                    mRecorderButton.setBackgroundResource(R.drawable.btn_record);
                    mReplayButton.setEnabled(true);
                }
                break;
            case R.id.bt_replay:
                hasReplayStart = !hasReplayStart;
                if (hasReplayStart) {
                    startAudioPlayer();
//                    mReplayButton.setBackgroundResource(R.drawable.ic_pause_black_24dp);
                    mRecorderButton.setEnabled(false);
                    mReplayButton.setEnabled(false);
                } else {
                    stopAudioPlayer();
                    mReplayButton.setBackgroundResource(R.drawable.btn_replay);
                    mRecorderButton.setEnabled(true);
                    mReplayButton.setEnabled(true);
                }
                break;
            case R.id.bt_save:
                //TODO 保存录音
                saveAudio();

                break;
            default:
                break;
        }
    }

    /**
     * 开始录音
     */
    private void startRecord() {
        mAudioCapturer = new AudioCapturer();
        mWavFileWriter = new WavFileWriter();
        try {
            mWavFileWriter.openFile(DEFAULT_TEST_FILE, 8000, 1, 16);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioCapturer.setOnAudioFrameCapturedListener(this);
        mAudioCapturer.startCapture();

    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        mAudioCapturer.stopCapture();
        try {
            mWavFileWriter.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    private void startAudioPlayer() {
        mWavFileReader = new WavFileReader();
        mAudioPlayer = new AudioPlayer();

        try {
            mWavFileReader.openFile(DEFAULT_TEST_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mAudioPlayer.startPlayer();

        new Thread(AudioPlayRunnable).start();

    }

    private void stopAudioPlayer() {
        mIsTestingExit = true;
        mAudioPlayer.stopPlayer();
        hasReplayStart = false;
    }

    private void saveAudio() {
//        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/");
//        path.mkdirs();
//
//        try {
//            DataInputStream mDataInputStream = new DataInputStream(new FileInputStream(DEFAULT_TEST_FILE));
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(getActivity(), "文件获取失败" + e.toString(), Toast.LENGTH_SHORT).show();
//            return;
//        }

        Toast.makeText(getActivity(), "保存路径为 " + DEFAULT_TEST_FILE, Toast.LENGTH_SHORT).show();


    }

    /**
     * 播放线程
     */
    private Runnable AudioPlayRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[mAudioCapturer.getMinBufferSize()];
            while (!mIsTestingExit && mWavFileReader.readData(buffer, 0, buffer.length) > 0) {
                mAudioPlayer.play(buffer, 0, buffer.length);

                //得到实时音量
                int v = 0;
                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i] * buffer[i];
                }
                double mean = v / buffer.length;
                int volume = new Double(10 * Math.log10(mean)).intValue();
                Log.e("RecorderFragment", "volume is " + volume);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVoiceLineView.setVolume(volume);
                    }
                });


            }

            mAudioPlayer.stopPlayer();
            hasReplayStart = false;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mReplayButton.setBackgroundResource(R.drawable.btn_replay);
                    mRecorderButton.setEnabled(true);
                    mReplayButton.setEnabled(true);
                }
            });
            try {
                mWavFileReader.closeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 声音抓取
     */
    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        //降噪
        if (hasnoiseProcess == true) {
            mProcessor.processData(audioData);
        }
        mWavFileWriter.writeData(audioData, 0, audioData.length);


        //得到实时音量
        int v = 0;
        for (int i = 0; i < audioData.length; i++) {
            v += audioData[i] * audioData[i];
        }
        double mean = v / audioData.length;
        int volume = new Double(10 * Math.log10(mean)).intValue();
        Log.e("RecorderFragment", "volume is " + volume);

        mVoiceLineView.setVolume(volume);

    }
}
