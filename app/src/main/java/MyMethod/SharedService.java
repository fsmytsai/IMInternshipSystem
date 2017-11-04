package MyMethod;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ViewModel.IdentityView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tw.edu.nutc.iminternshipsystem.R;

/**
 * Created by user on 2017/4/8.
 */

public class SharedService {
    public static SharedPreferences sp_httpData;
    public static IdentityView identityView;
    public static String token;

    public static OkHttpClient GetClient(Context context) {
        return new OkHttpClient().newBuilder()
                .addInterceptor(new AddHeaderInterceptor(context))
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build();
    }

    private static class AddHeaderInterceptor implements Interceptor {
        private Context context;

        public AddHeaderInterceptor(Context mContext) {
            context = mContext;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            sp_httpData = context.getSharedPreferences("HttpData", context.MODE_PRIVATE);
            token = sp_httpData.getString("Token", "");
            try {
                request = request.newBuilder().addHeader("Authorization", token).build();
            }catch (Exception e){
                e.printStackTrace();
            }
            return chain.proceed(request);
        }
    }

    public static boolean CheckNetWork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public static int getActionBarSize(Context context) {
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarSize;
    }

    //開啟鍵盤
    public static void ShowKeyboard(Activity activity, View v) {
        v.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(v, 0);
    }

    //關閉鍵盤
    public static void HideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    //避免重複Toast
    private static Toast toast = null;

    public static void ShowTextToast(String msg, Context context) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public static void HandleError(int StatusCode, String ResMsg, Context context) {
        if (StatusCode == 400) {
            List<String> ErrorMsgs = new Gson().fromJson(ResMsg, new TypeToken<List<String>>() {
            }.getType());
            SharedService.ShowErrorDialog(ErrorMsgs, context);
        } else {
            SharedService.ShowErrorDialog("ERROR:" + StatusCode + "\n請告知客服人員", context);
        }
    }

    public static void ShowErrorDialog(String msg, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("錯誤訊息")
                .setMessage(msg)
                .setPositiveButton("知道了", null)
                .show();
    }

    public static void ShowErrorDialog(List<String> msgs, Context context) {
        String msg = "";
        for (int i = 0; i < msgs.size(); i++) {
            msg += msgs.get(i);
            if (i != msgs.size() - 1) {
                msg += "\n";
            }
        }
        new AlertDialog.Builder(context)
                .setTitle("錯誤訊息")
                .setMessage(msg)
                .setPositiveButton("知道了", null)
                .show();
    }

    public static Drawable CutBitmapToDrawable(Bitmap bitmap,Context context){
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;
        bitmap = Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static void ShowAndHideBlock(final View Block, ImageView iv_UpAndDown) {
        if (Block.getVisibility() == View.VISIBLE) {
            iv_UpAndDown.setImageResource(R.drawable.down);
            Block.animate()
                    .translationY(-Block.getHeight())
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            Block.setVisibility(View.GONE);
                        }
                    });
        } else {
            iv_UpAndDown.setImageResource(R.drawable.up);
            Block.setVisibility(View.VISIBLE);
            Block.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            Block.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public static float DipToPixels(Context context, float dipValue){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  dipValue, metrics);
    }
}
