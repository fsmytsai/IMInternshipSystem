package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.ExistVisitView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class VisitRecordActivity extends MySharedActivity {
    private static final int EDIT_VISITSTU_CODE = 55;
    private static final int EDIT_VISITCOM_CODE = 66;
    private int SCid;
    private ExistVisitView existVisitView;
    private LinearLayout ll_ExistCompanyVisitTitle;
    private LinearLayout ll_ExistCompanyVisit;
    private LinearLayout ll_ExistStudentVisitTitle;
    private LinearLayout ll_ExistStudentVisit;

    private int nowPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_record);
        SCid = getIntent().getIntExtra("SCid", -1);
        SetCache((int) Runtime.getRuntime().maxMemory() / 20);
        initView();
        GetExistVisit();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar("訪視紀錄", true);
        ll_ExistCompanyVisitTitle = (LinearLayout) findViewById(R.id.ll_ExistCompanyVisitTitle);
        ll_ExistCompanyVisit = (LinearLayout) findViewById(R.id.ll_ExistCompanyVisit);
        ll_ExistStudentVisitTitle = (LinearLayout) findViewById(R.id.ll_ExistStudentVisitTitle);
        ll_ExistStudentVisit = (LinearLayout) findViewById(R.id.ll_ExistStudentVisit);

    }

    public void AddVisitRecord(View view) {
        Intent intent = new Intent(this, AddVisitRecordActivity.class);
        intent.putExtra("SCid", SCid);
        startActivity(intent);
    }

    private void GetExistVisit() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getInterviewBySCid?SCid=" + SCid)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", VisitRecordActivity.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatusCode = response.code();
                final String ResMsg = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            existVisitView = new Gson().fromJson(ResMsg, ExistVisitView.class);
                            for (int i = 0; i < existVisitView.InterviewComList.size(); i++) {
                                final int Position = i;
                                LinearLayout ll_ExistVisitBlock = GetExistVisitBlock(
                                        existVisitView.InterviewComList.get(i).profilePic,
                                        existVisitView.InterviewComList.get(i).comName + " 第" + (i + 1) + "次訪視");
                                ll_ExistVisitBlock.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        nowPosition = Position;
                                        Intent intent = new Intent(VisitRecordActivity.this, EditVisitCompanyActivity.class);
                                        intent.putExtra("ExistVisitCompany", new Gson().toJson(existVisitView.InterviewComList.get(Position)));
                                        startActivityForResult(intent, EDIT_VISITCOM_CODE);
                                    }
                                });
                                ll_ExistCompanyVisit.addView(ll_ExistVisitBlock);
                            }
                            for (int i = 0; i < existVisitView.InterviewStuList.size(); i++) {
                                final int Position = i;
                                LinearLayout ll_ExistVisitBlock = GetExistVisitBlock(
                                        existVisitView.InterviewStuList.get(i).profilePic,
                                        existVisitView.InterviewStuList.get(i).stuName + " 第" + (i + 1) + "次訪視");
                                ll_ExistVisitBlock.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        nowPosition = Position;
                                        Intent intent = new Intent(VisitRecordActivity.this, EditVisitStudentActivity.class);
                                        intent.putExtra("ExistVisitStudent", new Gson().toJson(existVisitView.InterviewStuList.get(Position)));
                                        startActivityForResult(intent, EDIT_VISITSTU_CODE);
                                    }
                                });
                                ll_ExistStudentVisit.addView(ll_ExistVisitBlock);
                            }

                            if (existVisitView.InterviewComList.size() != 0) {
                                SharedService.ShowAndHideBlock(ll_ExistCompanyVisit, (ImageView) findViewById(R.id.iv_CompanyUp));
                                ll_ExistCompanyVisitTitle.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        SharedService.ShowAndHideBlock(ll_ExistCompanyVisit, (ImageView) findViewById(R.id.iv_CompanyUp));
                                    }
                                });
                            }

                            if (existVisitView.InterviewStuList.size() != 0) {
                                SharedService.ShowAndHideBlock(ll_ExistStudentVisit, (ImageView) findViewById(R.id.iv_StudentUp));
                                ll_ExistStudentVisitTitle.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        SharedService.ShowAndHideBlock(ll_ExistStudentVisit, (ImageView) findViewById(R.id.iv_StudentUp));
                                    }
                                });
                            }
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, VisitRecordActivity.this);
                        }
                    }
                });

            }

        });
    }

    private LinearLayout GetExistVisitBlock(String profilePic, String Text) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, (int) SharedService.DipToPixels(this, 10), 0, 0);
        LinearLayout ll_ExistVisitBlock = new LinearLayout(this);
        ll_ExistVisitBlock.setOrientation(LinearLayout.HORIZONTAL);
        ll_ExistVisitBlock.setPadding((int) SharedService.DipToPixels(this, 20), 0, 0, 0);
        ll_ExistVisitBlock.setLayoutParams(layoutParams);
        ll_ExistVisitBlock.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams((int) SharedService.DipToPixels(this, 40), (int) SharedService.DipToPixels(this, 40));
        ImageView iv_MImg = new ImageView(this);
        if (profilePic != null) {
            iv_MImg.setTag(profilePic);
            showImage(iv_MImg, profilePic, true, null);
        } else
            iv_MImg.setImageResource(R.drawable.defaultmimg);
        iv_MImg.setLayoutParams(layoutParams1);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins((int) SharedService.DipToPixels(this, 20), 0, 0, 0);
        TextView tv_StudentName = new TextView(this);
        tv_StudentName.setText(Text);
        tv_StudentName.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tv_StudentName.setTextSize(22f);
        tv_StudentName.setLayoutParams(layoutParams2);

        ll_ExistVisitBlock.addView(iv_MImg);
        ll_ExistVisitBlock.addView(tv_StudentName);

        return ll_ExistVisitBlock;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_VISITCOM_CODE && resultCode == RESULT_OK) {
            ExistVisitView.ExistVisitCompany existVisitCompany = new Gson().fromJson(data.getStringExtra("ExistVisitCompany"), ExistVisitView.ExistVisitCompany.class);
            existVisitView.InterviewComList.set(nowPosition, existVisitCompany);
            nowPosition = -1;
        } else if (requestCode == EDIT_VISITSTU_CODE && resultCode == RESULT_OK) {
            ExistVisitView.ExistVisitStudent existVisitStudent = new Gson().fromJson(data.getStringExtra("ExistVisitStudent"), ExistVisitView.ExistVisitStudent.class);
            existVisitView.InterviewStuList.set(nowPosition, existVisitStudent);
            nowPosition = -1;
        }
    }
}
