package tw.edu.nutc.iminternshipsystem;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import MyMethod.SharedService;
import ViewModel.VisitStudentView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class VisitStudentFragment extends MySharedFragment {
    private AddVisitRecordActivity addVisitRecordActivity;

    public VisitStudentView visitStudentView;

    private int SCid;

    public RecyclerView rv_VisitStudentQList;
    private VisitStudentAdapter visitStudentAdapter;
    private int[] answerArray;

    private String studentCount = "";
    private String studentClass = "";
    private int visitWay = 0;
    private String visitDate = "";
    private String visitComment = "";

    public VisitStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visit_student, container, false);
        addVisitRecordActivity = (AddVisitRecordActivity) getActivity();
        super.client = addVisitRecordActivity.client;
        initView(view);
        SCid = addVisitRecordActivity.getIntent().getIntExtra("SCid", -1);
        GetVisitStudentData();
        return view;
    }

    private void initView(View view) {

        rv_VisitStudentQList = (RecyclerView) view.findViewById(R.id.rv_VisitStudentQList);

        view.findViewById(R.id.iv_Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateStudentVisit();
            }
        });
    }

    private void GetVisitStudentData() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getNullStuInterview?SCid=" + SCid)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                addVisitRecordActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", addVisitRecordActivity);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatusCode = response.code();
                final String ResMsg = response.body().string();

                addVisitRecordActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            visitStudentView = new Gson().fromJson(ResMsg, VisitStudentView.class);
                            answerArray = new int[visitStudentView.InterviewQ.size()];
                            rv_VisitStudentQList.setLayoutManager(new LinearLayoutManager(addVisitRecordActivity, LinearLayoutManager.VERTICAL, false));
                            visitStudentAdapter = new VisitStudentAdapter();
                            visitStudentAdapter.setHeaderId(R.layout.visitstudent_head);
                            visitStudentAdapter.setFooterId(R.layout.footer);
                            rv_VisitStudentQList.setAdapter(visitStudentAdapter);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, addVisitRecordActivity);
                        }
                    }
                });

            }

        });
    }

    public class VisitStudentAdapter extends RecyclerView.Adapter<VisitStudentAdapter.ViewHolder> {

        public final int TYPE_HEADER = 0;  //说明是带有Header的
        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        private View mHeaderView;
        private int mHeaderId;
        private View mFooterView;
        private int mFooterId;

        public void setHeaderId(int HeaderId) {
            mHeaderId = HeaderId;
            notifyItemInserted(0);
        }

        public void setFooterId(int FooterId) {
            mFooterId = FooterId;
            notifyItemInserted(getItemCount() - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 && mHeaderId != 0) {
                //第一个item应该加载Header
                return TYPE_HEADER;
            }
            if (position == getItemCount() - 1 && mFooterId != 0) {
                //最后一个,应该加载Footer
                return TYPE_FOOTER;
            }
            return TYPE_NORMAL;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            if (mHeaderId != 0 && viewType == TYPE_HEADER) {
                mHeaderView = LayoutInflater.from(context).inflate(mHeaderId, parent, false);
                return new ViewHolder(mHeaderView);
            }

            if (mFooterId != 0 && viewType == TYPE_FOOTER) {
                mFooterView = LayoutInflater.from(context).inflate(mFooterId, parent, false);
                return new ViewHolder(mFooterView);
            }
            View view = LayoutInflater.from(context).inflate(R.layout.visitquestion_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_HEADER) {
                holder.tv_CompanyName.setText(visitStudentView.comName);
                holder.tv_StudentName.setText(visitStudentView.stuName);
                holder.tv_CompanyAddress.setText(visitStudentView.comAddress);
                holder.et_StudentCount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        studentCount = holder.et_StudentCount.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                holder.et_StudentClass.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        studentClass = holder.et_StudentClass.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                holder.rg_VisitWay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        visitWay = group.indexOfChild(group.findViewById(checkedId));
                    }
                });

                final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
                Calendar newCalendar = Calendar.getInstance();
                final DatePickerDialog datePickerDialog = new DatePickerDialog(addVisitRecordActivity, new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        holder.et_VisitDate.setText(dateFormatter.format(newDate.getTime()));
                    }

                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

                holder.et_VisitDate.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        visitDate = holder.et_VisitDate.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                holder.et_VisitDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePickerDialog.show();
                    }
                });

                holder.et_VisitComment.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        visitComment = holder.et_VisitComment.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                return;
            }

            if (getItemViewType(position) == TYPE_FOOTER) {
                holder.tv_Footer.setText("沒有更多題目囉!");
                return;
            }

            final int Position = position - 1;

            final VisitStudentView.Question question = visitStudentView.InterviewQ.get(Position);


            holder.tv_Question.setText(position + "." + question.insQuestion);

            if (question.insAnswerType == 0) {
                holder.rg_AnsType0.setVisibility(View.VISIBLE);
                holder.rg_AnsType1.setVisibility(View.GONE);
                holder.rg_AnsType0.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        answerArray[Position] = group.indexOfChild(group.findViewById(checkedId));
                    }
                });
            } else if (question.insAnswerType == 1) {
                holder.rg_AnsType0.setVisibility(View.GONE);
                holder.rg_AnsType1.setVisibility(View.VISIBLE);
                holder.rg_AnsType1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        answerArray[Position] = group.indexOfChild(group.findViewById(checkedId));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int NormalCount = visitStudentView.InterviewQ.size();
            if (mHeaderId != 0)
                NormalCount++;
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tv_Question;

            private RadioGroup rg_AnsType0;
//            private RadioButton rb_00;
//            private RadioButton rb_01;
//            private RadioButton rb_02;
//            private RadioButton rb_03;
//            private RadioButton rb_04;

            private RadioGroup rg_AnsType1;
//            private RadioButton rb_10;
//            private RadioButton rb_11;

            private TextView tv_CompanyName;
            private TextView tv_StudentName;
            private TextView tv_CompanyAddress;
            private EditText et_StudentCount;
            private EditText et_StudentClass;
            private RadioGroup rg_VisitWay;
            private EditText et_VisitDate;
            private EditText et_VisitComment;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mHeaderView) {
                    tv_CompanyName = (TextView) itemView.findViewById(R.id.tv_CompanyName);
                    tv_StudentName = (TextView) itemView.findViewById(R.id.tv_StudentName);
                    tv_CompanyAddress = (TextView) itemView.findViewById(R.id.tv_CompanyAddress);
                    et_StudentCount = (EditText) itemView.findViewById(R.id.et_StudentCount);
                    et_StudentClass = (EditText) itemView.findViewById(R.id.et_StudentClass);
                    rg_VisitWay = (RadioGroup) itemView.findViewById(R.id.rg_VisitWay);
                    et_VisitDate = (EditText) itemView.findViewById(R.id.et_VisitDate);
                    et_VisitComment = (EditText) itemView.findViewById(R.id.et_VisitComment);

                    return;
                }

                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                tv_Question = (TextView) itemView.findViewById(R.id.tv_Question);

                rg_AnsType0 = (RadioGroup) itemView.findViewById(R.id.rg_AnsType0);
//                rb_00 = (RadioButton) itemView.findViewById(R.id.rb_00);
//                rb_01 = (RadioButton) itemView.findViewById(R.id.rb_01);
//                rb_02 = (RadioButton) itemView.findViewById(R.id.rb_02);
//                rb_03 = (RadioButton) itemView.findViewById(R.id.rb_03);
//                rb_04 = (RadioButton) itemView.findViewById(R.id.rb_04);

                rg_AnsType1 = (RadioGroup) itemView.findViewById(R.id.rg_AnsType1);
//                rb_10 = (RadioButton) itemView.findViewById(R.id.rb_10);
//                rb_11 = (RadioButton) itemView.findViewById(R.id.rb_11);
            }
        }
    }

    private void CreateStudentVisit() {
        String Answer = "";
        for (int i = 0; i < answerArray.length; i++) {
            Answer += answerArray[i];
            if (i != visitStudentView.InterviewQ.size() - 1)
                Answer += ",";
        }
        RequestBody formBody = new FormBody.Builder()
                .add("SCid", SCid + "")
                .add("insDate", visitDate)
                .add("insNum", studentCount)
                .add("insStuClass", studentClass)
                .add("insVisitWay", visitWay + "")
                .add("insComments", visitComment + "")
                .add("insAns", Answer)
                .add("insQuestionVer", visitStudentView.InterviewQ.get(0).insQuestionVer + "")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/teacherCreateStuInterview")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                addVisitRecordActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", addVisitRecordActivity);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int StatusCode = response.code();
                final String ResMsg = response.body().string();

                addVisitRecordActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("新增成功", addVisitRecordActivity);
                            addVisitRecordActivity.finish();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, addVisitRecordActivity);
                        }
                    }
                });
            }
        });
    }
}

