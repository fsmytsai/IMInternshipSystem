package tw.edu.nutc.iminternshipsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReLinkFragment extends MySharedFragment {

    private MainActivity mainActivity;

    public ReLinkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_re_link, container, false);

        mainActivity = (MainActivity) getActivity();
        SetToolBar("請檢察網路連線", view);
        LinearLayout ll_ReLink = (LinearLayout) view.findViewById(R.id.ll_ReLink);
        ll_ReLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.CheckLogon();
            }
        });
        return view;
    }

}
