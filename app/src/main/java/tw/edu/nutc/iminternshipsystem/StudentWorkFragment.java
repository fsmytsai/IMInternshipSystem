package tw.edu.nutc.iminternshipsystem;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import MyMethod.SharedService;
import ViewModel.ResumeView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class StudentWorkFragment extends MySharedFragment {
    private MainActivity mainActivity;

    public RecyclerView rv_StudentWorkList;
    private StudentWorkListAdapter studentWorkListAdapter;
    private ResumeView resumeView;

    public StudentWorkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_work, container, false);
        ResumeFragment resumeFragment = (ResumeFragment) getParentFragment();
        resumeFragment.GetResumeData();
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        initView(view);
        return view;
    }


    private void initView(View view) {
        rv_StudentWorkList = (RecyclerView) view.findViewById(R.id.rv_StudentWorkList);
    }

    public void DrawData(ResumeView resumeView) {
        this.resumeView = resumeView;
        rv_StudentWorkList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        studentWorkListAdapter = new StudentWorkListAdapter();
        rv_StudentWorkList.setAdapter(studentWorkListAdapter);
    }

    public class StudentWorkListAdapter extends RecyclerView.Adapter<StudentWorkListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            View view = LayoutInflater.from(context).inflate(R.layout.studentwork_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (position == resumeView.stu_works.size()) {
                holder.ll_StudentWorkSave.setVisibility(View.GONE);
                holder.ll_StudentWorkUnSave.setVisibility(View.VISIBLE);

                final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
                Calendar newCalendar = Calendar.getInstance();
                final DatePickerDialog datePickerDialog = new DatePickerDialog(mainActivity, new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        holder.et_WorkDate.setText(dateFormatter.format(newDate.getTime()));
                    }

                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

                holder.et_WorkDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePickerDialog.show();
                    }
                });
                holder.iv_Save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SaveStudentWork(holder.et_WorkName, holder.et_WorkDate, holder.et_WorkLink);
                    }
                });
            } else {
                holder.ll_StudentWorkSave.setVisibility(View.VISIBLE);
                holder.ll_StudentWorkUnSave.setVisibility(View.GONE);
                holder.tv_WorkName.setText(resumeView.stu_works.get(position).wName);
                holder.tv_WorkYear.setText(resumeView.stu_works.get(position).wCreatedDate);
                holder.tv_WorkLink.setText(resumeView.stu_works.get(position).wLink);
                holder.iv_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteStudentWork(position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return resumeView.stu_works.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_StudentWorkSave;
            private TextView tv_WorkName;
            private TextView tv_WorkYear;
            private TextView tv_WorkLink;
            private ImageView iv_Delete;

            private LinearLayout ll_StudentWorkUnSave;
            private EditText et_WorkName;
            private EditText et_WorkDate;
            private EditText et_WorkLink;
            private ImageView iv_Save;

            public ViewHolder(View itemView) {
                super(itemView);

                ll_StudentWorkSave = (LinearLayout) itemView.findViewById(R.id.ll_StudentWorkSave);
                tv_WorkName = (TextView) itemView.findViewById(R.id.tv_WorkName);
                tv_WorkYear = (TextView) itemView.findViewById(R.id.tv_WorkYear);
                tv_WorkLink = (TextView) itemView.findViewById(R.id.tv_WorkLink);
                iv_Delete = (ImageView) itemView.findViewById(R.id.iv_Delete);

                ll_StudentWorkUnSave = (LinearLayout) itemView.findViewById(R.id.ll_StudentWorkUnSave);
                et_WorkName = (EditText) itemView.findViewById(R.id.et_WorkName);
                et_WorkDate = (EditText) itemView.findViewById(R.id.et_WorkDate);
                et_WorkLink = (EditText) itemView.findViewById(R.id.et_WorkLink);
                iv_Save = (ImageView) itemView.findViewById(R.id.iv_Save);
            }
        }
    }

    private void SaveStudentWork(final EditText et_WorkName, final EditText et_WorkYear, final EditText et_WorkLink) {
        final String WorkName = et_WorkName.getText().toString();
        final String WorkYear = et_WorkYear.getText().toString();
        final String WorkLink = et_WorkLink.getText().toString();
        RequestBody formBody = new FormBody.Builder()
                .add("wName", WorkName)
                .add("wCreatedDate", WorkYear)
                .add("wLink", WorkLink)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/createWorksDataById")
                .post(formBody)
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            int wid = new Gson().fromJson(ResMsg, Integer.class);
                            ResumeView.StudentWork studentWork = new ResumeView.StudentWork();
                            studentWork.wid = wid;
                            studentWork.wName = WorkName;
                            studentWork.wCreatedDate = WorkYear;
                            studentWork.wLink = WorkLink;
                            resumeView.stu_works.add(studentWork);
                            studentWorkListAdapter.notifyItemInserted(resumeView.stu_works.size() - 1);
                            et_WorkName.setText("");
                            et_WorkYear.setText("");
                            et_WorkLink.setText("");
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }

    private void DeleteStudentWork(final int Position) {
        new AlertDialog.Builder(mainActivity)
                .setMessage("確定要刪除此作品嗎?")
                .setNeutralButton("取消", null)
                .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Request request = new Request.Builder()
                                .url(getString(R.string.BackEndPath) + "api/deleteWorksDataById?wid=" + resumeView.stu_works.get(Position).wid)
                                .delete()
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

                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (StatusCode == 200) {
                                            resumeView.stu_works.remove(Position);
                                            studentWorkListAdapter.notifyItemRemoved(Position);
                                            studentWorkListAdapter.notifyItemRangeChanged(0, resumeView.stu_works.size());
                                        } else {
                                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
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
