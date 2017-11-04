package tw.edu.nutc.iminternshipsystem;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.InternCourseView;
import ViewModel.JournalView;
import me.grantland.widget.AutofitTextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class InternCourseListFragment extends MySharedFragment {

    private static final int EDIT_CODE = 90;
    private MainActivity mainActivity;

    private InternCourseView internCourseView;
    private JournalView[] journalViewArray;

    private boolean isFirstLoad = true;

    private SwipeRefreshLayout mSwipeLayout;
    public RecyclerView rv_InternCourseList;
    private InternCourseListAdapter internCourseListAdapter;

    public InternCourseListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intern_course_list, container, false);
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
        rv_InternCourseList = (RecyclerView) view.findViewById(R.id.rv_InternCourseList);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_InternCourseList);
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
    }

    private void Refresh() {
        mSwipeLayout.setRefreshing(true);

        internCourseView = new InternCourseView();

        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            internCourseListAdapter.mFooterId = 0;
            internCourseListAdapter.notifyDataSetChanged();
        }
        GetInternCourseList();
    }

    private void GetInternCourseList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/studentGetInternList")
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

                        if (StatusCode == 200) {
                            internCourseView = new Gson().fromJson(ResMsg, InternCourseView.class);

                            journalViewArray = new JournalView[internCourseView.intern_list.size()];
                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_InternCourseList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                internCourseListAdapter = new InternCourseListAdapter();
                                rv_InternCourseList.setAdapter(internCourseListAdapter);
                            } else {
                                internCourseListAdapter.notifyDataSetChanged();
                            }

                            internCourseListAdapter.setFooterId(R.layout.footer);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    public class InternCourseListAdapter extends RecyclerView.Adapter<InternCourseListAdapter.ViewHolder> {

        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        private View mFooterView;
        private int mFooterId;

        public void setFooterId(int FooterId) {
            mFooterId = FooterId;
            notifyItemInserted(getItemCount() - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1 && mFooterId != 0) {
                //最后一个,应该加载Footer
                return TYPE_FOOTER;
            }
            return TYPE_NORMAL;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            if (mFooterId != 0 && viewType == TYPE_FOOTER) {
                mFooterView = LayoutInflater.from(context).inflate(mFooterId, parent, false);
                return new ViewHolder(mFooterView);
            }
            View view = LayoutInflater.from(context).inflate(R.layout.interncourse_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (internCourseView.intern_list.size() == 0)
                    holder.tv_Footer.setText("您沒有加入任何實習課程!");
                else
                    holder.tv_Footer.setText("沒有更多實習課程囉!");
                return;
            }

            if (internCourseView.intern_list.get(position).profilePic != null) {
                holder.iv_CompanyImage.setImageDrawable(null);
                holder.iv_CompanyImage.setTag(internCourseView.intern_list.get(position).profilePic);
                showImage(holder.iv_CompanyImage, internCourseView.intern_list.get(position).profilePic, true, null);
            } else {
                holder.iv_CompanyImage.setTag("");
                holder.iv_CompanyImage.setImageResource(R.drawable.defaultmimg);
            }
            holder.atv_CompanyName.setText(internCourseView.intern_list.get(position).com_name);
            holder.atv_CourseName.setText(internCourseView.intern_list.get(position).courseName);
            holder.iv_UpAndDown.setVisibility(View.VISIBLE);
            holder.pb_Loading.setVisibility(View.GONE);
            holder.ll_JournalList.removeAllViews();
            if (internCourseView.intern_list.get(position).IsOpen) {
                holder.ll_JournalList.setVisibility(View.VISIBLE);
                holder.iv_UpAndDown.setImageResource(R.drawable.up);

                LinearLayout ll_IPBlock = GetJournalBlock(-2, journalViewArray[position].internProposal.IPStart != null, true);
                ll_IPBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mainActivity, StudentReviewActivity.class);
                        intent.putExtra("Review", new Gson().toJson(journalViewArray[position].reviews));
                        startActivityForResult(intent, EDIT_CODE);
                    }
                });
                holder.ll_JournalList.addView(ll_IPBlock);

                for (final JournalView.Journal journal : journalViewArray[position].journalList) {
                    LinearLayout ll_JournalBlock = GetJournalBlock(journal.journalOrder + 1, journal.journalDetail_1 != null, journal.grade_teacher != 0);
                    ll_JournalBlock.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mainActivity, JournalActivity.class);
                            intent.putExtra("Journal", new Gson().toJson(journal));
                            startActivityForResult(intent, EDIT_CODE);
                        }
                    });
                    holder.ll_JournalList.addView(ll_JournalBlock);
                }

                LinearLayout ll_JournalBlock = GetJournalBlock(-1, journalViewArray[position].reviews.reContent != null, journalViewArray[position].reviews.reRead);
                ll_JournalBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mainActivity, StudentReviewActivity.class);
                        intent.putExtra("Review", new Gson().toJson(journalViewArray[position].reviews));
                        startActivityForResult(intent, EDIT_CODE);
                    }
                });
                holder.ll_JournalList.addView(ll_JournalBlock);
            } else {
                holder.ll_JournalList.setVisibility(View.GONE);
                holder.iv_UpAndDown.setImageResource(R.drawable.down);
            }

            holder.ll_InternCourseBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.ll_JournalList.getVisibility() == View.VISIBLE) {
                        internCourseView.intern_list.get(position).IsOpen = false;
                        holder.iv_UpAndDown.setImageResource(R.drawable.down);
                        holder.ll_JournalList.animate()
                                .translationY(-holder.ll_JournalList.getHeight())
                                .alpha(0.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        holder.ll_JournalList.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        internCourseView.intern_list.get(position).IsOpen = true;
                        if (internCourseView.intern_list.get(position).IsFill) {
                            holder.ll_JournalList.removeAllViews();
                            LinearLayout ll_IPBlock = GetJournalBlock(-2, journalViewArray[position].internProposal.IPStart != null, true);
                            ll_IPBlock.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mainActivity, StudentReviewActivity.class);
                                    intent.putExtra("Review", new Gson().toJson(journalViewArray[position].reviews));
                                    startActivityForResult(intent, EDIT_CODE);
                                }
                            });
                            holder.ll_JournalList.addView(ll_IPBlock);

                            for (final JournalView.Journal journal : journalViewArray[position].journalList) {
                                LinearLayout ll_JournalBlock = GetJournalBlock(journal.journalOrder + 1, journal.journalDetail_1 != null, journal.grade_teacher != 0);
                                ll_JournalBlock.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(mainActivity, JournalActivity.class);
                                        intent.putExtra("Journal", new Gson().toJson(journal));
                                        startActivityForResult(intent, EDIT_CODE);
                                    }
                                });
                                holder.ll_JournalList.addView(ll_JournalBlock);
                            }

                            LinearLayout ll_JournalBlock = GetJournalBlock(-1, journalViewArray[position].reviews.reContent != null, journalViewArray[position].reviews.reRead);
                            ll_JournalBlock.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mainActivity, StudentReviewActivity.class);
                                    intent.putExtra("Review", new Gson().toJson(journalViewArray[position].reviews));
                                    startActivityForResult(intent, EDIT_CODE);
                                }
                            });
                            holder.ll_JournalList.addView(ll_JournalBlock);

                            holder.iv_UpAndDown.setImageResource(R.drawable.up);
                            holder.ll_JournalList.setVisibility(View.VISIBLE);
                            holder.ll_JournalList.animate()
                                    .translationY(0)
                                    .alpha(1.0f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            holder.ll_JournalList.setVisibility(View.VISIBLE);
                                        }
                                    });
                        } else {
                            holder.iv_UpAndDown.setVisibility(View.GONE);
                            holder.pb_Loading.setVisibility(View.VISIBLE);
                            GetJournalList(position, holder.ll_JournalList, holder.iv_UpAndDown, holder.pb_Loading);
                        }
                    }
                }
            });


        }

        @Override
        public int getItemCount() {
            int NormalCount = internCourseView.intern_list.size();
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_InternCourseBlock;
            private ImageView iv_CompanyImage;
            private AutofitTextView atv_CompanyName;
            private AutofitTextView atv_CourseName;
            private ImageView iv_UpAndDown;
            private ProgressBar pb_Loading;
            private LinearLayout ll_JournalList;
            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_InternCourseBlock = (LinearLayout) itemView.findViewById(R.id.ll_InternCourseBlock);
                iv_CompanyImage = (ImageView) itemView.findViewById(R.id.iv_CompanyImage);
                atv_CompanyName = (AutofitTextView) itemView.findViewById(R.id.atv_CompanyName);
                atv_CourseName = (AutofitTextView) itemView.findViewById(R.id.atv_CourseName);
                iv_UpAndDown = (ImageView) itemView.findViewById(R.id.iv_UpAndDown);
                pb_Loading = (ProgressBar) itemView.findViewById(R.id.pb_Loading);
                ll_JournalList = (LinearLayout) itemView.findViewById(R.id.ll_JournalList);
            }
        }
    }

    private void GetJournalList(final int Position, final LinearLayout ll_JournalList, final ImageView iv_UpAndDown, final ProgressBar pb_Loading) {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/studentGetJournalList?SCid=" + internCourseView.intern_list.get(Position).SCid)
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
                            journalViewArray[Position] = new Gson().fromJson(ResMsg, JournalView.class);

                            LinearLayout ll_IPBlock = GetJournalBlock(-2, journalViewArray[Position].internProposal.IPStart != null, true);
                            ll_IPBlock.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mainActivity, MyWebViewActivity.class);
                                    intent.putExtra("URL", "");
                                    startActivityForResult(intent, EDIT_CODE);
                                }
                            });
                            ll_JournalList.addView(ll_IPBlock);

                            for (final JournalView.Journal journal : journalViewArray[Position].journalList) {
                                LinearLayout ll_JournalBlock = GetJournalBlock(journal.journalOrder + 1, journal.journalDetail_1 != null, journal.grade_teacher != 0);
                                ll_JournalBlock.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(mainActivity, JournalActivity.class);
                                        intent.putExtra("Journal", new Gson().toJson(journal));
                                        startActivityForResult(intent, EDIT_CODE);
                                    }
                                });
                                ll_JournalList.addView(ll_JournalBlock);
                            }

                            LinearLayout ll_JournalBlock = GetJournalBlock(-1, !journalViewArray[Position].reviews.reContent.equals(""), journalViewArray[Position].reviews.reRead);
                            ll_JournalBlock.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mainActivity, StudentReviewActivity.class);
                                    intent.putExtra("Review", new Gson().toJson(journalViewArray[Position].reviews));
                                    startActivityForResult(intent, EDIT_CODE);
                                }
                            });
                            ll_JournalList.addView(ll_JournalBlock);

                            internCourseView.intern_list.get(Position).IsFill = true;
                            iv_UpAndDown.setVisibility(View.VISIBLE);
                            pb_Loading.setVisibility(View.GONE);
                            SharedService.ShowAndHideBlock(ll_JournalList, iv_UpAndDown);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    private LinearLayout GetJournalBlock(int JournalNum, boolean IsSend, boolean IsCheck) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, (int) SharedService.DipToPixels(mainActivity, 10), 0, 0);
        LinearLayout ll_JournalBlock = new LinearLayout(mainActivity);
        ll_JournalBlock.setOrientation(LinearLayout.HORIZONTAL);
        int pad = (int) SharedService.DipToPixels(mainActivity, 5);
        ll_JournalBlock.setPadding(pad, 0, pad, 0);
        ll_JournalBlock.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.weight = 1;
        TextView tv_JournalNum = new TextView(mainActivity);
        if (JournalNum == -1)
            tv_JournalNum.setText("實習總心得");
        else if (JournalNum == -2)
            tv_JournalNum.setText("實習計劃書");
        else
            tv_JournalNum.setText("第" + JournalNum + "份週誌");

        tv_JournalNum.setTextColor(ContextCompat.getColor(mainActivity, android.R.color.black));
        tv_JournalNum.setTextSize(22f);
        tv_JournalNum.setLayoutParams(layoutParams2);

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams((int) SharedService.DipToPixels(mainActivity, 40), (int) SharedService.DipToPixels(mainActivity, 40));

        ImageView iv_JournalSend = new ImageView(mainActivity);
        if (IsSend)
            iv_JournalSend.setImageResource(R.drawable.sent);
        else
            iv_JournalSend.setImageResource(R.drawable.send);
        iv_JournalSend.setLayoutParams(layoutParams1);

        ImageView iv_JournalCheck = new ImageView(mainActivity);

        ll_JournalBlock.addView(tv_JournalNum);
        ll_JournalBlock.addView(iv_JournalSend);

        if (JournalNum != -2) {
            if (IsCheck)
                iv_JournalCheck.setImageResource(R.drawable.checked);
            else
                iv_JournalCheck.setImageResource(R.drawable.check);
            iv_JournalCheck.setLayoutParams(layoutParams1);
            ll_JournalBlock.addView(iv_JournalCheck);
        }

        return ll_JournalBlock;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_CODE && resultCode == Activity.RESULT_OK) {
            Refresh();
        }
    }
}
