package tw.edu.nutc.iminternshipsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.net.URL;

import MyMethod.SharedService;
import ViewModel.AllJobView;
import ViewModel.CompanyView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompanyDetailActivity extends MySharedActivity {
    private SwipeRefreshLayout mSwipeLayout;

    private AllJobView allJobView;
    private CompanyView.Company company;
    private String c_account = "";

    private boolean isFirstLoad = true;
    private boolean isFirstLoadVR = true;
    private boolean isLoading = true;
    private boolean isFinishLoad = false;

    public RecyclerView rv_CompanyJobList;
    private CompanyJobListAdapter companyJobListAdapter;

    private ImageView iv_GoMail;

    private VrPanoramaView vpv_CompanyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_detail);

        initView();

        c_account = getIntent().getStringExtra("c_account");

        company = new Gson().fromJson(getIntent().getStringExtra("Company"), CompanyView.Company.class);
        SetCache((int) Runtime.getRuntime().maxMemory() / 10);
        Refresh();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar("廠商詳細資料", true);
        activity_Outer = findViewById(R.id.ll_ActivityOuter);
        rv_CompanyJobList = (RecyclerView) findViewById(R.id.rv_CompanyJobList);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.srl_CompanyJobList);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //檢查網路連線
                if (!SharedService.CheckNetWork(CompanyDetailActivity.this)) {
                    SharedService.ShowTextToast("請檢察網路連線", CompanyDetailActivity.this);
                    mSwipeLayout.setRefreshing(false);
                    return;
                }
                Refresh();
            }
        });
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(CompanyDetailActivity.this, R.color.colorTransparent));
        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeLayout.setDistanceToTriggerSync(400);// 设置手指在屏幕下拉多少距离会触发下拉刷新
        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);

        iv_GoMail = (ImageView) findViewById(R.id.iv_GoMail);

        if (SharedService.identityView.u_status != 0)
            iv_GoMail.setVisibility(View.GONE);
    }

    private void Refresh() {
        mSwipeLayout.setRefreshing(true);

        allJobView = new AllJobView();
        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            isFinishLoad = false;
            companyJobListAdapter.notifyDataSetChanged();
        }
        if (c_account == null)
            GetCompanyJobList();
        else
            GetCompanyDetail();
    }

    private void GetCompanyDetail() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getCompanyDetailsByAccount?c_account=" + c_account)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 停止刷新
                        mSwipeLayout.setRefreshing(false);
                        SharedService.ShowTextToast("請檢察網路連線", CompanyDetailActivity.this);
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
                            company = new Gson().fromJson(ResMsg, CompanyView.Company.class);
                            GetCompanyJobList();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, CompanyDetailActivity.this);
                        }
                    }
                });

            }

        });
    }

    private void GetCompanyJobList() {
        if (SharedService.identityView.u_status == 0)
            iv_GoMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CompanyDetailActivity.this, SendMailActivity.class);
                    intent.putExtra("c_account", company.c_account);
                    startActivity(intent);
                }
            });

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getJobOpeningByAccount?c_account=" + company.c_account + "&page=" + allJobView.current_page + 1)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 停止刷新
                        mSwipeLayout.setRefreshing(false);
                        SharedService.ShowTextToast("請檢察網路連線", CompanyDetailActivity.this);
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
                                rv_CompanyJobList.setLayoutManager(new LinearLayoutManager(CompanyDetailActivity.this, LinearLayoutManager.VERTICAL, false));
                                companyJobListAdapter = new CompanyJobListAdapter();
                                rv_CompanyJobList.setAdapter(companyJobListAdapter);
                            } else {
                                companyJobListAdapter.notifyDataSetChanged();
                            }

                            if (tempView.current_page >= tempView.last_page) {
                                //最後一次載入
                                isFinishLoad = true;
                            }
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, CompanyDetailActivity.this);
                        }
                    }
                });

            }

        });
    }

    public class CompanyJobListAdapter extends RecyclerView.Adapter<CompanyJobListAdapter.ViewHolder> {

        public final int TYPE_HEADER = 0;  //说明是带有Header的
        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        private View mHeaderView;
        private View mFooterView;

        private boolean isAnimatingOut = false;

        public CompanyJobListAdapter() {
            rv_CompanyJobList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                }

                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (SharedService.identityView.u_status == 0) {
                        if (dy < 0) {
                            if (!isAnimatingOut) {
                                iv_GoMail.setVisibility(View.VISIBLE);
                                ViewCompat.animate(iv_GoMail)
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
                            if (iv_GoMail.getVisibility() != View.GONE && !isAnimatingOut) {
                                ViewCompat.animate(iv_GoMail)
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

                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                //第一个item应该加载Header
                return TYPE_HEADER;
            }
            if (position == getItemCount() - 1) {
                //最后一个,应该加载Footer
                return TYPE_FOOTER;
            }
            return TYPE_NORMAL;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            if (viewType == TYPE_HEADER) {
                mHeaderView = LayoutInflater.from(context).inflate(R.layout.company_head, parent, false);
                return new ViewHolder(mHeaderView);
            }

            if (viewType == TYPE_FOOTER) {
                mFooterView = LayoutInflater.from(context).inflate(R.layout.footer, parent, false);
                return new ViewHolder(mFooterView);
            }


            View view = LayoutInflater.from(context).inflate(R.layout.alljob_detailblock, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_HEADER) {
                if (company.profilePic != null) {
                    holder.iv_CompanyImage.setTag(company.profilePic);
                    showImage(holder.iv_CompanyImage, company.profilePic, true, null);
                } else {
                    holder.iv_CompanyImage.setTag("");
                    holder.iv_CompanyImage.setImageResource(R.drawable.defaultmimg);
                }

                holder.tv_CompanyName.setText(company.c_name);
                holder.tv_CompanyType.setText(company.ctypes != null ? company.ctypes : "尚未填寫");
                holder.tv_CompanyAddress.setText(company.caddress != null ? company.caddress : "尚未填寫");
                holder.tv_CompanyFax.setText(company.cfax != null ? company.cfax : "尚未填寫");
                holder.tv_CompanyEmpolyeeNum.setText(company.cempolyee_num + "");
                holder.tv_CompanyTel.setText(company.tel);
                holder.tv_CompanyIntro.setText(company.cintroduction != null ? company.cintroduction : "簡介尚未填寫");

                if (company.introductionPic != null) {
                    if (isFirstLoadVR) {
                        isFirstLoadVR = false;

                        vpv_CompanyImage.setInfoButtonEnabled(false);
                        mLoadPanoramaImageTask = new LoadPanoramaImageTask(vpv_CompanyImage, company.introductionPic);
                        mLoadPanoramaImageTask.execute();
                    }
                } else {
                    vpv_CompanyImage.setVisibility(View.GONE);
                }

                return;
            }

            if (getItemViewType(position) == TYPE_FOOTER) {
                if (allJobView.data.size() == 0)
                    holder.tv_Footer.setText("該廠商還沒有任何職缺!");
                else
                    holder.tv_Footer.setText("沒有更多職缺囉!");
                return;
            }

            final int Position = position - 1;

            final AllJobView.AllJob allJob = allJobView.data.get(Position);


            holder.iv_CompanyImage.setVisibility(View.GONE);
            holder.tv_CompanyName.setVisibility(View.GONE);

            if (allJob.jtypes == 0)
                holder.tv_JobType.setText("暑期實習");
            else if (allJob.jtypes == 1)
                holder.tv_JobType.setText("學期實習");
            else if (allJob.jtypes == 2)
                holder.tv_JobType.setText("工讀");
            else if (allJob.jtypes == 3)
                holder.tv_JobType.setText("正職");

            holder.tv_JobDuty.setText(allJob.jduties);
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

            holder.bt_CompanyDetail.setVisibility(View.GONE);

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
                    holder.bt_SubmitResume.setBackgroundColor(ContextCompat.getColor(CompanyDetailActivity.this, android.R.color.black));
                    holder.bt_SubmitResume.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SubmitResume(Position);
                        }
                    });
                }
            }

            //避免重複請求
            if (position > allJobView.data.size() * 0.6 && !isFinishLoad && !isLoading) {
                isLoading = true;
                GetCompanyJobList();
            }
        }

        @Override
        public int getItemCount() {
            return allJobView.data.size() + 2;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv_CompanyImage;
            private TextView tv_CompanyName;
            private TextView tv_CompanyType;
            private TextView tv_CompanyAddress;
            private TextView tv_CompanyFax;
            private TextView tv_CompanyEmpolyeeNum;
            private TextView tv_CompanyTel;
            private TextView tv_CompanyIntro;

            private TextView tv_JobType;
            private TextView tv_JobDuty;
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

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                iv_CompanyImage = (ImageView) itemView.findViewById(R.id.iv_CompanyImage);
                tv_CompanyName = (TextView) itemView.findViewById(R.id.tv_CompanyName);
                if (itemView == mHeaderView) {
                    tv_CompanyType = (TextView) itemView.findViewById(R.id.tv_CompanyType);
                    tv_CompanyAddress = (TextView) itemView.findViewById(R.id.tv_CompanyAddress);
                    tv_CompanyFax = (TextView) itemView.findViewById(R.id.tv_CompanyFax);
                    tv_CompanyEmpolyeeNum = (TextView) itemView.findViewById(R.id.tv_CompanyEmpolyeeNum);
                    tv_CompanyTel = (TextView) itemView.findViewById(R.id.tv_CompanyTel);
                    tv_CompanyIntro = (TextView) itemView.findViewById(R.id.tv_CompanyIntro);
                    vpv_CompanyImage = (VrPanoramaView) itemView.findViewById(R.id.vpv_CompanyImage);

                    return;
                }

                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                tv_JobType = (TextView) itemView.findViewById(R.id.tv_JobType);
                tv_JobDuty = (TextView) itemView.findViewById(R.id.tv_JobDuty);
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

    @Override
    protected void onPause() {
        if (vpv_CompanyImage != null)
            vpv_CompanyImage.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vpv_CompanyImage != null)
            vpv_CompanyImage.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        if (vpv_CompanyImage != null)
            vpv_CompanyImage.shutdown();
        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (mLoadPanoramaImageTask != null) {
            mLoadPanoramaImageTask.cancel(true);
        }
        super.onDestroy();
    }

    private LoadPanoramaImageTask mLoadPanoramaImageTask;

    private class LoadPanoramaImageTask extends AsyncTask<Void, Void, Bitmap> {
        private VrPanoramaView mVrPanoramaView;
        private String introductionPic;

        public LoadPanoramaImageTask(VrPanoramaView mVrPanoramaView, String IntroductionPic) {
            this.mVrPanoramaView = mVrPanoramaView;
            this.introductionPic = IntroductionPic;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                //加载assets目录下的全景图片

                URL url = new URL(getString(R.string.BackEndPath) + "storage/user-upload/" + introductionPic);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return image;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            VrPanoramaView.Options options = new VrPanoramaView.Options();
            //图片类型为立体图像
            options.inputType = VrPanoramaView.Options.TYPE_MONO;
            mVrPanoramaView.loadImageFromBitmap(bitmap, options);
        }
    }

    private void SubmitResume(final int Position) {
        new AlertDialog.Builder(CompanyDetailActivity.this)
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SharedService.ShowTextToast("請檢察網路連線", CompanyDetailActivity.this);
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
                                            SharedService.ShowTextToast("投遞履歷成功", CompanyDetailActivity.this);
                                            allJobView.data.get(Position).jResume_submitted = true;
                                            companyJobListAdapter.notifyDataSetChanged();
                                        } else {
                                            SharedService.HandleError(StatusCode, ResMsg, CompanyDetailActivity.this);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).show();
    }
}
