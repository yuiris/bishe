package com.example.iris.recorder.common.utils;

import com.example.iris.recorder.WebrtcProcessor;

public class Processor {
    private WebrtcProcessor mProcessor;
    private int frequency;

    public Processor(int frequency) {
        mProcessor=new WebrtcProcessor();
        this.frequency=frequency;

    }

    public void initProcessor() {
        mProcessor = new WebrtcProcessor();
        mProcessor.init(frequency);
    }

    public void releaseProcessor() {
        if (mProcessor != null) {
            mProcessor.release();
        }
    }

    public void processData(byte[] data) {
        if (mProcessor != null) {
            mProcessor.processNoise(data);
        }
    }

    public void processData(short[] data) {
        if (mProcessor != null) {
            mProcessor.processNoise(data);
        }
    }
}
