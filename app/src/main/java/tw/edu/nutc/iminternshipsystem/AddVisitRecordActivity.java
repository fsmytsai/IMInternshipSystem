package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import MyMethod.ViewPagerAdapter;

public class AddVisitRecordActivity extends MySharedActivity {
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit_record);
        initView();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar("新增訪視紀錄", true);
        activity_Outer = findViewById(R.id.ll_ActivityOuter);
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new VisitCompanyFragment());
        fragments.add(new VisitStudentFragment());

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, this);
        viewPagerAdapter.tabTitles = new String[]{"實習機構", "實習生"};

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(viewPager);
    }
}
