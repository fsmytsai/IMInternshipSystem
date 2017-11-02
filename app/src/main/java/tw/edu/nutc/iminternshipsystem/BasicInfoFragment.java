package tw.edu.nutc.iminternshipsystem;


import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import MyMethod.FileChooser;
import MyMethod.SharedService;
import ViewModel.ResumeView;
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
public class BasicInfoFragment extends MySharedFragment {


    private MainActivity mainActivity;
    private ImageView iv_MImg;

    private EditText et_ChName;
    private EditText et_EgName;
    private EditText et_BirthPlace;
    private EditText et_BirthDay;
    private EditText et_ResidenceAddress;
    private EditText et_Email;
    private EditText et_Cellphone;
    private RadioGroup rg_Gender;
    private RadioGroup rg_EnglishSpeak;
    private RadioGroup rg_EnglishRead;
    private RadioGroup rg_EnglishWrite;
    private EditText et_TOEICGrade;
    private EditText et_TOEFLGrade;
    private EditText et_SencondName;
    private RadioGroup rg_SecondSpeak;
    private RadioGroup rg_SecondRead;
    private RadioGroup rg_SecondWrite;
    private RadioGroup rg_NowEducation;
    private EditText et_GraduateYear;
    private EditText et_GraduateSchool;
    private EditText et_GraduateSection;
    private EditText et_GraduateDepartment;
    private EditText et_Intro;

    private DatePickerDialog datePickerDialog;

    private final int REQUEST_EXTERNAL_STORAGE = 18;
    private FileChooser fileChooser;
    private File mImg = null;

    public BasicInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_basic_info, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        super.activity = mainActivity;
        initViews(view);
        return view;
    }

    private void initViews(final View view) {
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
                    fileChooser = new FileChooser(mainActivity, BasicInfoFragment.this);
                    if (!fileChooser.showFileChooser("image/*", null, false, true)) {
                        SharedService.ShowTextToast("您沒有適合的檔案選取器", getActivity());
                    }
                } else {
                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        et_ChName = (EditText) view.findViewById(R.id.et_ChName);
        et_EgName = (EditText) view.findViewById(R.id.et_EgName);
        et_BirthPlace = (EditText) view.findViewById(R.id.et_BirthPlace);
        et_BirthDay = (EditText) view.findViewById(R.id.et_BirthDay);
        et_ResidenceAddress = (EditText) view.findViewById(R.id.et_ResidenceAddress);
        et_Email = (EditText) view.findViewById(R.id.et_Email);
        et_Cellphone = (EditText) view.findViewById(R.id.et_Cellphone);
        rg_Gender = (RadioGroup) view.findViewById(R.id.rg_Gender);
        rg_EnglishSpeak = (RadioGroup) view.findViewById(R.id.rg_EnglishSpeak);
        rg_EnglishRead = (RadioGroup) view.findViewById(R.id.rg_EnglishRead);
        rg_EnglishWrite = (RadioGroup) view.findViewById(R.id.rg_EnglishWrite);
        et_TOEICGrade = (EditText) view.findViewById(R.id.et_TOEICGrade);
        et_TOEFLGrade = (EditText) view.findViewById(R.id.et_TOEFLGrade);
        et_SencondName = (EditText) view.findViewById(R.id.et_SencondName);
        rg_SecondSpeak = (RadioGroup) view.findViewById(R.id.rg_SecondSpeak);
        rg_SecondRead = (RadioGroup) view.findViewById(R.id.rg_SecondRead);
        rg_SecondWrite = (RadioGroup) view.findViewById(R.id.rg_SecondWrite);
        rg_NowEducation = (RadioGroup) view.findViewById(R.id.rg_NowEducation);
        et_GraduateYear = (EditText) view.findViewById(R.id.et_GraduateYear);
        et_GraduateSchool = (EditText) view.findViewById(R.id.et_GraduateSchool);
        et_GraduateSection = (EditText) view.findViewById(R.id.et_GraduateSection);
        et_GraduateDepartment = (EditText) view.findViewById(R.id.et_GraduateDepartment);
        et_Intro = (EditText) view.findViewById(R.id.et_Intro);

        setDateTimeField();
        et_BirthDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        view.findViewById(R.id.ll_BasicInfoTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedService.ShowAndHideBlock(view.findViewById(R.id.ll_BasicInfo), (ImageView) v.findViewById(R.id.iv_BasicInfoUp));
            }
        });

        view.findViewById(R.id.ll_LanguageTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedService.ShowAndHideBlock(view.findViewById(R.id.ll_Language), (ImageView) v.findViewById(R.id.iv_LanguageUp));
            }
        });

        view.findViewById(R.id.ll_EducationTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedService.ShowAndHideBlock(view.findViewById(R.id.ll_Education), (ImageView) v.findViewById(R.id.iv_EducationUp));
            }
        });

        view.findViewById(R.id.ll_IntroTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedService.ShowAndHideBlock(view.findViewById(R.id.ll_Intro), (ImageView) v.findViewById(R.id.iv_IntroUp));
            }
        });

        view.findViewById(R.id.iv_Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditBasicInfo();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fileChooser = new FileChooser(getActivity(), BasicInfoFragment.this);
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

    private void setDateTimeField() {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(mainActivity, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                et_BirthDay.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public void DrawData(ResumeView resumeView) {
        if (resumeView.stu_basic.profilePic != null)
            new LoadImgAsyncTask(iv_MImg, resumeView.stu_basic.profilePic).execute();

        et_ChName.setText(resumeView.stu_basic.chiName);
        et_EgName.setText(resumeView.stu_basic.engName);
        et_BirthPlace.setText(resumeView.stu_basic.bornedPlace);
        et_BirthDay.setText(resumeView.stu_basic.birthday);
        et_ResidenceAddress.setText(resumeView.stu_basic.address);
        et_Email.setText(resumeView.stu_basic.email);
        et_Cellphone.setText(resumeView.stu_basic.contact);
        switch (resumeView.stu_basic.gender) {
            case 0:
                rg_Gender.check(R.id.rb_Male);
                break;
            case 1:
                rg_Gender.check(R.id.rb_Female);
                break;
            case 2:
                rg_Gender.check(R.id.rb_Other);
                break;
        }
        switch (resumeView.stu_basic.ES) {
            case 0:
                rg_EnglishSpeak.check(R.id.rb_ES0);
                break;
            case 1:
                rg_EnglishSpeak.check(R.id.rb_ES1);
                break;
            case 2:
                rg_EnglishSpeak.check(R.id.rb_ES2);
                break;
            case 3:
                rg_EnglishSpeak.check(R.id.rb_ES3);
                break;
        }
        switch (resumeView.stu_basic.ER) {
            case 0:
                rg_EnglishRead.check(R.id.rb_ER0);
                break;
            case 1:
                rg_EnglishRead.check(R.id.rb_ER1);
                break;
            case 2:
                rg_EnglishRead.check(R.id.rb_ER2);
                break;
            case 3:
                rg_EnglishRead.check(R.id.rb_ER3);
                break;
        }
        switch (resumeView.stu_basic.EW) {
            case 0:
                rg_EnglishWrite.check(R.id.rb_EW0);
                break;
            case 1:
                rg_EnglishWrite.check(R.id.rb_EW1);
                break;
            case 2:
                rg_EnglishWrite.check(R.id.rb_EW2);
                break;
            case 3:
                rg_EnglishWrite.check(R.id.rb_EW3);
                break;
        }
        et_TOEICGrade.setText(resumeView.stu_basic.TOEIC + "");
        et_TOEFLGrade.setText(resumeView.stu_basic.TOEFL + "");
        et_SencondName.setText(resumeView.stu_basic.Oname);
        switch (resumeView.stu_basic.OS) {
            case 0:
                rg_SecondSpeak.check(R.id.rb_SS0);
                break;
            case 1:
                rg_SecondSpeak.check(R.id.rb_SS1);
                break;
            case 2:
                rg_SecondSpeak.check(R.id.rb_SS2);
                break;
            case 3:
                rg_SecondSpeak.check(R.id.rb_SS3);
                break;
        }
        switch (resumeView.stu_basic.OR) {
            case 0:
                rg_SecondRead.check(R.id.rb_SR0);
                break;
            case 1:
                rg_SecondRead.check(R.id.rb_SR1);
                break;
            case 2:
                rg_SecondRead.check(R.id.rb_SR2);
                break;
            case 3:
                rg_SecondRead.check(R.id.rb_SR3);
                break;
        }
        switch (resumeView.stu_basic.OW) {
            case 0:
                rg_SecondWrite.check(R.id.rb_SW0);
                break;
            case 1:
                rg_SecondWrite.check(R.id.rb_SW1);
                break;
            case 2:
                rg_SecondWrite.check(R.id.rb_SW2);
                break;
            case 3:
                rg_SecondWrite.check(R.id.rb_SW3);
                break;
        }
        switch (resumeView.stu_basic.eTypes) {
            case 0:
                rg_NowEducation.check(R.id.rb_NE0);
                break;
            case 1:
                rg_NowEducation.check(R.id.rb_NE1);
                break;
            case 2:
                rg_NowEducation.check(R.id.rb_NE2);
                break;
            case 3:
                rg_NowEducation.check(R.id.rb_NE3);
                break;
        }
        et_GraduateYear.setText(resumeView.stu_basic.graduateYear + "");
        et_GraduateSchool.setText(resumeView.stu_basic.graduatedSchool);
        et_GraduateSection.setText(resumeView.stu_basic.section);
        et_GraduateDepartment.setText(resumeView.stu_basic.department);
        et_Intro.setText(resumeView.stu_basic.autobiography);
    }

//    public class LoadImgAsyncTask extends AsyncTask<Void, Void, Bitmap> {
//        private String imgName;
//        private ImageView imageView;
//
//        public LoadImgAsyncTask(ImageView imageView, String ImgName) {
//            this.imgName = ImgName;
//            this.imageView = imageView;
//        }
//
//        @Override
//        protected Bitmap doInBackground(Void... params) {
//            Bitmap bitmap = null;
//            try {
//                URL url = new URL(getString(R.string.BackEndPath) + "storage/user-upload/" + imgName);
//                bitmap = BitmapFactory.decodeStream(url.openStream());
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return bitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            Drawable[] layers = new Drawable[2];
//            if (bitmap != null)
//                layers[0] = SharedService.CutBitmapToDrawable(bitmap, mainActivity);
//            else
//                layers[0] = ContextCompat.getDrawable(mainActivity, R.drawable.defaultmimg);
//            layers[1] = ContextCompat.getDrawable(mainActivity, R.drawable.editmimg);
//            LayerDrawable layerDrawable = new LayerDrawable(layers);
//            imageView.setImageDrawable(layerDrawable);
//        }
//
//    }

    public void EditBasicInfo() {
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

        builder.addFormDataPart("chiName", et_ChName.getText().toString())
                .addFormDataPart("engName", et_EgName.getText().toString())
                .addFormDataPart("bornedPlace", et_BirthPlace.getText().toString())
                .addFormDataPart("birthday", et_BirthDay.getText().toString())
                .addFormDataPart("gender", rg_Gender.indexOfChild(mainActivity.findViewById(rg_Gender.getCheckedRadioButtonId())) + "")
                .addFormDataPart("address", et_ResidenceAddress.getText().toString())
                .addFormDataPart("email", et_Email.getText().toString())
                .addFormDataPart("contact", et_Cellphone.getText().toString())
                .addFormDataPart("ES", rg_EnglishSpeak.indexOfChild(mainActivity.findViewById(rg_EnglishSpeak.getCheckedRadioButtonId())) + "")
                .addFormDataPart("ER", rg_EnglishRead.indexOfChild(mainActivity.findViewById(rg_EnglishRead.getCheckedRadioButtonId())) + "")
                .addFormDataPart("EW", rg_EnglishWrite.indexOfChild(mainActivity.findViewById(rg_EnglishWrite.getCheckedRadioButtonId())) + "")
                .addFormDataPart("TOEIC", et_TOEICGrade.getText().toString())
                .addFormDataPart("TOEFL", et_TOEFLGrade.getText().toString())
                .addFormDataPart("Oname", et_SencondName.getText().toString())
                .addFormDataPart("OS", rg_SecondSpeak.indexOfChild(mainActivity.findViewById(rg_SecondSpeak.getCheckedRadioButtonId())) + "")
                .addFormDataPart("OR", rg_SecondRead.indexOfChild(mainActivity.findViewById(rg_SecondRead.getCheckedRadioButtonId())) + "")
                .addFormDataPart("OW", rg_SecondWrite.indexOfChild(mainActivity.findViewById(rg_SecondWrite.getCheckedRadioButtonId())) + "")
                .addFormDataPart("eTypes", rg_NowEducation.indexOfChild(mainActivity.findViewById(rg_NowEducation.getCheckedRadioButtonId())) + "")
                .addFormDataPart("graduateYear", et_GraduateYear.getText().toString())
                .addFormDataPart("graduatedSchool", et_GraduateSchool.getText().toString())
                .addFormDataPart("department", et_GraduateDepartment.getText().toString())
                .addFormDataPart("section", et_GraduateSection.getText().toString())
                .addFormDataPart("autobiography", et_Intro.getText().toString());

        RequestBody body = builder.build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/editBasicDataById")
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
                            SharedService.ShowTextToast("修改履歷成功", mainActivity);
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
