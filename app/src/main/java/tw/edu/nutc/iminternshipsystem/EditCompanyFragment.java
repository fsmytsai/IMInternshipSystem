package tw.edu.nutc.iminternshipsystem;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import MyMethod.FileChooser;
import MyMethod.SharedService;
import ViewModel.CompanyView;
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
public class EditCompanyFragment extends MySharedFragment {
    private CompanyView.Company company;
    private MainActivity mainActivity;
    private ImageView iv_MImg;
    private EditText et_CompanyName;
    private EditText et_CompanyType;
    private EditText et_CompanyAddress;
    private EditText et_CompanyFax;
    private EditText et_CompanyEmpolyeeNum;
    private EditText et_CompanyTel;
    private EditText et_CompanyIntro;

    private final int REQUEST_EXTERNAL_STORAGE = 18;
    private FileChooser fileChooser;
    private File mImg = null;

    public EditCompanyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_company, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        initView(view);
        GetCompanyData();
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        iv_MImg = (ImageView) view.findViewById(R.id.iv_MImg);
        Drawable[] layers = new Drawable[2];
        layers[0] = ContextCompat.getDrawable(mainActivity, R.drawable.defaultmimg);
        layers[1] = ContextCompat.getDrawable(mainActivity, R.drawable.editmimg);
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        iv_MImg.setImageDrawable(layerDrawable);
        iv_MImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    fileChooser = new FileChooser(mainActivity, EditCompanyFragment.this);
                    if (!fileChooser.showFileChooser("image/*", null, false, true)) {
                        SharedService.ShowTextToast("您沒有適合的檔案選取器", getActivity());
                    }
                } else {
                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });
        et_CompanyName = (EditText) view.findViewById(R.id.et_CompanyName);
        et_CompanyType = (EditText) view.findViewById(R.id.et_CompanyType);
        et_CompanyAddress = (EditText) view.findViewById(R.id.et_CompanyAddress);
        et_CompanyFax = (EditText) view.findViewById(R.id.et_CompanyFax);
        et_CompanyEmpolyeeNum = (EditText) view.findViewById(R.id.et_CompanyEmpolyeeNum);
        et_CompanyTel = (EditText) view.findViewById(R.id.et_CompanyTel);
        et_CompanyIntro = (EditText) view.findViewById(R.id.et_CompanyIntro);
        view.findViewById(R.id.iv_Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCompany();
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
                    fileChooser = new FileChooser(getActivity(), EditCompanyFragment.this);
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
                }
                return;
        }
    }

    private void GetCompanyData() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getCompanyDetailsByToken")
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            company = new Gson().fromJson(ResMsg, CompanyView.Company.class);
                            if (company.profilePic != null)
                                new LoadImgAsyncTask(iv_MImg, company.profilePic).execute();

                            et_CompanyName.setText(company.c_name);
                            et_CompanyType.setText(company.ctypes);
                            et_CompanyAddress.setText(company.caddress);
                            et_CompanyFax.setText(company.cfax);
                            et_CompanyEmpolyeeNum.setText(company.cempolyee_num + "");
                            et_CompanyTel.setText(company.tel);
                            et_CompanyIntro.setText(company.cintroduction);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
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
            if (bitmap != null)
                layers[0] = SharedService.CutBitmapToDrawable(bitmap, mainActivity);
            else
                layers[0] = ContextCompat.getDrawable(mainActivity, R.drawable.defaultmimg);
            layers[1] = ContextCompat.getDrawable(mainActivity, R.drawable.editmimg);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            imageView.setImageDrawable(layerDrawable);
        }

    }

    private void EditCompany() {
        SharedService.HideKeyboard(mainActivity);
        mainActivity.activity_Outer.requestFocus();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (mImg != null) {
            String FileName = mImg.getName();
            String[] Type = FileName.split(Pattern.quote("."));
            if (Type[Type.length - 1].equalsIgnoreCase("jpg"))
                Type[Type.length - 1] = "jpeg";
            builder.addFormDataPart("profilePic", mImg.getName(), RequestBody.create(MediaType.parse("image/" + Type[Type.length - 1]), mImg));
        }

        builder.addFormDataPart("c_name", et_CompanyName.getText().toString())
                .addFormDataPart("ctypes", et_CompanyType.getText().toString())
                .addFormDataPart("caddress", et_CompanyAddress.getText().toString())
                .addFormDataPart("cfax", et_CompanyFax.getText().toString())
                .addFormDataPart("cempolyee_num", et_CompanyEmpolyeeNum.getText().toString())
                .addFormDataPart("u_tel", et_CompanyTel.getText().toString())
                .addFormDataPart("cintroduction", et_CompanyIntro.getText().toString())
                .build();

        RequestBody body = builder.build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/editCompanyDetails")
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("修改廠商資料成功", mainActivity);
                            mainActivity.CheckLogon();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }
}
