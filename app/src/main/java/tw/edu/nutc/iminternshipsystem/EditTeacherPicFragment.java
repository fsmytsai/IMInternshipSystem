package tw.edu.nutc.iminternshipsystem;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import MyMethod.FileChooser;
import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditTeacherPicFragment extends MySharedFragment {
    private MainActivity mainActivity;
    private ImageView iv_MImg;

    private LoadImgAsyncTask loadImgAsyncTask;

    private final int REQUEST_EXTERNAL_STORAGE = 18;
    private FileChooser fileChooser;
    private File mImg = null;

    public EditTeacherPicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_teacher_pic, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        super.activity = mainActivity;
        initViews(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        if (loadImgAsyncTask != null) {
            loadImgAsyncTask.cancel(true);
        }
        super.onDestroyView();
    }

    private void initViews(final View view) {
        SetToolBar("", view);
        iv_MImg = (ImageView) view.findViewById(R.id.iv_MImg);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (mainActivity.myWidth * 0.7), (int) (mainActivity.myWidth * 0.7));
        iv_MImg.setLayoutParams(layoutParams);
        if (SharedService.identityView.profilePic != null) {
            loadImgAsyncTask = new LoadImgAsyncTask(iv_MImg, SharedService.identityView.profilePic);
            loadImgAsyncTask.execute();
        } else {
            Drawable[] layers = new Drawable[2];
            layers[0] = ContextCompat.getDrawable(mainActivity, R.drawable.defaultmimg);
            layers[1] = ContextCompat.getDrawable(mainActivity, R.drawable.editmimg);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            iv_MImg.setImageDrawable(layerDrawable);
        }

        iv_MImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    fileChooser = new FileChooser(mainActivity, EditTeacherPicFragment.this);
                    if (!fileChooser.showFileChooser("image/*", null, false, true)) {
                        SharedService.ShowTextToast("您沒有適合的檔案選取器", getActivity());
                    }
                } else {
                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        view.findViewById(R.id.bt_ResetPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fileChooser = new FileChooser(getActivity(), EditTeacherPicFragment.this);
                    if (!fileChooser.showFileChooser("image/*", null, false, true)) {
                        SharedService.ShowTextToast("您沒有適合的檔案選取器", getActivity());
                    }
                } else {
                    SharedService.ShowTextToast("您拒絕選取檔案", getActivity());
                }
                return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FileChooser.ACTIVITY_FILE_CHOOSER:
                if (fileChooser.onActivityResult(requestCode, resultCode, data)) {
                    File[] files = fileChooser.getChosenFiles();
                    mImg = files[0];
                    Drawable[] layers = new Drawable[2];
                    layers[0] = SharedService.CutBitmapToDrawable(BitmapFactory.decodeFile(mImg.getAbsolutePath()), mainActivity);
                    layers[1] = ContextCompat.getDrawable(mainActivity, R.drawable.editmimg);
                    LayerDrawable layerDrawable = new LayerDrawable(layers);
                    iv_MImg.setImageDrawable(layerDrawable);
                    EditTeacherPic();
                }
                return;
        }
    }

    public void EditTeacherPic() {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        String FileName = mImg.getName();
        String[] Type = FileName.split(Pattern.quote("."));
        if (Type[Type.length - 1].equalsIgnoreCase("jpg"))
            Type[Type.length - 1] = "jpeg";
        builder.addFormDataPart("profilePic", mImg.getName(), RequestBody.create(MediaType.parse("image/" + Type[Type.length - 1]), mImg));

        RequestBody body = builder.build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/teacherUploadProfilePic")
                .post(body)
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
                mainActivity.runOnUiThread(new Runnable() {//这是Activity的方法，会在主线程执行任务
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("修改大頭貼成功", mainActivity);
                            mainActivity.getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.MainFrameLayout, new HomeFragment(), "HomeFragment")
                                    .commit();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });


    }
}
