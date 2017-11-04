package tw.edu.nutc.iminternshipsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.just.library.AgentWeb;
import com.just.library.ChromeClientCallbackManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyWebViewFragment extends Fragment {

    private AgentWeb mAgentWeb;
    private MyWebViewActivity myWebViewActivity;

    public MyWebViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_web_view, container, false);
        myWebViewActivity = (MyWebViewActivity) getActivity();
        LinearLayout ll_Container = (LinearLayout) view.findViewById(R.id.ll_Container);
        String Link = myWebViewActivity.getIntent().getStringExtra("URL");
        if (!Link.contains("http"))
            Link = "http://" + Link;
        mAgentWeb = AgentWeb.with(myWebViewActivity)
                .setAgentWebParent(ll_Container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .defaultProgressBarColor()
                .setReceivedTitleCallback(new ChromeClientCallbackManager.ReceivedTitleCallback() {
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        if (title.length() > 15)
                            title = title.substring(0, 14) + "...";
                        myWebViewActivity.SetToolBar(title, true);
                    }
                })
                .createAgentWeb()
                .ready()
                .go(Link);
        return view;
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroyView();
    }
}
