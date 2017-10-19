package tw.edu.nutc.iminternshipsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ant.liao.GifView;

import MyMethod.SharedService;


/**
 * A simple {@link Fragment} subclass.
 */
public class SplashScreenFragment extends Fragment {

    public SplashScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_splash_screen, container, false);
        GifView gv_Loading = (GifView) view.findViewById(R.id.gv_Loading);
        gv_Loading.setGifImage(R.drawable.loading);
        gv_Loading.setShowDimension((int) SharedService.DipToPixels(getActivity(), 140), (int) SharedService.DipToPixels(getActivity(), 49));
        gv_Loading.setGifImageType(GifView.GifImageType.COVER);
        return view;
    }
}
