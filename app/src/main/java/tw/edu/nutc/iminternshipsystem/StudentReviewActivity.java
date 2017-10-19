package tw.edu.nutc.iminternshipsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.JournalView;
import ViewModel.ResumeView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StudentReviewActivity extends MySharedActivity {

    private JournalView.Review review;
    private TextView tv_GoogleForm;
    private EditText et_ReviewContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_review);
        review = new Gson().fromJson(getIntent().getStringExtra("Review"), JournalView.Review.class);
        initViews();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initViews() {
        SetToolBar("實習總心得", true);

        activity_Outer = findViewById(R.id.ll_ActivityOuter);

        tv_GoogleForm = (TextView) findViewById(R.id.tv_GoogleForm);
        et_ReviewContent = (EditText) findViewById(R.id.et_ReviewContent);

        tv_GoogleForm.setText(review.googleForm);
        tv_GoogleForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setAction("android.intent.action.VIEW");

                Uri content_url = Uri.parse(review.googleForm);

                intent.setData(content_url);

                startActivity(intent);
            }
        });

        et_ReviewContent.setText(review.reContent);

        if (review.reRead)
            et_ReviewContent.setFocusable(false);
    }

    public void EditReview(View v) {
        if (review.reRead) {
            SharedService.ShowTextToast("老師已閱，無法修改", this);
            return;
        }


        SharedService.HideKeyboard(this);
        activity_Outer.requestFocus();

        new AlertDialog.Builder(StudentReviewActivity.this)
                .setMessage("是否已確實填寫必填問卷?")
                .setNeutralButton("取消", null)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RequestBody formBody = new FormBody.Builder()
                                .add("reId", review.reId + "")
                                .add("reContent", et_ReviewContent.getText().toString())
                                .build();

                        Request request = new Request.Builder()
                                .url(getString(R.string.BackEndPath) + "api/editReview")
                                .put(formBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SharedService.ShowTextToast("請檢察網路連線", StudentReviewActivity.this);
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
                                            SharedService.ShowTextToast("修改成功", StudentReviewActivity.this);
                                            setResult(RESULT_OK);
                                            finish();
                                        } else {
                                            SharedService.HandleError(StatusCode, ResMsg, StudentReviewActivity.this);
                                        }
                                    }
                                });
                            }
                        });
                    }
                })
                .show();
    }
}
