package com.example.iris.recorder;


import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.iris.recorder.module.HomeFragment;
import com.example.iris.recorder.module.recorder.RecorderFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.home_content, RecorderFragment.getNewInstance()).commit();

    }
}
