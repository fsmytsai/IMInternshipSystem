package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import MyMethod.SharedService;
import MyMethod.ViewPagerAdapter;
import ViewModel.ResumeView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MyStudentResumeActivity extends MySharedActivity {
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabs;
    public ResumeView resumeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_student_resume);
        GetResumeData();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar(getIntent().getStringExtra("StudentName") + "的履歷", true);
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new MyStuBasicInfoFragment());
        fragments.add(new MyStuAbilityFragment());
        fragments.add(new MyStuJobExFragment());
        fragments.add(new MyStuWorkFragment());

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, this);
        viewPagerAdapter.tabTitles = new String[]{"", "", "", ""};
        viewPagerAdapter.tabIcons = new int[]{
                R.drawable.person,
                R.drawable.computer,
                R.drawable.work,
                R.drawable.outcome,
        };

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(viewPager);
    }

    public void GetResumeData() {
        int sid = getIntent().getIntExtra("Sid", -1);

        if (sid == -1) {
            resumeView = new Gson().fromJson(getIntent().getStringExtra("ResumeView"), ResumeView.class);
            initView();
            return;
        }

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getResumeDataBySid?sid=" + sid)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MyStudentResumeActivity.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatusCode = response.code();
                final String ResMsg = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            resumeView = new Gson().fromJson(ResMsg, ResumeView.class);
                            initView();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MyStudentResumeActivity.this);
                        }
                    }
                });

            }

        });
    }
}
