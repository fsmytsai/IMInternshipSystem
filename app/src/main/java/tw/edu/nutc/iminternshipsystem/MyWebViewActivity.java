package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.just.library.AgentWeb;
import com.just.library.ChromeClientCallbackManager;

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
//        webView = (WebView) findViewById(R.id.webView);
//
//        webView.loadUrl("http://www.yahoo.com.tw");
//
//        // 是否資源 JavaScript 資源
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//
//        webView.setWebChromeClient(new WebChromeClient() {
//
//            //將網頁Title顯示到Acitity Title
//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                super.onReceivedTitle(view, title);
//
//                SetToolBar(title, true);
//            }
//
//        });

//        mAgentWeb = AgentWeb.with(this)//传入Activity or Fragment
//                .setAgentWebParent((LinearLayout) activity_Outer, new LinearLayout.LayoutParams(-1, -1))//传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
//                .useDefaultIndicator()// 使用默认进度条
//                .defaultProgressBarColor() // 使用默认进度条颜色
//                .setReceivedTitleCallback(new ChromeClientCallbackManager.ReceivedTitleCallback() {
//                    @Override
//                    public void onReceivedTitle(WebView view, String title) {
//                        if (mTitleTextView != null)
//                            mTitleTextView.setText(title);
//                    }
//                })
//                .setSecutityType(AgentWeb.SecurityType.strict)
//                .createAgentWeb()
//                .ready()
//                .go("http://tsaiweb.southeastasia.cloudapp.azure.com/aa9453aa/#Page=home_UnLog");
    }

}
