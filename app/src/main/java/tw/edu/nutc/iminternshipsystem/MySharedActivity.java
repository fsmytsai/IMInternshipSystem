package tw.edu.nutc.iminternshipsystem;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import MyMethod.IMyImgCallBack;
import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MySharedActivity extends AppCompatActivity {
    public OkHttpClient client;
    public int myWidth;

    public List<ImageView> wImageViewList;
    public List<String> loadingImgNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = SharedService.GetClient(this);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        myWidth = dm.widthPixels;
        wImageViewList = new ArrayList<>();
        loadingImgNameList = new ArrayList<>();
    }

    public void SetToolBar(String Title, boolean HasBack) {
        RelativeLayout rl_toolBar = (RelativeLayout) findViewById(R.id.rl_ToolBar);
        TextView tv_ToolBar = (TextView) findViewById(R.id.tv_ToolBar);

        if (!Title.equals("")) {
            rl_toolBar.setVisibility(View.GONE);
            tv_ToolBar.setVisibility(View.VISIBLE);
            tv_ToolBar.setText(Title);
        } else {
            rl_toolBar.setVisibility(View.VISIBLE);
            tv_ToolBar.setVisibility(View.GONE);
        }

        if (HasBack) {
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);  //取消Toolbar的內建靠左title(像Actionbar的特性)
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public View activity_Outer;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View v = getCurrentFocus();
        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];
            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
                    .getBottom())) {

                SharedService.HideKeyboard(this);
                if (activity_Outer != null)
                    activity_Outer.requestFocus();
            }
        }
        boolean ret = super.dispatchTouchEvent(event);
        return ret;
    }


    //cache
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
        if (mMemoryCaches != null) {
            return mMemoryCaches.get(ImgName);
        }
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
        } else {
            if (IsCut)
                imageView.setImageDrawable(SharedService.CutBitmapToDrawable(bitmap, this));
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

        client.newCall(request).enqueue(new Callback() {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null) {
                                addBitmapToLrucaches(ImgName, bitmap);
                                loadingImgNameList.remove(ImgName);
                                for (int i = 0; i < wImageViewList.size(); i++) {
                                    if (wImageViewList.get(i).getTag().equals(ImgName)) {
                                        if (IsCut)
                                            wImageViewList.get(i).setImageDrawable(SharedService.CutBitmapToDrawable(bitmap, MySharedActivity.this));
                                        else
                                            wImageViewList.get(i).setImageBitmap(bitmap);
                                        wImageViewList.remove(i);
                                        i--;
                                    }
                                }
                            }
                            Log.d("loadingImgNameListSize:", loadingImgNameList.size() + "");
                            Log.d("wImageViewListSize:", wImageViewList.size() + "");
                            if (wImageViewList.size() == 0 && iMyImgCallBack != null)
                                iMyImgCallBack.CallBack();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
