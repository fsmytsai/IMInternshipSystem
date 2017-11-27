package tw.edu.nutc.iminternshipsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.JournalView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.RECORD_AUDIO;

public class StudentReviewActivity extends MySharedActivity implements ISpeechRecognitionServerEvents {

    private JournalView.Review review;
    private EditText et_ReviewContent;
    private ImageView iv_Record;
    private MicrophoneRecognitionClient micClient = null;
    private String oldText = "";
    private boolean canStartRecord = true;

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

        et_ReviewContent = (EditText) findViewById(R.id.et_ReviewContent);
        iv_Record = (ImageView) findViewById(R.id.iv_Record);
        et_ReviewContent.setText(review.reContent);

        if (review.reRead)
            et_ReviewContent.setFocusable(false);
    }

    public void openGoogleForm(View view) {
        Intent intent = new Intent(this, MyWebViewActivity.class);
        intent.putExtra("URL", review.googleForm);
        startActivity(intent);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Record();
            } else {
                //請求權限被拒絕
                SharedService.ShowTextToast("您拒絕錄音", this);
                canStartRecord = false;
            }
        }
    }

    public void StartRecord(View view) {
        int permission = ActivityCompat.checkSelfPermission(this, RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{RECORD_AUDIO},
                    1);
        } else {
            Record();
        }
    }

    private void Record() {
        if (!canStartRecord) {
            SharedService.ShowTextToast("您正在錄音囉！", this);
            return;
        }
        iv_Record.setImageResource(R.drawable.recording);
        //暫存上一次說完話的文字結果，用於串接
        oldText = et_ReviewContent.getText().toString();
        canStartRecord = false;
        //設置Context、辨識模式、語言、處理各個事件的類別及PrimaryKey
        this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                this,
                SpeechRecognitionMode.ShortPhrase,
                "zh-tw",
                this,
                getString(R.string.PrimaryKey));
        //設置取得Token的Uri
        this.micClient.setAuthenticationUri(getString(R.string.AuthenticationUri));
        //開始錄音及辨識
        this.micClient.startMicAndRecognition();
    }

    public void onPartialResponseReceived(final String response) {
        //即時的辨識回覆，將上次結果串接即時結果後顯示
        this.et_ReviewContent.setText(oldText + response);
    }

    public void onFinalResponseReceived(final RecognitionResult response) {
        if (null != this.micClient) {
            this.micClient.endMicAndRecognition();
        }
        //設置最終辨識出的最佳結果
        String FianlText = oldText + response.Results[0].DisplayText;
        this.et_ReviewContent.setText(FianlText);
        this.et_ReviewContent.setSelection(FianlText.length());
    }

    @Override
    public void onIntentReceived(String s) {
    }

    public void onError(final int errorCode, final String response) {
        canStartRecord = true;
        iv_Record.setImageResource(R.drawable.record);
        new AlertDialog.Builder(this)
                .setTitle("錯誤訊息")
                .setMessage("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode + "\nError text: " + response)
                .setPositiveButton("知道了", null)
                .show();
    }

    public void onAudioEvent(boolean recording) {
        if (!recording) {
            //結束錄音及辨識
            this.micClient.endMicAndRecognition();
            iv_Record.setImageResource(R.drawable.record);
            canStartRecord = true;
        }
    }
}