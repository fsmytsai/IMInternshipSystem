package tw.edu.nutc.iminternshipsystem;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import MyMethod.IMyImgCallBack;
import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MySharedFragment extends Fragment {
    public OkHttpClient client;
    public OkHttpClient imageClient;
    public List<ImageView> wImageViewList;
    public List<String> loadingImgNameList;
    public Activity activity;

    public MySharedFragment() {
        // Required empty public constructor
        wImageViewList = new ArrayList<>();
        loadingImgNameList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return null;
    }

    public void SetToolBar(String Title, View view) {
        RelativeLayout rl_toolBar = (RelativeLayout) view.findViewById(R.id.rl_ToolBar);
        TextView tv_ToolBar = (TextView) view.findViewById(R.id.tv_ToolBar);

        if (!Title.equals("")) {
            rl_toolBar.setVisibility(View.GONE);
            tv_ToolBar.setVisibility(View.VISIBLE);
            tv_ToolBar.setText(Title);
        } else {
            rl_toolBar.setVisibility(View.VISIBLE);
            tv_ToolBar.setVisibility(View.GONE);
            if (SharedService.identityView == null)
                rl_toolBar.findViewById(R.id.ib_Letter).setVisibility(View.GONE);
            else
                rl_toolBar.findViewById(R.id.ib_Letter).setVisibility(View.VISIBLE);
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setContentInsetsRelative(0, 0);

        View sb_Cover = view.findViewById(R.id.sb_Cover);
        if (sb_Cover != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Rect rectangle = new Rect();
            Window window = getActivity().getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int statusBarHeight = rectangle.top;
            sb_Cover.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight));
        }
    }

    public void SetCache(int Size) {
        cacheSizes = Size;
        mMemoryCaches = new LruCache<String, Bitmap>(cacheSizes) {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    private int cacheSizes = 0;

    private LruCache<String, Bitmap> mMemoryCaches;

    public Bitmap getBitmapFromLrucache(String ImgName) {
        if (mMemoryCaches != null)
            return mMemoryCaches.get(ImgName);
        return null;
    }

    public void addBitmapToLrucaches(String url, Bitmap bitmap) {
        if (mMemoryCaches != null && getBitmapFromLrucache(url) == null) {
            mMemoryCaches.put(url, bitmap);
        }
    }

    public void clearLruCache() {
        if (mMemoryCaches != null) {
            if (mMemoryCaches.size() > 0) {
                mMemoryCaches.evictAll();
            }
        }
    }

    public void showImage(ImageView imageView, String ImgName, boolean IsCut, IMyImgCallBack iMyImgCallBack) {

        Bitmap bitmap = getBitmapFromLrucache(ImgName);
        if (bitmap == null) {
            LoadImgByOkHttp(
                    imageView,
                    ImgName,
                    getString(R.string.BackEndPath) + "storage/user-upload/" + ImgName,
                    IsCut,
                    iMyImgCallBack
            );
        } else if (imageView != null) {
            if (IsCut)
                imageView.setImageDrawable(SharedService.CutBitmapToDrawable(bitmap, activity));
            else
                imageView.setImageBitmap(bitmap);
            if (iMyImgCallBack != null)
                iMyImgCallBack.CallBack();
        }
    }

    public void LoadImgByOkHttp(final ImageView imageView, final String ImgName, final String url, final boolean IsCut, final IMyImgCallBack iMyImgCallBack) {
        if (imageView != null) {
            for (ImageView mImgView : wImageViewList) {
                if (mImgView == imageView)
                    return;
            }
            wImageViewList.add(imageView);
        }

        for (String imgName : loadingImgNameList) {
            if (imgName.equals(ImgName))
                return;
        }
        loadingImgNameList.add(ImgName);

        Request request = new Request.Builder()
                .url(url)
                .build();

        imageClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                loadingImgNameList.remove(ImgName);
                for (int i = 0; i < wImageViewList.size(); i++) {
                    if (wImageViewList.get(i).getTag().equals(ImgName)) {
                        wImageViewList.remove(i);
                        i--;
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null) {
                                addBitmapToLrucaches(ImgName, bitmap);
                                loadingImgNameList.remove(ImgName);
                                for (int i = 0; i < wImageViewList.size(); i++) {
                                    if (wImageViewList.get(i).getTag().equals(ImgName)) {
                                        if (IsCut)
                                            wImageViewList.get(i).setImageDrawable(SharedService.CutBitmapToDrawable(bitmap, activity));
                                        else
                                            wImageViewList.get(i).setImageBitmap(bitmap);
                                        wImageViewList.remove(i);
                                        i--;
                                    }
                                }
                            }
                            Log.d("loadingImgNameListSize:", loadingImgNameList.size() + "");
                            Log.d("wImageViewListSize:", wImageViewList.size() + "");
                            if (iMyImgCallBack != null)
                                iMyImgCallBack.CallBack();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ClearAllImageRequest();
    }

    public void ClearAllImageRequest() {
        if (imageClient != null) {
            imageClient.dispatcher().cancelAll();
            wImageViewList = new ArrayList<>();
            loadingImgNameList = new ArrayList<>();
        }
    }

    public class LoadImgAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        private String imgName;
        private ImageView imageView;

        public LoadImgAsyncTask(ImageView imageView, String ImgName) {
            this.imgName = ImgName;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(getString(R.string.BackEndPath) + "storage/user-upload/" + imgName);
                bitmap = BitmapFactory.decodeStream(url.openStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Drawable[] layers = new Drawable[2];
            layers[0] = SharedService.CutBitmapToDrawable(bitmap, activity);
            layers[1] = ContextCompat.getDrawable(activity, R.drawable.editmimg);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            imageView.setImageDrawable(layerDrawable);
        }

    }
}
