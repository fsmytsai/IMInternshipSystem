package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.os.Bundle;

public class AboutUsActivity extends MySharedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        SetToolBar("關於我們", true);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }
}
