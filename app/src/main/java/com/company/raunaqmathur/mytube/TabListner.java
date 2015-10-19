package com.company.raunaqmathur.mytube;

/**
 * Created by raunaqmathur on 10/18/15.
 */



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabListner extends FragmentPagerAdapter {

    public TabListner(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Search fragment activity
                return new SearchFragment();
            case 1:
                // Favorite fragment activity
                return new FavoriteFragment();

        }

        return null;
    }

    @Override
    public int getCount() {

        return 2;
    }
}