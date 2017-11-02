package tw.edu.nutc.iminternshipsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import MyMethod.SharedService;
import ViewModel.MyJobView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditJobActivity extends MySharedActivity {
    private MyJobView.MyJob myJob;

    private RadioGroup rg_JobType;
    private EditText et_JobExpireTime;
    private EditText et_JobStartTime;
    private EditText et_JobFinishTime;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog sTimePickerDialog;
    private TimePickerDialog fTimePickerDialog;
    private EditText et_JobDuty;
    private EditText et_JobSalaryLow;
    private EditText et_JobSalaryUp;
    private EditText et_JobPlace;
    private EditText et_ContactName;
    private EditText et_ContactPhone;
    private EditText et_ContactEmail;
    private EditText et_JobCount;
    private EditText et_JobDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);
        myJob = new Gson().fromJson(getIntent().getStringExtra("MyJobData"), MyJobView.MyJob.class);
        initView();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar("修改職缺", true);
        activity_Outer = findViewById(R.id.ll_ActivityOuter);

        et_JobExpireTime = (EditText) findViewById(R.id.et_JobExpireTime);
        et_JobStartTime = (EditText) findViewById(R.id.et_JobStartTime);
        et_JobFinishTime = (EditText) findViewById(R.id.et_JobFinishTime);

        setDateTimeField();
        et_JobExpireTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        et_JobExpireTime.setText(myJob.jdeadline);

        et_JobStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sTimePickerDialog.show();
            }
        });
        et_JobStartTime.setText(myJob.jStartDutyTime);

        et_JobFinishTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fTimePickerDialog.show();
            }
        });
        et_JobFinishTime.setText(myJob.jEndDutyTime);

        rg_JobType = (RadioGroup) findViewById(R.id.rg_JobType);

        et_JobDuty = (EditText) findViewById(R.id.et_JobDuty);
        et_JobSalaryLow = (EditText) findViewById(R.id.et_JobSalaryLow);
        et_JobSalaryUp = (EditText) findViewById(R.id.et_JobSalaryUp);
        et_JobPlace = (EditText) findViewById(R.id.et_JobPlace);
        et_ContactName = (EditText) findViewById(R.id.et_ContactName);
        et_ContactPhone = (EditText) findViewById(R.id.et_ContactPhone);
        et_ContactEmail = (EditText) findViewById(R.id.et_ContactEmail);
        et_JobCount = (EditText) findViewById(R.id.et_JobCount);
        et_JobDetail = (EditText) findViewById(R.id.et_JobDetail);

        switch (myJob.jtypes) {
            case 0:
                rg_JobType.check(R.id.rb_1);
                break;
            case 1:
                rg_JobType.check(R.id.rb_2);
                break;
            case 2:
                rg_JobType.check(R.id.rb_3);
                break;
            case 3:
                rg_JobType.check(R.id.rb_4);
                break;
        }

        et_JobDuty.setText(myJob.jduties);
        et_JobSalaryLow.setText(myJob.jsalary_low + "");
        et_JobSalaryUp.setText(myJob.jsalary_up + "");
        et_JobPlace.setText(myJob.jaddress);
        et_ContactName.setText(myJob.jcontact_name);
        et_ContactPhone.setText(myJob.jcontact_phone);
        et_ContactEmail.setText(myJob.jcontact_email);
        et_JobCount.setText(myJob.jNOP + "");
        et_JobDetail.setText(myJob.jdetails);
    }

    private void setDateTimeField() {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                et_JobExpireTime.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        sTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                et_JobStartTime.setText(hourOfDay + ":" + minute);
            }
        }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), false);

        fTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                et_JobFinishTime.setText(hourOfDay + ":" + minute);
            }
        }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), false);
    }

    public void EditJob(View view) {
        SharedService.HideKeyboard(this);
        activity_Outer.requestFocus();

        RequestBody formBody = new FormBody.Builder()
                .add("joid", myJob.joid + "")
                .add("jtypes", rg_JobType.indexOfChild(findViewById(rg_JobType.getCheckedRadioButtonId())) + "")
                .add("jduties", et_JobDuty.getText().toString())
                .add("jsalary_low", et_JobSalaryLow.getText().toString())
                .add("jsalary_up", et_JobSalaryUp.getText().toString())
                .add("jaddress", et_JobPlace.getText().toString())
                .add("jStartDutyTime", et_JobStartTime.getText().toString())
                .add("jEndDutyTime", et_JobFinishTime.getText().toString())
                .add("jcontact_name", et_ContactName.getText().toString())
                .add("jcontact_phone", et_ContactPhone.getText().toString())
                .add("jcontact_email", et_ContactEmail.getText().toString())
                .add("jNOP", et_JobCount.getText().toString())
                .add("jdeadline", et_JobExpireTime.getText().toString())
                .add("jdetails", et_JobDetail.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/editJobOpening")
                .put(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", EditJobActivity.this);
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
                            SharedService.ShowTextToast("修改成功", EditJobActivity.this);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, EditJobActivity.this);
                        }
                    }
                });
            }
        });
    }
}
