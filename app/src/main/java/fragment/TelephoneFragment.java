package fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.yumeng.tillo.R;

import java.util.ArrayList;

import base.BaseFragment;

/**
 * tab:通话
 */

public class TelephoneFragment extends BaseFragment {
    private SegmentTabLayout segmentTabLayout;
    private String[] mTitles = {"所有通话", "未接来电"};
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ViewPager viewPager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_telephone;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        segmentTabLayout = myFindViewsById(R.id.fragment_telephone_stl_tab);
        viewPager = myFindViewsById(R.id.fragment_telephone_vp_pager);
        viewPager.setAdapter(new MyPagerAdapter(getFragmentManager()));
        segmentTabLayout.setTabData(mTitles);
        segmentTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {


            }
        });
    }



    public void initFragment(){}


    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }


}
