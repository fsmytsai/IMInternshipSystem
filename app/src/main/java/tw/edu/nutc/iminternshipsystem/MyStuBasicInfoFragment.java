package tw.edu.nutc.iminternshipsystem;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import MyMethod.SharedService;
import ViewModel.ResumeView;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyStuBasicInfoFragment extends MySharedFragment {

    private MyStudentResumeActivity myStudentResumeActivity;
    private CircleImageView civ_StudentImage;

    private TextView tv_ChName;
    private TextView tv_EgName;
    private TextView tv_BirthPlace;
    private TextView tv_BirthDay;
    private TextView tv_ResidenceAddress;
    private TextView tv_Email;
    private TextView tv_Cellphone;
    private TextView tv_Gender;
    private TextView tv_EnglishSpeak;
    private TextView tv_EnglishRead;
    private TextView tv_EnglishWrite;
    private TextView tv_TOEICGrade;
    private TextView tv_TOEFLGrade;
    private TextView tv_SencondName;
    private TextView tv_SecondSpeak;
    private TextView tv_SecondRead;
    private TextView tv_SecondWrite;
    private TextView tv_NowEducation;
    private TextView tv_GraduateYear;
    private TextView tv_GraduateSchool;
    private TextView tv_GraduateSection;
    private TextView tv_GraduateDepartment;
    private TextView tv_Intro;

    public MyStuBasicInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_stu_basic_info, container, false);
        myStudentResumeActivity = (MyStudentResumeActivity) getActivity();
        super.client = myStudentResumeActivity.client;
        super.activity = myStudentResumeActivity;
        initViews(view);
        DrawData();
        return view;
    }

    private void initViews(final View view) {
        civ_StudentImage = (CircleImageView) view.findViewById(R.id.civ_StudentImage);

        tv_ChName = (TextView) view.findViewById(R.id.tv_ChName);
        tv_EgName = (TextView) view.findViewById(R.id.tv_EgName);
        tv_BirthPlace = (TextView) view.findViewById(R.id.tv_BirthPlace);
        tv_BirthDay = (TextView) view.findViewById(R.id.tv_BirthDay);
        tv_ResidenceAddress = (TextView) view.findViewById(R.id.tv_ResidenceAddress);
        tv_Email = (TextView) view.findViewById(R.id.tv_Email);
        tv_Cellphone = (TextView) view.findViewById(R.id.tv_Cellphone);
        tv_Gender = (TextView) view.findViewById(R.id.tv_Gender);
        tv_EnglishSpeak = (TextView) view.findViewById(R.id.tv_EnglishSpeak);
        tv_EnglishRead = (TextView) view.findViewById(R.id.tv_EnglishRead);
        tv_EnglishWrite = (TextView) view.findViewById(R.id.tv_EnglishWrite);
        tv_TOEICGrade = (TextView) view.findViewById(R.id.tv_TOEICGrade);
        tv_TOEFLGrade = (TextView) view.findViewById(R.id.tv_TOEFLGrade);
        tv_SencondName = (TextView) view.findViewById(R.id.tv_SencondName);
        tv_SecondSpeak = (TextView) view.findViewById(R.id.tv_SecondSpeak);
        tv_SecondRead = (TextView) view.findViewById(R.id.tv_SecondRead);
        tv_SecondWrite = (TextView) view.findViewById(R.id.tv_SecondWrite);
        tv_NowEducation = (TextView) view.findViewById(R.id.tv_NowEducation);
        tv_GraduateYear = (TextView) view.findViewById(R.id.tv_GraduateYear);
        tv_GraduateSchool = (TextView) view.findViewById(R.id.tv_GraduateSchool);
        tv_GraduateSection = (TextView) view.findViewById(R.id.tv_GraduateSection);
        tv_GraduateDepartment = (TextView) view.findViewById(R.id.tv_GraduateDepartment);
        tv_Intro = (TextView) view.findViewById(R.id.tv_Intro);

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
    }

    public void DrawData() {
        ResumeView resumeView = myStudentResumeActivity.resumeView;
        if (resumeView.stu_basic.profilePic != null)
            new LoadImgAsyncTask(civ_StudentImage, resumeView.stu_basic.profilePic).execute();

        tv_ChName.setText(resumeView.stu_basic.chiName);
        tv_EgName.setText(resumeView.stu_basic.engName);
        tv_BirthPlace.setText(resumeView.stu_basic.bornedPlace);
        tv_BirthDay.setText(resumeView.stu_basic.birthday);
        tv_ResidenceAddress.setText(resumeView.stu_basic.address);
        tv_Email.setText(resumeView.stu_basic.email);
        tv_Cellphone.setText(resumeView.stu_basic.contact);
        switch (resumeView.stu_basic.gender) {
            case 0:
                tv_Gender.setText("男");
                break;
            case 1:
                tv_Gender.setText("女");
                break;
            case 2:
                tv_Gender.setText("其他");
                break;
        }
        switch (resumeView.stu_basic.ES) {
            case 0:
                tv_EnglishSpeak.setText("不懂");
                break;
            case 1:
                tv_EnglishSpeak.setText("略懂");
                break;
            case 2:
                tv_EnglishSpeak.setText("流暢");
                break;
            case 3:
                tv_EnglishSpeak.setText("精通");
                break;
        }
        switch (resumeView.stu_basic.ER) {
            case 0:
                tv_EnglishRead.setText("不懂");
                break;
            case 1:
                tv_EnglishRead.setText("略懂");
                break;
            case 2:
                tv_EnglishRead.setText("流暢");
                break;
            case 3:
                tv_EnglishRead.setText("精通");
                break;
        }
        switch (resumeView.stu_basic.EW) {
            case 0:
                tv_EnglishWrite.setText("不懂");
                break;
            case 1:
                tv_EnglishWrite.setText("略懂");
                break;
            case 2:
                tv_EnglishWrite.setText("流暢");
                break;
            case 3:
                tv_EnglishWrite.setText("精通");
                break;
        }
        tv_TOEICGrade.setText(resumeView.stu_basic.TOEIC + "");
        tv_TOEFLGrade.setText(resumeView.stu_basic.TOEFL + "");
        tv_SencondName.setText(resumeView.stu_basic.Oname);
        switch (resumeView.stu_basic.OS) {
            case 0:
                tv_SecondSpeak.setText("不懂");
                break;
            case 1:
                tv_SecondSpeak.setText("略懂");
                break;
            case 2:
                tv_SecondSpeak.setText("流暢");
                break;
            case 3:
                tv_SecondSpeak.setText("精通");
                break;
        }
        switch (resumeView.stu_basic.OR) {
            case 0:
                tv_SecondRead.setText("不懂");
                break;
            case 1:
                tv_SecondRead.setText("略懂");
                break;
            case 2:
                tv_SecondRead.setText("流暢");
                break;
            case 3:
                tv_SecondRead.setText("精通");
                break;
        }
        switch (resumeView.stu_basic.OW) {
            case 0:
                tv_SecondWrite.setText("不懂");
                break;
            case 1:
                tv_SecondWrite.setText("略懂");
                break;
            case 2:
                tv_SecondWrite.setText("流暢");
                break;
            case 3:
                tv_SecondWrite.setText("精通");
                break;
        }
        switch (resumeView.stu_basic.eTypes) {
            case 0:
                tv_NowEducation.setText("五專");
                break;
            case 1:
                tv_NowEducation.setText("二技");
                break;
            case 2:
                tv_NowEducation.setText("四技");
                break;
            case 3:
                tv_NowEducation.setText("研究所");
                break;
        }
        tv_GraduateYear.setText(resumeView.stu_basic.graduateYear + "");
        tv_GraduateSchool.setText(resumeView.stu_basic.graduatedSchool);
        tv_GraduateSection.setText(resumeView.stu_basic.section);
        tv_GraduateDepartment.setText(resumeView.stu_basic.department);
        tv_Intro.setText(resumeView.stu_basic.autobiography);
    }

    public class LoadImgAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        private String imgName;
        private CircleImageView circleImageView;

        public LoadImgAsyncTask(CircleImageView circleImageView, String ImgName) {
            this.imgName = ImgName;
            this.circleImageView = circleImageView;
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
            if (bitmap != null) {
                Drawable drawable = SharedService.CutBitmapToDrawable(bitmap, myStudentResumeActivity);
                circleImageView.setImageDrawable(drawable);
            }
        }

    }
}
