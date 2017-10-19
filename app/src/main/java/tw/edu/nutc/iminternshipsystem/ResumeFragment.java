package tw.edu.nutc.iminternshipsystem;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class ResumeFragment extends MySharedFragment {

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabs;
    private MainActivity mainActivity;
    private ResumeView resumeView;

    public ResumeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resume, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        initView(view);
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new BasicInfoFragment());
        fragments.add(new StudentAbilityFragment());
        fragments.add(new JobExperienceFragment());
        fragments.add(new StudentWorkFragment());

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments, mainActivity);
        viewPagerAdapter.tabTitles = new String[]{"", "", "", ""};
        viewPagerAdapter.tabIcons = new int[]{
                R.drawable.person,
                R.drawable.computer,
                R.drawable.work,
                R.drawable.outcome,
        };

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabs = (TabLayout) view.findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(viewPager);
    }

    public void GetResumeData() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/findResumeDataById")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", mainActivity);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatusCode = response.code();
                final String ResMsg = response.body().string();

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            resumeView = new Gson().fromJson(ResMsg, ResumeView.class);
                            getBasicInfoFragment().DrawData(resumeView);
                            getStudentAbilityFragment().DrawData(resumeView);
                            getJobExperienceFragment().DrawData(resumeView);
                            getStudentWorkFragment().DrawData(resumeView);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    public BasicInfoFragment getBasicInfoFragment() {
        return (BasicInfoFragment) getChildFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager + ":0");
    }

    public StudentAbilityFragment getStudentAbilityFragment() {
        return (StudentAbilityFragment) getChildFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager + ":1");
    }

    public JobExperienceFragment getJobExperienceFragment() {
        return (JobExperienceFragment) getChildFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager + ":2");
    }

    public StudentWorkFragment getStudentWorkFragment() {
        return (StudentWorkFragment) getChildFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager + ":3");
    }

}
