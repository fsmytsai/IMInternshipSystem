package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.MyJobView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyJobFragment extends MySharedFragment {

    private final int EDITJOB_CODE = 517;
    private MainActivity mainActivity;
    private SwipeRefreshLayout mSwipeLayout;

    public MyJobView myJobView;

    private boolean isFirstLoad = true;
    private boolean isLoading = true;
    private boolean isFinishLoad = false;

    public RecyclerView rv_MyJobList;
    private MyJobListAdapter myJobListAdapter;

    private int nowPosition = -1;
    private ImageView iv_AddJob;

    public MyJobFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_job, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;

        initView(view);
        Refresh();
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        rv_MyJobList = (RecyclerView) view.findViewById(R.id.rv_MyJobList);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_MyJobList);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //檢查網路連線
                if (!SharedService.CheckNetWork(mainActivity)) {
                    SharedService.ShowTextToast("請檢察網路連線", mainActivity);
                    mSwipeLayout.setRefreshing(false);
                    return;
                }
                Refresh();
            }
        });
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(mainActivity, R.color.colorTransparent));
        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeLayout.setDistanceToTriggerSync(400);// 设置手指在屏幕下拉多少距离会触发下拉刷新
        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);

        iv_AddJob = (ImageView) view.findViewById(R.id.iv_AddJob);
        iv_AddJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, MyWebViewActivity.class);
                intent.putExtra("URL", getString(R.string.FrontEndPath) + "jobadd_ent&Token=" + SharedService.token);
                startActivity(intent);
            }
        });
    }

    private void Refresh() {
        mSwipeLayout.setRefreshing(true);

        myJobView = new MyJobView();
        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            isFinishLoad = false;
            myJobListAdapter.mFooterId = 0;
            myJobListAdapter.notifyDataSetChanged();
        }
        GetMyJobList();
    }

    private void GetMyJobList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getJobOpeningByToken?page=" + (myJobView.current_page + 1))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 停止刷新
                        mSwipeLayout.setRefreshing(false);
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
                        // 停止刷新
                        mSwipeLayout.setRefreshing(false);
                        //請求完畢
                        isLoading = false;

                        if (StatusCode == 200) {
                            MyJobView tempView = new Gson().fromJson(ResMsg, MyJobView.class);

                            myJobView.current_page = tempView.current_page;
                            myJobView.data.addAll(tempView.data);

                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_MyJobList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                myJobListAdapter = new MyJobListAdapter();
                                rv_MyJobList.setAdapter(myJobListAdapter);
                            } else {
                                myJobListAdapter.notifyDataSetChanged();
                            }

                            if (tempView.current_page >= tempView.last_page) {
                                //最後一次載入
                                isFinishLoad = true;
//                                View footer = LayoutInflater.from(getActivity()).inflate(R.layout.footer, rv_MyJobList, false);
                                myJobListAdapter.setFooterId(R.layout.footer);
                            }
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }


    public class MyJobListAdapter extends RecyclerView.Adapter<MyJobListAdapter.ViewHolder> {

        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        public final int TYPE_DETAIL = 3;
        private View mFooterView;
        private int mFooterId;

        private boolean isAnimatingOut = false;

        public MyJobListAdapter() {
            rv_MyJobList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                }

                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy < 0) {
                        if (!isAnimatingOut) {
                            iv_AddJob.setVisibility(View.VISIBLE);
                            ViewCompat.animate(iv_AddJob)
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1.0f)
                                    .setDuration(800)
                                    .setListener(new ViewPropertyAnimatorListener() {

                                        @Override
                                        public void onAnimationStart(View view) {
                                            isAnimatingOut = true;
                                        }

                                        @Override
                                        public void onAnimationEnd(View view) {
                                            isAnimatingOut = false;
                                        }

                                        @Override
                                        public void onAnimationCancel(View arg0) {
                                            isAnimatingOut = false;
                                        }
                                    })
                                    .setInterpolator(new FastOutSlowInInterpolator())
                                    .start();
                        }

                    } else if (dy > 0) {
                        if (iv_AddJob.getVisibility() != View.GONE && !isAnimatingOut) {
                            ViewCompat.animate(iv_AddJob)
                                    .scaleX(0.0f)
                                    .scaleY(0.0f)
                                    .alpha(0.0f)
                                    .setDuration(800)
                                    .setInterpolator(new FastOutSlowInInterpolator())
                                    .setListener(new ViewPropertyAnimatorListener() {

                                        @Override
                                        public void onAnimationStart(View view) {
                                            isAnimatingOut = true;
                                        }

                                        @Override
                                        public void onAnimationEnd(View view) {
                                            isAnimatingOut = false;
                                            view.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onAnimationCancel(View arg0) {
                                            isAnimatingOut = false;
                                        }
                                    })
                                    .start();
                        }
                    }
                }
            });
        }

        public void setFooterId(int FooterId) {
            mFooterId = FooterId;
            notifyItemInserted(getItemCount() - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1 && mFooterId != 0) {
                return TYPE_FOOTER;
            }
            if (myJobView.data.get(position).isDetail)
                return TYPE_DETAIL;
            return TYPE_NORMAL;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            if (mFooterId != 0 && viewType == TYPE_FOOTER) {
                mFooterView = LayoutInflater.from(context).inflate(mFooterId, parent, false);
                return new ViewHolder(mFooterView);
            }

            View view;
            if (viewType == TYPE_DETAIL)
                view = LayoutInflater.from(context).inflate(R.layout.myjob_detailblock, parent, false);
            else
                view = LayoutInflater.from(context).inflate(R.layout.myjob_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (myJobView.data.size() == 0)
                    holder.tv_Footer.setText("您沒有任何職缺!");
                else
                    holder.tv_Footer.setText("沒有更多職缺囉!");
                return;
            }

            MyJobView.MyJob myJob = myJobView.data.get(position);

            if (myJob.jtypes == 0)
                holder.tv_JobType.setText("暑期實習");
            else if (myJob.jtypes == 1)
                holder.tv_JobType.setText("學期實習");
            else if (myJob.jtypes == 2)
                holder.tv_JobType.setText("工讀");
            else if (myJob.jtypes == 3)
                holder.tv_JobType.setText("正職");

            holder.tv_JobDuty.setText(myJob.jduties);
            holder.tv_JobSalary.setText(myJob.jsalary_low + "~" + myJob.jsalary_up);
            holder.tv_JobPlace.setText(myJob.jaddress);
            holder.tv_JobTime.setText(myJob.jStartDutyTime + "~" + myJob.jEndDutyTime);

            if (myJob.isDetail) {
                holder.tv_ContactName.setText(myJob.jcontact_name);
                holder.tv_ContactPhone.setText(myJob.jcontact_phone);
                holder.tv_ContactEmail.setText(myJob.jcontact_email);
                holder.tv_JobCount.setText(myJob.jNOP + "");
                holder.tv_ResumeCount.setText(myJob.jResume_num + "");
                holder.tv_ExpireTime.setText(myJob.jdeadline);
                holder.tv_JobDetail.setText(myJob.jdetails);
                holder.ll_MyJobDetailBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myJobView.data.get(position).isDetail = false;
                        myJobListAdapter.notifyItemChanged(position);
                    }
                });
            } else {
                holder.ll_MyJobBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myJobView.data.get(position).isDetail = true;
                        myJobListAdapter.notifyItemChanged(position);
                    }
                });
            }

            holder.bt_JobEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nowPosition == -1) {
                        nowPosition = position;
                        EditJob();
                    }
                }
            });

            holder.bt_JobDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeleteJob(position);
                }
            });

            //避免重複請求
            if (position > myJobView.data.size() * 0.6 && !isFinishLoad && !isLoading) {
                isLoading = true;
                GetMyJobList();
            }
        }

        @Override
        public int getItemCount() {
            int NormalCount = myJobView.data.size();
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_MyJobBlock;
            private TextView tv_JobType;
            private TextView tv_JobDuty;
            private TextView tv_JobSalary;
            private TextView tv_JobPlace;
            private TextView tv_JobTime;
            private Button bt_JobEdit;
            private Button bt_JobDelete;

            private LinearLayout ll_MyJobDetailBlock;
            private TextView tv_ContactName;
            private TextView tv_ContactPhone;
            private TextView tv_ContactEmail;
            private TextView tv_JobCount;
            private TextView tv_ResumeCount;
            private TextView tv_ExpireTime;
            private TextView tv_JobDetail;


            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_MyJobBlock = (LinearLayout) itemView.findViewById(R.id.ll_MyJobBlock);
                tv_JobType = (TextView) itemView.findViewById(R.id.tv_JobType);
                tv_JobDuty = (TextView) itemView.findViewById(R.id.tv_JobDuty);
                tv_JobSalary = (TextView) itemView.findViewById(R.id.tv_JobSalary);
                tv_JobPlace = (TextView) itemView.findViewById(R.id.tv_JobPlace);
                tv_JobTime = (TextView) itemView.findViewById(R.id.tv_JobTime);
                bt_JobEdit = (Button) itemView.findViewById(R.id.bt_JobEdit);
                bt_JobDelete = (Button) itemView.findViewById(R.id.bt_JobDelete);

                ll_MyJobDetailBlock = (LinearLayout) itemView.findViewById(R.id.ll_MyJobDetailBlock);
                tv_ContactName = (TextView) itemView.findViewById(R.id.tv_ContactName);
                tv_ContactPhone = (TextView) itemView.findViewById(R.id.tv_ContactPhone);
                tv_ContactEmail = (TextView) itemView.findViewById(R.id.tv_ContactEmail);
                tv_JobCount = (TextView) itemView.findViewById(R.id.tv_JobCount);
                tv_ResumeCount = (TextView) itemView.findViewById(R.id.tv_ResumeCount);
                tv_ExpireTime = (TextView) itemView.findViewById(R.id.tv_ExpireTime);
                tv_JobDetail = (TextView) itemView.findViewById(R.id.tv_JobDetail);
            }
        }
    }

    private void EditJob() {
        Intent intent = new Intent(mainActivity, EditJobActivity.class);
        intent.putExtra("MyJobData", new Gson().toJson(myJobView.data.get(nowPosition)));
        startActivityForResult(intent, EDITJOB_CODE);
        nowPosition = -1;
    }

    private void DeleteJob(final int Position) {
        new AlertDialog.Builder(mainActivity)
                .setMessage("確定要刪除此職缺嗎?")
                .setNeutralButton("取消", null)
                .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Request request = new Request.Builder()
                                .url(getString(R.string.BackEndPath) + "api/deleteJobOpeningByCom?joid=" + myJobView.data.get(Position).joid)
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
                                            myJobView.data.remove(Position);

                                            myJobListAdapter.notifyItemRemoved(Position);
                                            int dItemCount = myJobView.data.size() - Position;
                                            myJobListAdapter.notifyItemRangeChanged(Position, dItemCount);
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
