package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.os.Bundle;

public class MyWebViewActivity extends MySharedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_web_view);
        initViews();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initViews() {
        SetToolBar("載入中...", true);
        activity_Outer = findViewById(R.id.ll_ActivityOuter);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.FrameLayout, new MyWebViewFragment(), "MyWebViewFragment")
                .commit();
    }

}
