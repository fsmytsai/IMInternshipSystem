package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.AllJobView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllJobFragment extends MySharedFragment {
    private MainActivity mainActivity;
    private SwipeRefreshLayout mSwipeLayout;

    public AllJobView allJobView;

    private boolean isFirstLoad = true;
    private boolean isLoading = true;
    private boolean isFinishLoad = false;

    public RecyclerView rv_AllJobList;
    private AllJobListAdapter allJobListAdapter;

    private ImageView iv_ToTop;

    private int jTypes = -1;
    private int sortBy = 1;
    private int jSalary_Low = 0;
    private String keyword = "";

    public AllJobFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_job, container, false);
        mainActivity = (MainActivity) getActivity();
        super.activity = mainActivity;
        super.client = mainActivity.client;
        super.imageClient = SharedService.GetClient(mainActivity);
        SetCache((int) Runtime.getRuntime().maxMemory() / 20);
        initView(view);
        Refresh();
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        rv_AllJobList = (RecyclerView) view.findViewById(R.id.rv_AllJobList);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_AllJobList);
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

        iv_ToTop = (ImageView) view.findViewById(R.id.iv_ToTop);
        iv_ToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_AllJobList.smoothScrollToPosition(0);
            }
        });
    }

    private void Refresh() {
        mSwipeLayout.setRefreshing(true);

        allJobView = new AllJobView();

        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            isFinishLoad = false;
            allJobListAdapter.mFooterId = 0;
            allJobListAdapter.notifyDataSetChanged();
        }
        GetAllJobList();
    }

    private void GetAllJobList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getJobOpeningBySearch?jtypes=" + jTypes + "&sortBy=" + sortBy + "&jsalary_lows=" + jSalary_Low + "&keyword=" + keyword + "&page=" + (allJobView.current_page + 1))
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
                            AllJobView tempView = new Gson().fromJson(ResMsg, AllJobView.class);

                            allJobView.current_page = tempView.current_page;
                            allJobView.data.addAll(tempView.data);

                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_AllJobList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                allJobListAdapter = new AllJobListAdapter();
                                allJobListAdapter.setHeaderId(R.layout.alljob_head);
                                rv_AllJobList.setAdapter(allJobListAdapter);
                            } else {
                                allJobListAdapter.notifyDataSetChanged();
                            }

                            if (tempView.current_page >= tempView.last_page) {
                                //最後一次載入
                                isFinishLoad = true;

                                allJobListAdapter.setFooterId(R.layout.footer);
                            }
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }


    public class AllJobListAdapter extends RecyclerView.Adapter<AllJobListAdapter.ViewHolder> {

        public final int TYPE_HEADER = 0;  //说明是带有Header的
        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        public final int TYPE_DETAIL = 3;
        private View mHeaderView;
        private int mHeaderId;
        private View mFooterView;
        private int mFooterId;

        private boolean isAnimatingOut = false;

        public AllJobListAdapter() {
            rv_AllJobList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            iv_ToTop.setVisibility(View.VISIBLE);
                            ViewCompat.animate(iv_ToTop)
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
                                    .start();
                        }

                    } else if (dy > 0) {
                        if (iv_ToTop.getVisibility() != View.GONE && !isAnimatingOut) {
                            ViewCompat.animate(iv_ToTop)
                                    .scaleX(0.0f)
                                    .scaleY(0.0f)
                                    .alpha(0.0f)
                                    .setDuration(800)
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
            if (allJobView.data.get(position - 1).isDetail)
                return TYPE_DETAIL;
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

            View view;
            if (viewType == TYPE_DETAIL)
                view = LayoutInflater.from(context).inflate(R.layout.alljob_detailblock, parent, false);
            else
                view = LayoutInflater.from(context).inflate(R.layout.alljob_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_HEADER) {
                holder.ll_AllJobFilterTop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedService.ShowAndHideBlock(holder.ll_AllJobFilter, holder.iv_UpAndDown);
//                        if (holder.ll_AllJobFilter.getVisibility() == View.VISIBLE) {
//                            holder.iv_UpAndDown.setImageResource(R.drawable.down);
//                            holder.ll_AllJobFilter.animate()
//                                    .translationY(-holder.ll_AllJobFilter.getHeight())
//                                    .alpha(0.0f)
//                                    .setDuration(300)
//                                    .setListener(new AnimatorListenerAdapter() {
//                                        @Override
//                                        public void onAnimationEnd(Animator animation) {
//                                            super.onAnimationEnd(animation);
//                                            holder.ll_AllJobFilter.setVisibility(View.GONE);
//                                        }
//                                    });
//                        } else {
//                            holder.iv_UpAndDown.setImageResource(R.drawable.up);
//                            holder.ll_AllJobFilter.setVisibility(View.VISIBLE);
//                            holder.ll_AllJobFilter.animate()
//                                    .translationY(0)
//                                    .alpha(1.0f)
//                                    .setDuration(300)
//                                    .setListener(new AnimatorListenerAdapter() {
//                                        @Override
//                                        public void onAnimationEnd(Animator animation) {
//                                            super.onAnimationEnd(animation);
//                                            holder.ll_AllJobFilter.setVisibility(View.VISIBLE);
//                                        }
//                                    });
//                        }
                    }
                });

                holder.iv_SearchJob.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        keyword = holder.et_SearchJob.getText().toString();
                        Refresh();
                    }
                });

                SetHeader(holder.ll_AllJobFilter);
                return;
            }

            if (getItemViewType(position) == TYPE_FOOTER) {
                if (allJobView.data.size() == 0)
                    holder.tv_Footer.setText("還沒有任何職缺!");
                else
                    holder.tv_Footer.setText("沒有更多職缺囉!");
                return;
            }

            final int Position = position - 1;

            final AllJobView.AllJob allJob = allJobView.data.get(Position);

            if (allJob.jtypes == 0)
                holder.tv_JobType.setText("暑期實習");
            else if (allJob.jtypes == 1)
                holder.tv_JobType.setText("學期實習");
            else if (allJob.jtypes == 2)
                holder.tv_JobType.setText("工讀");
            else if (allJob.jtypes == 3)
                holder.tv_JobType.setText("正職");

            if (allJob.profilePic != null) {
                holder.iv_CompanyImage.setImageDrawable(null);
                holder.iv_CompanyImage.setTag(allJob.profilePic);
                showImage(holder.iv_CompanyImage, allJob.profilePic, true, null);
            } else {
                holder.iv_CompanyImage.setTag("");
                holder.iv_CompanyImage.setImageResource(R.drawable.defaultmimg);
            }

            holder.tv_CompanyName.setText(allJob.c_name);
            holder.tv_JobDuty.setText(allJob.jduties);

            if (allJob.isDetail) {
                holder.tv_JobSalary.setText(allJob.jsalary_low + "~" + allJob.jsalary_up);
                holder.tv_JobPlace.setText(allJob.jaddress);
                holder.tv_JobTime.setText(allJob.jStartDutyTime + "~" + allJob.jEndDutyTime);
                holder.tv_ContactName.setText(allJob.jcontact_name);
                holder.tv_ContactPhone.setText(allJob.jcontact_phone);
                holder.tv_ContactEmail.setText(allJob.jcontact_email);
                holder.tv_JobCount.setText(allJob.jNOP + "");
                holder.tv_ResumeCount.setText(allJob.jResume_num + "");
                holder.tv_ExpireTime.setText(allJob.jdeadline);
                holder.tv_JobDetail.setText(allJob.jdetails);
                holder.ll_AllJobDetailBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allJobView.data.get(Position).isDetail = false;
                        allJobListAdapter.notifyItemChanged(Position);
                    }
                });

                if (SharedService.identityView.u_status != 0) {
                    holder.bt_SubmitResume.setVisibility(View.GONE);
                } else {
                    holder.bt_SubmitResume.setVisibility(View.VISIBLE);
                    if (allJob.jResume_submitted) {
                        holder.bt_SubmitResume.setText("已投遞");
                        holder.bt_SubmitResume.setBackgroundColor(Color.parseColor("#626262"));
                        holder.bt_SubmitResume.setOnClickListener(null);
                    } else {
                        holder.bt_SubmitResume.setText("投遞履歷");
                        holder.bt_SubmitResume.setBackgroundColor(ContextCompat.getColor(mainActivity, android.R.color.black));
                        holder.bt_SubmitResume.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SubmitResume(Position);
                            }
                        });
                    }
                }

                holder.bt_CompanyDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mainActivity, CompanyDetailActivity.class);
                        intent.putExtra("c_account", allJob.c_account);
                        startActivity(intent);
                    }
                });

            } else {

                holder.tv_JobCreateTime.setText(allJob.created_at);
                holder.ll_AllJobBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allJobView.data.get(Position).isDetail = true;
                        allJobListAdapter.notifyItemChanged(Position);
                    }
                });
            }

            //避免重複請求
            if (position > allJobView.data.size() * 0.6 && !isFinishLoad && !isLoading) {
                isLoading = true;
                GetAllJobList();
            }
        }

        @Override
        public int getItemCount() {
            int NormalCount = allJobView.data.size();
            if (mHeaderId != 0)
                NormalCount++;
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_AllJobBlock;
            private ImageView iv_CompanyImage;
            private TextView tv_CompanyName;
            private TextView tv_JobType;
            private TextView tv_JobDuty;
            private TextView tv_JobCreateTime;

            private LinearLayout ll_AllJobDetailBlock;
            private TextView tv_JobSalary;
            private TextView tv_JobPlace;
            private TextView tv_JobTime;
            private TextView tv_ContactName;
            private TextView tv_ContactPhone;
            private TextView tv_ContactEmail;
            private TextView tv_JobCount;
            private TextView tv_ResumeCount;
            private TextView tv_ExpireTime;
            private TextView tv_JobDetail;
            private Button bt_SubmitResume;
            private Button bt_CompanyDetail;

            private ImageView iv_UpAndDown;
            private LinearLayout ll_AllJobFilterTop;
            private LinearLayout ll_AllJobFilter;
            private EditText et_SearchJob;
            private ImageView iv_SearchJob;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mHeaderView) {
                    iv_UpAndDown = (ImageView) itemView.findViewById(R.id.iv_UpAndDown);
                    ll_AllJobFilterTop = (LinearLayout) itemView.findViewById(R.id.ll_AllJobFilterTop);
                    ll_AllJobFilter = (LinearLayout) itemView.findViewById(R.id.ll_AllJobFilter);
                    et_SearchJob = (EditText) itemView.findViewById(R.id.et_SearchJob);
                    iv_SearchJob = (ImageView) itemView.findViewById(R.id.iv_SearchJob);

                    return;
                }
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_AllJobBlock = (LinearLayout) itemView.findViewById(R.id.ll_AllJobBlock);
                iv_CompanyImage = (ImageView) itemView.findViewById(R.id.iv_CompanyImage);
                tv_CompanyName = (TextView) itemView.findViewById(R.id.tv_CompanyName);
                tv_JobType = (TextView) itemView.findViewById(R.id.tv_JobType);
                tv_JobDuty = (TextView) itemView.findViewById(R.id.tv_JobDuty);
                tv_JobCreateTime = (TextView) itemView.findViewById(R.id.tv_JobCreateTime);

                ll_AllJobDetailBlock = (LinearLayout) itemView.findViewById(R.id.ll_AllJobDetailBlock);
                tv_JobSalary = (TextView) itemView.findViewById(R.id.tv_JobSalary);
                tv_JobPlace = (TextView) itemView.findViewById(R.id.tv_JobPlace);
                tv_JobTime = (TextView) itemView.findViewById(R.id.tv_JobTime);
                tv_ContactName = (TextView) itemView.findViewById(R.id.tv_ContactName);
                tv_ContactPhone = (TextView) itemView.findViewById(R.id.tv_ContactPhone);
                tv_ContactEmail = (TextView) itemView.findViewById(R.id.tv_ContactEmail);
                tv_JobCount = (TextView) itemView.findViewById(R.id.tv_JobCount);
                tv_ResumeCount = (TextView) itemView.findViewById(R.id.tv_ResumeCount);
                tv_ExpireTime = (TextView) itemView.findViewById(R.id.tv_ExpireTime);
                tv_JobDetail = (TextView) itemView.findViewById(R.id.tv_JobDetail);
                bt_SubmitResume = (Button) itemView.findViewById(R.id.bt_SubmitResume);
                bt_CompanyDetail = (Button) itemView.findViewById(R.id.bt_CompanyDetail);
            }
        }
    }

    private View.OnClickListener MyClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.tv_JobType) {
                if (jTypes != -1) {
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_SummerIntern).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_SemesterIntern).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_WorkStudy).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_Career).setBackground(null);

                    jTypes = -1;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_SummerIntern) {
                if (jTypes != 0) {
                    rv_AllJobList.findViewById(R.id.tv_JobType).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_SemesterIntern).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_WorkStudy).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_Career).setBackground(null);

                    jTypes = 0;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_SemesterIntern) {
                if (jTypes != 1) {
                    rv_AllJobList.findViewById(R.id.tv_JobType).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_SummerIntern).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_WorkStudy).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_Career).setBackground(null);

                    jTypes = 1;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_WorkStudy) {
                if (jTypes != 2) {
                    rv_AllJobList.findViewById(R.id.tv_JobType).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_SummerIntern).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_SemesterIntern).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_Career).setBackground(null);

                    jTypes = 2;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_Career) {
                if (jTypes != 3) {
                    rv_AllJobList.findViewById(R.id.tv_JobType).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_SummerIntern).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_SemesterIntern).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_WorkStudy).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));

                    jTypes = 3;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_NewToOld) {
                if (sortBy != 1) {
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_OldToNew).setBackground(null);

                    rv_AllJobList.findViewById(R.id.tv_JobFilterSalary)
                            .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_HighToLow).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_LowToHigh).setBackground(null);

                    sortBy = 1;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_OldToNew) {
                if (sortBy != 2) {
                    rv_AllJobList.findViewById(R.id.tv_NewToOld).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));

                    rv_AllJobList.findViewById(R.id.tv_JobFilterSalary)
                            .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_HighToLow).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_LowToHigh).setBackground(null);

                    sortBy = 2;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_JobFilterSalary) {
                if (sortBy == 3 || sortBy == 4) {
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_HighToLow).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_LowToHigh).setBackground(null);

                    sortBy = 1;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_HighToLow) {
                if (sortBy != 3) {
                    rv_AllJobList.findViewById(R.id.tv_NewToOld)
                            .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_OldToNew).setBackground(null);

                    rv_AllJobList.findViewById(R.id.tv_JobFilterSalary).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_LowToHigh).setBackground(null);

                    sortBy = 3;
                    Refresh();
                }
            } else if (v.getId() == R.id.tv_LowToHigh) {
                if (sortBy != 4) {
                    rv_AllJobList.findViewById(R.id.tv_NewToOld)
                            .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
                    rv_AllJobList.findViewById(R.id.tv_OldToNew).setBackground(null);

                    rv_AllJobList.findViewById(R.id.tv_JobFilterSalary).setBackground(null);
                    rv_AllJobList.findViewById(R.id.tv_HighToLow).setBackground(null);
                    v.setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));

                    sortBy = 4;
                    Refresh();
                }
            }
        }
    };

    private void SubmitResume(final int Position) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("投遞履歷")
                .setMessage("確定要對此職缺投遞履歷嗎?")
                .setNegativeButton("取消", null)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RequestBody formBody = new FormBody.Builder()
                                .add("joid", allJobView.data.get(Position).joid + "")
                                .build();

                        Request request = new Request.Builder()
                                .url(getString(R.string.BackEndPath) + "api/studentSubmitResume")
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
                                            SharedService.ShowTextToast("投遞履歷成功", mainActivity);
                                            allJobView.data.get(Position).jResume_submitted = true;
                                            allJobListAdapter.notifyDataSetChanged();
                                        } else {
                                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).show();
    }

    private void SetHeader(LinearLayout ll_AllJobFilter) {
        ll_AllJobFilter.findViewById(R.id.tv_JobType).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_SummerIntern).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_SemesterIntern).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_WorkStudy).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_Career).setOnClickListener(MyClick);

        ll_AllJobFilter.findViewById(R.id.tv_NewToOld).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_OldToNew).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_JobFilterSalary).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_HighToLow).setOnClickListener(MyClick);
        ll_AllJobFilter.findViewById(R.id.tv_LowToHigh).setOnClickListener(MyClick);


        if (jTypes == -1) {
            ll_AllJobFilter.findViewById(R.id.tv_JobType)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_SummerIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SemesterIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_WorkStudy).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_Career).setBackground(null);

        } else if (jTypes == 0) {
            ll_AllJobFilter.findViewById(R.id.tv_JobType).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SummerIntern)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_SemesterIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_WorkStudy).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_Career).setBackground(null);

        } else if (jTypes == 1) {
            ll_AllJobFilter.findViewById(R.id.tv_JobType).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SummerIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SemesterIntern)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_WorkStudy).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_Career).setBackground(null);

        } else if (jTypes == 2) {
            ll_AllJobFilter.findViewById(R.id.tv_JobType).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SummerIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SemesterIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_WorkStudy)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_Career).setBackground(null);

        } else if (jTypes == 3) {
            ll_AllJobFilter.findViewById(R.id.tv_JobType).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SummerIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_SemesterIntern).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_WorkStudy).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_Career)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));

        }


        if (sortBy == 1) {
            ll_AllJobFilter.findViewById(R.id.tv_NewToOld)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_OldToNew).setBackground(null);

            ll_AllJobFilter.findViewById(R.id.tv_JobFilterSalary)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_HighToLow).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_LowToHigh).setBackground(null);
        } else if (sortBy == 2) {
            ll_AllJobFilter.findViewById(R.id.tv_NewToOld).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_OldToNew)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));

            ll_AllJobFilter.findViewById(R.id.tv_JobFilterSalary)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_HighToLow).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_LowToHigh).setBackground(null);
        } else if (sortBy == 3) {
            ll_AllJobFilter.findViewById(R.id.tv_NewToOld)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_OldToNew).setBackground(null);

            ll_AllJobFilter.findViewById(R.id.tv_JobFilterSalary).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_HighToLow)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_LowToHigh).setBackground(null);
        } else if (sortBy == 4) {
            ll_AllJobFilter.findViewById(R.id.tv_NewToOld)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
            ll_AllJobFilter.findViewById(R.id.tv_OldToNew).setBackground(null);

            ll_AllJobFilter.findViewById(R.id.tv_JobFilterSalary).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_HighToLow).setBackground(null);
            ll_AllJobFilter.findViewById(R.id.tv_LowToHigh)
                    .setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.filterjob_textview_back));
        }

    }
}
