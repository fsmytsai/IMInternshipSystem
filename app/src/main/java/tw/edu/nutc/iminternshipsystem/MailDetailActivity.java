package tw.edu.nutc.iminternshipsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.MailView;
import me.grantland.widget.AutofitTextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MailDetailActivity extends MySharedActivity {
    private MailView.Mail mail;

    private AutofitTextView atv_MailTitle;
    private TextView tv_MailContent;
    private TextView tv_MailCreateTime;
    private ImageView iv_SenderImg;
    private TextView tv_MailSender;
    private ImageView iv_recipientImg;
    private TextView tv_MailRecipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);
        mail = new Gson().fromJson(getIntent().getStringExtra("Mail"), MailView.Mail.class);
        initView();
        if (!mail.read)
            GetDetailMail();
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
        atv_MailTitle = (AutofitTextView) findViewById(R.id.atv_MailTitle);
        tv_MailContent = (TextView) findViewById(R.id.tv_MailContent);
        tv_MailCreateTime = (TextView) findViewById(R.id.tv_MailCreateTime);
        iv_SenderImg = (ImageView) findViewById(R.id.iv_SenderImg);
        tv_MailSender = (TextView) findViewById(R.id.tv_MailSender);
        iv_recipientImg = (ImageView) findViewById(R.id.iv_recipientImg);
        tv_MailRecipient = (TextView) findViewById(R.id.tv_MailRecipient);

        atv_MailTitle.setText(mail.lTitle);
        mail.lContent = mail.lContent.replace("\\n","\n");
        tv_MailContent.setText(mail.lContent);
        tv_MailCreateTime.setText(mail.created_at);
        if (mail.senderPic != null) {
            iv_SenderImg.setTag(mail.senderPic);
            showImage(iv_SenderImg, mail.senderPic, true, null);
        }
        tv_MailSender.setText(mail.lSenderName);
        if (mail.recipientPic != null) {
            iv_recipientImg.setTag(mail.recipientPic);
            showImage(iv_recipientImg, mail.recipientPic, true, null);
        }
        tv_MailRecipient.setText(mail.lRecipientName);

        if (SharedService.identityView.account.equals(mail.lSender) || mail.lSender.equals("admin123"))
            findViewById(R.id.ib_ReplyMail).setVisibility(View.GONE);
        else
            findViewById(R.id.ib_ReplyMail).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MailDetailActivity.this, SendMailActivity.class);
                    intent.putExtra("slId", mail.slId);
                    startActivity(intent);
                }
            });

        LinearLayout ll_MailContent = (LinearLayout) findViewById(R.id.ll_MailContent);

        if (mail.expired) {
            atv_MailTitle.append(" (已處理)");
        } else {
            if (mail.lStatus == 1) {
                View view = LayoutInflater.from(this).inflate(R.layout.mailstatus1_block, ll_MailContent, false);
                ll_MailContent.addView(view);
            } else if (mail.lStatus == 3) {
                View view = LayoutInflater.from(this).inflate(R.layout.mailstatus3_block, ll_MailContent, false);
                ll_MailContent.addView(view);
            } else if (mail.lStatus == 4) {
                View view = LayoutInflater.from(this).inflate(R.layout.mailstatus4_block, ll_MailContent, false);
                ll_MailContent.addView(view);
            } else if (mail.lStatus == 6 || mail.lStatus == 7) {
                View view = LayoutInflater.from(this).inflate(R.layout.mailstatus6_block, ll_MailContent, false);
                ll_MailContent.addView(view);
            } else if (mail.lStatus == 11 && SharedService.identityView.u_status == 1) {
                //老師 學生分配到名下
                View view = LayoutInflater.from(this).inflate(R.layout.mailstatus11_block, ll_MailContent, false);
                ll_MailContent.addView(view);
            } else if (mail.lStatus == 13 || mail.lStatus == 14 || mail.lStatus == 15) {
                //老師 學生填寫完週誌 廠商已批改週誌
                View view = LayoutInflater.from(this).inflate(R.layout.mailstatus13_block, ll_MailContent, false);
                ll_MailContent.addView(view);
            }
        }
    }

    private void GetDetailMail() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getMailDetails?slId=" + mail.slId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MailDetailActivity.this);
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

                        if (StatusCode != 200) {
                            SharedService.HandleError(StatusCode, ResMsg, MailDetailActivity.this);
                        } else {
                            setResult(RESULT_OK);
                        }
                    }
                });

            }

        });
    }

    public void GoProcessResume(View v) {
        getIntent().putExtra("MailStatus", 1);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    public void AcceptInterview(View v) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.internlogo)
                .setTitle("面試確認")
                .setMessage("確定要接受面試嗎?")
                .setPositiveButton("確定接受",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                StudentProcessInterview(1);
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    public void RejectInterview(View v) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.internlogo)
                .setTitle("面試確認")
                .setMessage("確定要拒絕面試嗎?")
                .setPositiveButton("確定拒絕",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                StudentProcessInterview(2);
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    private void StudentProcessInterview(int mstatus) {
        RequestBody formBody = new FormBody.Builder()
                .add("mid", mail.lNotes)
                .add("mstatus", mstatus + "")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/studentAcceptInterview")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MailDetailActivity.this);
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
                            SharedService.ShowTextToast("處理成功", MailDetailActivity.this);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MailDetailActivity.this);
                        }
                    }
                });
            }
        });
    }

    public void PassInterview(View v) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.internlogo)
                .setTitle("面試結果確認")
                .setMessage("確定要通過該學生的面試嗎?")
                .setPositiveButton("確定通過",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CompanyProcessInterview(1, "");
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    public void FailedInterview(View v) {
        View view = LayoutInflater.from(this).inflate(R.layout.failed_interview_reason_block, null, false);
        final EditText et_Reason = (EditText) view.findViewById(R.id.et_Reason);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("確定不通過",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CompanyProcessInterview(2, et_Reason.getText().toString());
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    private void CompanyProcessInterview(int mstatus, String Reason) {
        RequestBody formBody = new FormBody.Builder()
                .add("mid", mail.lNotes)
                .add("mstatus", mstatus + "")
                .add("mfailedreason", Reason)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/companyResponseInterview")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MailDetailActivity.this);
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
                            SharedService.ShowTextToast("處理成功", MailDetailActivity.this);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MailDetailActivity.this);
                        }
                    }
                });
            }
        });
    }

    public void AcceptJob(View v) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.internlogo)
                .setTitle("到職確認")
                .setMessage("確定要接受到職嗎?")
                .setPositiveButton("確定接受",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ProcessJob(1);
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    public void RejectJob(View v) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.internlogo)
                .setTitle("到職確認")
                .setMessage("確定要拒絕到職嗎?")
                .setPositiveButton("確定拒絕",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ProcessJob(2);
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    private void ProcessJob(int mstatus) {
        RequestBody formBody = new FormBody.Builder()
                .add("mid", mail.lNotes)
                .add("mstatus", mstatus + "")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/studentAcceptJob")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MailDetailActivity.this);
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
                            SharedService.ShowTextToast("處理成功", MailDetailActivity.this);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MailDetailActivity.this);
                        }
                    }
                });
            }
        });
    }

    public void GoStudentResume(View view) {
        getIntent().putExtra("MailStatus", 11);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    public void GoManageStudent(View view) {
        Intent intent = new Intent(this, MyWebViewActivity.class);
        intent.putExtra("URL", getString(R.string.FrontEndPath) + "studentManagement&courseid=" + mail.lNotes + "&Token=" + SharedService.token);
        startActivity(intent);
    }
}
