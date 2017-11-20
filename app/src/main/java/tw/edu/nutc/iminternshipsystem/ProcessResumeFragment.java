package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tsaiweb.bottompopmenu.BottomMenuFragment;
import com.tsaiweb.bottompopmenu.MenuItem;
import com.tsaiweb.bottompopmenu.MenuItemOnClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import MyMethod.SharedService;
import ViewModel.ProcessResumeView;
import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitTextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProcessResumeFragment extends MySharedFragment {

    private final int INVITE_INTERVIEW_CODE = 60;
    private MainActivity mainActivity;
    public ProcessResumeView processResumeView;

    public RecyclerView rv_ResumeList;
    private ResumeListAdapter resumeListAdapter;
    private int nowPosition = -1;

    public ProcessResumeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_process_resume, container, false);
        mainActivity = (MainActivity) getActivity();
        super.activity = mainActivity;
        super.client = mainActivity.client;
        super.imageClient = SharedService.GetClient(mainActivity);
        SetCache((int) Runtime.getRuntime().maxMemory() / 20);
        initView(view);
        GetResumeList();
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        rv_ResumeList = (RecyclerView) view.findViewById(R.id.rv_ResumeList);
    }

    private void GetResumeList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/companyGetResumeByAccount")
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
                            processResumeView = new Gson().fromJson(ResMsg, ProcessResumeView.class);

                            rv_ResumeList.setLayoutManager(new GridLayoutManager(mainActivity, 2));
                            resumeListAdapter = new ResumeListAdapter();
                            rv_ResumeList.setAdapter(resumeListAdapter);

                            if (processResumeView.ResumeList.size() == 0)
                                SharedService.ShowTextToast("無任何履歷投遞", mainActivity);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    public class ResumeListAdapter extends RecyclerView.Adapter<ResumeListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            View view = LayoutInflater.from(context).inflate(R.layout.mystudent_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.atv_JobDuty.setVisibility(View.VISIBLE);
            holder.atv_JobDuty.setText(processResumeView.ResumeList.get(position).jDuties);

            int width = mainActivity.myWidth / 3;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
            holder.civ_StudentImage.setLayoutParams(layoutParams);
            holder.civ_StudentImage.setImageResource(R.drawable.defaultmimg);
            if (processResumeView.ResumeList.get(position).stu_basic.profilePic != null) {
                holder.civ_StudentImage.setTag(processResumeView.ResumeList.get(position).stu_basic.profilePic);
                showImage(holder.civ_StudentImage, processResumeView.ResumeList.get(position).stu_basic.profilePic, true, null);
            } else {
                holder.civ_StudentImage.setTag("");
            }

            holder.tv_StudentName.setText(processResumeView.ResumeList.get(position).stu_basic.chiName);

            holder.ll_MyStudent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    BottomMenuFragment bottomMenuFragment = new BottomMenuFragment();

                    List<MenuItem> menuItemList = new ArrayList<MenuItem>();

                    MenuItem menuItem1 = new MenuItem();
                    menuItem1.setText("查看履歷");
                    menuItem1.setStyle(MenuItem.MenuItemStyle.COMMON);
                    menuItem1.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                        @Override
                        public void onClickMenuItem(View v, MenuItem menuItem) {
                            Intent intent = new Intent(mainActivity, MyStudentResumeActivity.class);
                            intent.putExtra("ResumeView", new Gson().toJson(processResumeView.ResumeList.get(position)));
                            intent.putExtra("StudentName", processResumeView.ResumeList.get(position).stu_basic.chiName);
                            startActivity(intent);
                        }
                    });

                    MenuItem menuItem2 = new MenuItem();
                    menuItem2.setText("邀請面試");
                    menuItem2.setStyle(MenuItem.MenuItemStyle.COMMON);
                    menuItem2.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                        @Override
                        public void onClickMenuItem(View v, MenuItem menuItem) {
                            nowPosition = position;
                            Intent intent = new Intent(mainActivity, InviteInterviewActivity.class);
                            intent.putExtra("mid", processResumeView.ResumeList.get(position).mid);
                            startActivityForResult(intent, INVITE_INTERVIEW_CODE);
                        }
                    });

                    MenuItem menuItem3 = new MenuItem();
                    menuItem3.setText("直接錄取");
                    menuItem3.setStyle(MenuItem.MenuItemStyle.COMMON);
                    menuItem3.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                        @Override
                        public void onClickMenuItem(View v, MenuItem menuItem) {
                            new AlertDialog.Builder(mainActivity)
                                    .setIcon(R.drawable.internlogo)
                                    .setMessage("確定要直接錄取 " + processResumeView.ResumeList.get(position).stu_basic.chiName + " 嗎?")
                                    .setPositiveButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AcceptResume(position);
                                                }
                                            })
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                    });

                    MenuItem menuItem4 = new MenuItem();
                    menuItem4.setText("拒絕履歷");
                    menuItem4.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                        @Override
                        public void onClickMenuItem(View v, MenuItem menuItem) {
                            View view = LayoutInflater.from(mainActivity).inflate(R.layout.reject_resume_reason_block, null, false);
                            final EditText et_Reason = (EditText) view.findViewById(R.id.et_Reason);

                            new AlertDialog.Builder(mainActivity)
                                    .setView(view)
                                    .setPositiveButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    RejectResume(position, et_Reason.getText().toString());
                                                }
                                            })
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                    });

                    menuItemList.add(menuItem1);
                    menuItemList.add(menuItem2);
                    menuItemList.add(menuItem3);
                    menuItemList.add(menuItem4);

                    bottomMenuFragment.setMenuItems(menuItemList);

                    bottomMenuFragment.show(getFragmentManager(), "BottomMenuFragment");
                }
            });
        }

        @Override
        public int getItemCount() {
            return processResumeView.ResumeList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_MyStudent;
            private AutofitTextView atv_JobDuty;
            private CircleImageView civ_StudentImage;
            private TextView tv_StudentName;

            public ViewHolder(View itemView) {
                super(itemView);
                ll_MyStudent = (LinearLayout) itemView.findViewById(R.id.ll_MyStudent);
                atv_JobDuty = (AutofitTextView) itemView.findViewById(R.id.atv_JobDuty);
                civ_StudentImage = (CircleImageView) itemView.findViewById(R.id.civ_StudentImage);
                tv_StudentName = (TextView) itemView.findViewById(R.id.tv_StudentName);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INVITE_INTERVIEW_CODE && resultCode == mainActivity.RESULT_OK) {
            processResumeView.ResumeList.remove(nowPosition);
            resumeListAdapter.notifyItemRemoved(nowPosition);
            resumeListAdapter.notifyItemRangeChanged(0, processResumeView.ResumeList.size());
            nowPosition = -1;
        }
    }

    private void AcceptResume(final int Position) {
        RequestBody formBody = new FormBody.Builder()
                .add("mid", processResumeView.ResumeList.get(Position).mid + "")
                .add("mstatus", "2")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/companyAcceptResume")
                .put(formBody)
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
                            SharedService.ShowTextToast("錄取成功", mainActivity);
                            processResumeView.ResumeList.remove(Position);
                            resumeListAdapter.notifyItemRemoved(Position);
                            resumeListAdapter.notifyItemRangeChanged(0, processResumeView.ResumeList.size());
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }

    private void RejectResume(final int Position, String Reason) {
        RequestBody formBody = new FormBody.Builder()
                .add("mid", processResumeView.ResumeList.get(Position).mid + "")
                .add("mfailedreason", Reason)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/companyRejectResume")
                .put(formBody)
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
                            SharedService.ShowTextToast("拒絕成功", mainActivity);
                            processResumeView.ResumeList.remove(Position);
                            resumeListAdapter.notifyItemRemoved(Position);
                            resumeListAdapter.notifyItemRangeChanged(0, processResumeView.ResumeList.size());
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }
}
