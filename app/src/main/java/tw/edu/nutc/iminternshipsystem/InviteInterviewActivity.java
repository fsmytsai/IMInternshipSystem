package tw.edu.nutc.iminternshipsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.Calendar;

import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InviteInterviewActivity extends MySharedActivity {
    private int mid;

    private EditText et_InterviewPlace;
    private EditText et_ContactName;
    private EditText et_ContactPhone;
    private EditText et_ContactEmail;
    private EditText et_InterviewTime;
    private EditText et_InterviewPrecautions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_interview);
        mid = getIntent().getIntExtra("mid", -1);
        if (mid == -1)
            finish();

        initViews();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initViews() {
        SetToolBar("邀請面試", true);

        activity_Outer = findViewById(R.id.ll_ActivityOuter);

        et_InterviewPlace = (EditText) findViewById(R.id.et_InterviewPlace);
        et_ContactName = (EditText) findViewById(R.id.et_ContactName);
        et_ContactPhone = (EditText) findViewById(R.id.et_ContactPhone);
        et_ContactEmail = (EditText) findViewById(R.id.et_ContactEmail);
        et_InterviewTime = (EditText) findViewById(R.id.et_InterviewTime);
        et_InterviewPrecautions = (EditText) findViewById(R.id.et_InterviewPrecautions);

        et_InterviewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
    }

    String date_time = "";
    int mYear;
    int mMonth;
    int mDay;

    int mHour;
    int mMinute;

    private void datePicker() {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        date_time = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;
                        //*************Call Time Picker Here ********************
                        tiemPicker();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void tiemPicker() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        et_InterviewTime.setText(date_time + " " + hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void InviteInterview(View v) {
        SharedService.HideKeyboard(this);
        activity_Outer.requestFocus();

        RequestBody formBody = new FormBody.Builder()
                .add("mid", mid + "")
                .add("mstatus", "1")
                .add("inaddress", et_InterviewPlace.getText().toString())
                .add("jcontact_name", et_ContactName.getText().toString())
                .add("jcontact_phone", et_ContactPhone.getText().toString())
                .add("jcontact_email", et_ContactEmail.getText().toString())
                .add("intime", et_InterviewTime.getText().toString())
                .add("innotice", et_InterviewPrecautions.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/companyAcceptResume")
                .put(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", InviteInterviewActivity.this);
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
                            SharedService.ShowTextToast("邀請成功", InviteInterviewActivity.this);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, InviteInterviewActivity.this);
                        }
                    }
                });
            }
        });
    }
}
