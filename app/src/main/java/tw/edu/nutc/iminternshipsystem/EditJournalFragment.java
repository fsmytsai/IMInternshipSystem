package tw.edu.nutc.iminternshipsystem;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import MyMethod.SharedService;
import ViewModel.JournalView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditJournalFragment extends MySharedFragment {
    private JournalView.Journal journal;

    private JournalActivity journalActivity;
    private TextView tv_StudentName;
    private TextView tv_StudentId;
    private TextView tv_CompanyName;
    private TextView tv_TeacherName;
    private EditText et_InstructorName;
    private EditText et_JournalStartTime;
    private EditText et_JournalFinishTime;

    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;
    private EditText et_JournalDetail1;
    private EditText et_JournalDetail2;

    public EditJournalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_journal, container, false);
        journalActivity = (JournalActivity) getActivity();
        this.journal = journalActivity.journal;
        super.client = journalActivity.client;
        initViews(view);
        return view;
    }

    private void initViews(final View view) {
        setDateTimeField();
        tv_StudentName = (TextView) view.findViewById(R.id.tv_StudentName);
        tv_StudentId = (TextView) view.findViewById(R.id.tv_StudentId);
        tv_CompanyName = (TextView) view.findViewById(R.id.tv_CompanyName);
        tv_TeacherName = (TextView) view.findViewById(R.id.tv_TeacherName);
        et_InstructorName = (EditText) view.findViewById(R.id.et_InstructorName);
        et_JournalStartTime = (EditText) view.findViewById(R.id.et_JournalStartTime);
        et_JournalFinishTime = (EditText) view.findViewById(R.id.et_JournalFinishTime);
        et_JournalDetail1 = (EditText) view.findViewById(R.id.et_JournalDetail1);
        et_JournalDetail2 = (EditText) view.findViewById(R.id.et_JournalDetail2);

        tv_StudentName.setText(journal.stuName);
        tv_StudentId.setText(journal.stuNum);
        tv_CompanyName.setText(journal.comName);
        tv_TeacherName.setText(journal.teaName);
        et_InstructorName.setText(journal.journalInstructor);
        et_JournalStartTime.setText(journal.journalStart);
        et_JournalFinishTime.setText(journal.journalEnd);
        et_JournalDetail1.setText(journal.journalDetail_1);
        et_JournalDetail2.setText(journal.journalDetail_2);

        et_JournalStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePickerDialog.show();
            }
        });

        et_JournalFinishTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDatePickerDialog.show();
            }
        });

        view.findViewById(R.id.iv_Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditJournal();
            }
        });
    }

    private void setDateTimeField() {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
        Calendar newCalendar = Calendar.getInstance();
        startDatePickerDialog = new DatePickerDialog(journalActivity, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                et_JournalStartTime.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        endDatePickerDialog = new DatePickerDialog(journalActivity, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                et_JournalFinishTime.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void EditJournal() {
        SharedService.HideKeyboard(journalActivity);
        journalActivity.activity_Outer.requestFocus();

        RequestBody formBody = new FormBody.Builder()
                .add("journalID", journal.journalID + "")
                .add("journalInstructor", et_InstructorName.getText().toString())
                .add("journalDetail_1", et_JournalDetail1.getText().toString())
                .add("journalDetail_2", et_JournalDetail2.getText().toString())
                .add("journalStart", et_JournalStartTime.getText().toString())
                .add("journalEnd", et_JournalFinishTime.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/studentEditJournal")
                .put(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                journalActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", journalActivity);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatusCode = response.code();
                final String ResMsg = response.body().string();

                journalActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("修改成功", journalActivity);
                            journalActivity.setResult(Activity.RESULT_OK);
                            journalActivity.finish();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, journalActivity);
                        }
                    }
                });
            }
        });
    }
}
