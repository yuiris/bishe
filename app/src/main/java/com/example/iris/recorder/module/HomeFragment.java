package com.example.iris.recorder.module;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.iris.recorder.R;
import com.example.iris.recorder.module.recorder.RecorderFragment;


/**
 * Created by iris on 2018/4/17.
 */

public class HomeFragment extends Fragment {
    private static final int NUM_ITEMS = 3;

    private View mRoot;
    private TabLayout mTableLayout;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_home, null);
        mTableLayout = mRoot.findViewById(R.id.home_tablayout);
        mViewPager=mRoot.findViewById(R.id.home_pager);
        MyFragmentPagerAdapter myFragmentPagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(myFragmentPagerAdapter);
        mTableLayout.setupWithViewPager(mViewPager);
        return mRoot;
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RecorderFragment.getNewInstance();
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Recorder";
                default:
                    break;
            }
            return null;
        }
    }
}
