package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import MyMethod.ViewPagerAdapter;
import ViewModel.JournalView;

public class JournalActivity extends MySharedActivity {
    public JournalView.Journal journal;

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        journal = new Gson().fromJson(getIntent().getStringExtra("Journal"), JournalView.Journal.class);
        initView();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar("我的週誌", true);
        activity_Outer = findViewById(R.id.ll_ActivityOuter);
        List<Fragment> fragments = new ArrayList<Fragment>();
        if (journal.grade_teacher == 0) {
            fragments.add(new EditJournalFragment());
        } else {
            fragments.add(new ShowJournalFragment());
            fragments.add(new JournalGradeFragment());
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, this);
        if (journal.grade_teacher == 0) {
            viewPagerAdapter.tabTitles = new String[]{"週誌"};
        } else {
            viewPagerAdapter.tabTitles = new String[]{"週誌", "評分"};
        }

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(viewPager);
    }
}
