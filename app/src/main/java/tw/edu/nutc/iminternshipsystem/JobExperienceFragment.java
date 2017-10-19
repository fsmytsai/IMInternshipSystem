package tw.edu.nutc.iminternshipsystem;


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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

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
public class JobExperienceFragment extends MySharedFragment {
    private MainActivity mainActivity;

    public RecyclerView rv_JobExList;
    private JobExListAdapter jobExListAdapter;
    private ResumeView resumeView;

    public JobExperienceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_job_experience, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;

        initView(view);
        return view;
    }


    private void initView(View view) {
        rv_JobExList = (RecyclerView) view.findViewById(R.id.rv_JobExList);
    }

    public void DrawData(ResumeView resumeView) {
        this.resumeView = resumeView;
        rv_JobExList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        jobExListAdapter = new JobExListAdapter();
        rv_JobExList.setAdapter(jobExListAdapter);
    }


    public class JobExListAdapter extends RecyclerView.Adapter<JobExListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            View view = LayoutInflater.from(context).inflate(R.layout.jobexperience_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (position == resumeView.stu_jobExperience.size()) {
                holder.ll_JobExSave.setVisibility(View.GONE);
                holder.ll_JobExUnSave.setVisibility(View.VISIBLE);
                holder.iv_Save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SaveJobEx(holder.et_JobName, holder.et_CompanyName);
                    }
                });
            } else {
                holder.ll_JobExSave.setVisibility(View.VISIBLE);
                holder.ll_JobExUnSave.setVisibility(View.GONE);
                holder.tv_JobName.setText(resumeView.stu_jobExperience.get(position).jobTitle);
                holder.tv_CompanyName.setText(resumeView.stu_jobExperience.get(position).comName);
                holder.iv_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteJobEx(position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return resumeView.stu_jobExperience.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_JobExSave;
            private TextView tv_JobName;
            private TextView tv_CompanyName;
            private ImageView iv_Delete;

            private LinearLayout ll_JobExUnSave;
            private EditText et_JobName;
            private EditText et_CompanyName;
            private ImageView iv_Save;

            public ViewHolder(View itemView) {
                super(itemView);

                ll_JobExSave = (LinearLayout) itemView.findViewById(R.id.ll_JobExSave);
                tv_JobName = (TextView) itemView.findViewById(R.id.tv_JobName);
                tv_CompanyName = (TextView) itemView.findViewById(R.id.tv_CompanyName);
                iv_Delete = (ImageView) itemView.findViewById(R.id.iv_Delete);

                ll_JobExUnSave = (LinearLayout) itemView.findViewById(R.id.ll_JobExUnSave);
                et_JobName = (EditText) itemView.findViewById(R.id.et_JobName);
                et_CompanyName = (EditText) itemView.findViewById(R.id.et_CompanyName);
                iv_Save = (ImageView) itemView.findViewById(R.id.iv_Save);
            }
        }
    }

    private void SaveJobEx(final EditText et_JobName, final EditText et_CompanyName) {
        final String JobName = et_JobName.getText().toString();
        final String CompanyName = et_CompanyName.getText().toString();
        RequestBody formBody = new FormBody.Builder()
                .add("jobTitle", JobName)
                .add("comName", CompanyName)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/createJobExperienceById")
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
                            int jid = new Gson().fromJson(ResMsg, Integer.class);
                            ResumeView.JobExperience jobExperience = new ResumeView.JobExperience();
                            jobExperience.jid = jid;
                            jobExperience.jobTitle = JobName;
                            jobExperience.comName = CompanyName;
                            resumeView.stu_jobExperience.add(jobExperience);
                            jobExListAdapter.notifyItemInserted(resumeView.stu_jobExperience.size() - 1);
                            et_JobName.setText("");
                            et_CompanyName.setText("");
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }

    private void DeleteJobEx(final int Position) {
        new AlertDialog.Builder(mainActivity)
                .setMessage("確定要刪除此工作經歷嗎?")
                .setNeutralButton("取消", null)
                .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Request request = new Request.Builder()
                                .url(getString(R.string.BackEndPath) + "api/deleteJobExperienceById?jid=" + resumeView.stu_jobExperience.get(Position).jid)
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
                                            resumeView.stu_jobExperience.remove(Position);
                                            jobExListAdapter.notifyItemRemoved(Position);
                                            jobExListAdapter.notifyItemRangeChanged(0, resumeView.stu_jobExperience.size());
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
