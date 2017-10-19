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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
public class StudentAbilityFragment extends MySharedFragment {
    private MainActivity mainActivity;

    public RecyclerView rv_StudentAbilityList;
    private StudentAbilityListAdapter studentAbilityListAdapter;
    private String[] AbilityTypes = {"大數據", "伺服器架設", "資料庫", "後端程式設計", "前端程式設計", "多媒體", "文書處理"};
    private ResumeView resumeView;

    public StudentAbilityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_ability, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;

        initView(view);
        return view;
    }

    private void initView(View view) {
        rv_StudentAbilityList = (RecyclerView) view.findViewById(R.id.rv_StudentAbilityList);
    }

    public void DrawData(ResumeView resumeView) {
        this.resumeView = resumeView;
        rv_StudentAbilityList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        studentAbilityListAdapter = new StudentAbilityListAdapter();
        rv_StudentAbilityList.setAdapter(studentAbilityListAdapter);
    }

    public class StudentAbilityListAdapter extends RecyclerView.Adapter<StudentAbilityListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            View view = LayoutInflater.from(context).inflate(R.layout.studentability_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (position == resumeView.stu_ability.size()) {
                holder.ll_AbilitySave.setVisibility(View.GONE);
                holder.ll_AbilityUnSave.setVisibility(View.VISIBLE);
                ArrayAdapter<CharSequence> AbilityTypesArrayAdapter = new ArrayAdapter<CharSequence>(mainActivity, android.R.layout.simple_spinner_item, AbilityTypes);
                AbilityTypesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.sp_AbilityType.setAdapter(AbilityTypesArrayAdapter);

                holder.iv_Save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SaveAbility(holder.sp_AbilityType, holder.et_Intro);
                    }
                });
            } else {
                holder.ll_AbilitySave.setVisibility(View.VISIBLE);
                holder.ll_AbilityUnSave.setVisibility(View.GONE);
                holder.tv_AbilityType.setText(AbilityTypes[resumeView.stu_ability.get(position).abiType]);
                holder.tv_AbilityIntro.setText(resumeView.stu_ability.get(position).abiName);
                holder.iv_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteAbility(position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return resumeView.stu_ability.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_AbilitySave;
            private TextView tv_AbilityType;
            private TextView tv_AbilityIntro;
            private ImageView iv_Delete;

            private LinearLayout ll_AbilityUnSave;
            private Spinner sp_AbilityType;
            private EditText et_Intro;
            private ImageView iv_Save;

            public ViewHolder(View itemView) {
                super(itemView);

                ll_AbilitySave = (LinearLayout) itemView.findViewById(R.id.ll_AbilitySave);
                tv_AbilityType = (TextView) itemView.findViewById(R.id.tv_AbilityType);
                tv_AbilityIntro = (TextView) itemView.findViewById(R.id.tv_AbilityIntro);
                iv_Delete = (ImageView) itemView.findViewById(R.id.iv_Delete);

                ll_AbilityUnSave = (LinearLayout) itemView.findViewById(R.id.ll_AbilityUnSave);
                sp_AbilityType = (Spinner) itemView.findViewById(R.id.sp_AbilityType);
                et_Intro = (EditText) itemView.findViewById(R.id.et_Intro);
                iv_Save = (ImageView) itemView.findViewById(R.id.iv_Save);
            }
        }
    }

    private void SaveAbility(final Spinner sp_AbilityType, final EditText et_AbilityIntro) {
        final int AbilityType = sp_AbilityType.getSelectedItemPosition();
        final String AbilityIntro = et_AbilityIntro.getText().toString();
        RequestBody formBody = new FormBody.Builder()
                .add("abiType", AbilityType + "")
                .add("abiName", AbilityIntro)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/createAbilityById")
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
                            int abiid = new Gson().fromJson(ResMsg, Integer.class);
                            ResumeView.StudentAbility studentAbility = new ResumeView.StudentAbility();
                            studentAbility.abiid = abiid;
                            studentAbility.abiType = AbilityType;
                            studentAbility.abiName = AbilityIntro;
                            resumeView.stu_ability.add(studentAbility);
                            studentAbilityListAdapter.notifyItemInserted(resumeView.stu_ability.size() - 1);
                            sp_AbilityType.setSelection(0);
                            et_AbilityIntro.setText("");
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }

    private void DeleteAbility(final int Position) {
        new AlertDialog.Builder(mainActivity)
                .setMessage("確定要刪除此技術嗎?")
                .setNeutralButton("取消", null)
                .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Request request = new Request.Builder()
                                .url(getString(R.string.BackEndPath) + "api/deleteAbilityById?abiid=" + resumeView.stu_ability.get(Position).abiid)
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
                                            resumeView.stu_ability.remove(Position);
                                            studentAbilityListAdapter.notifyItemRemoved(Position);
                                            studentAbilityListAdapter.notifyItemRangeChanged(0, resumeView.stu_ability.size());
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
