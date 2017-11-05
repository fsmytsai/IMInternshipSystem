package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendMailActivity extends MySharedActivity {
    private EditText et_MailTitle;
    private EditText et_MailContent;
    private String c_account;
    private int slId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);
        c_account = getIntent().getStringExtra("c_account");
        slId = getIntent().getIntExtra("slId", -1);
        initView();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        activity_Outer = findViewById(R.id.ll_ActivityOuter);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.ib_SendMail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slId == -1)
                    SendMail();
                else
                    ReplyMail();
            }
        });

        et_MailTitle = (EditText) findViewById(R.id.et_MailTitle);
        et_MailContent = (EditText) findViewById(R.id.et_MailContent);

        if (slId != -1) {
            et_MailTitle.setVisibility(View.GONE);
            TextView tv_ToolBar = (TextView) findViewById(R.id.tv_ToolBar);
            tv_ToolBar.setText("回信");
        }
    }

    private void SendMail() {
        RequestBody formBody = new FormBody.Builder()
                .add("lRecipient", c_account)
                .add("lTitle", et_MailTitle.getText().toString())
                .add("lContent", et_MailContent.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/sendMail")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", SendMailActivity.this);
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
                            SharedService.ShowTextToast("寄信成功", SendMailActivity.this);
                            finish();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, SendMailActivity.this);
                        }
                    }
                });
            }
        });
    }

    private void ReplyMail() {
        RequestBody formBody = new FormBody.Builder()
                .add("slId", slId + "")
                .add("lContent", et_MailContent.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/replyMailById")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", SendMailActivity.this);
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
                            SharedService.ShowTextToast("回信成功", SendMailActivity.this);
                            finish();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, SendMailActivity.this);
                        }
                    }
                });
            }
        });
    }
}
