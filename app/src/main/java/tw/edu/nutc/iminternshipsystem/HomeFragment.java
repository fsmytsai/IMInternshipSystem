package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.AnnouncementView;
import me.grantland.widget.AutofitTextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends MySharedFragment {
    private MainActivity mainActivity;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView rv_AnnouncementList;
    private AnnouncementView announcementView;
    private AppBarLayout app_bar;
    private ImageView iv_ToTop;

    private boolean isFirstLoad = true;
    private boolean isLoading = true;
    private boolean isFinishLoad = false;

    private AnnouncementListAdapter announcementListAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        initView(view);
        Refresh();
        return view;
    }

    private void initView(View view) {
        app_bar = (AppBarLayout) view.findViewById(R.id.app_bar);
        rv_AnnouncementList = (RecyclerView) view.findViewById(R.id.rv_AnnouncementList);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_AnnouncementList);
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
                rv_AnnouncementList.smoothScrollToPosition(0);
            }
        });

        if (SharedService.identityView == null || SharedService.identityView.u_status == 3)
            view.findViewById(R.id.ib_Letter).setVisibility(View.GONE);
        else
            view.findViewById(R.id.ib_Letter).setVisibility(View.VISIBLE);
    }

    private void Refresh() {
        mSwipeLayout.setRefreshing(true);

        announcementView = new AnnouncementView();

        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            isFinishLoad = false;
            announcementListAdapter.mFooterId = 0;
            announcementListAdapter.notifyDataSetChanged();
        }
        GetAnnouncementList();
    }


    private void GetAnnouncementList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getAnnouncement?page=" + (announcementView.current_page + 1))
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
                            AnnouncementView tempView = new Gson().fromJson(ResMsg, AnnouncementView.class);

                            announcementView.current_page = tempView.current_page;
                            announcementView.data.addAll(tempView.data);

                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_AnnouncementList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                announcementListAdapter = new AnnouncementListAdapter();
                                rv_AnnouncementList.setAdapter(announcementListAdapter);
                            } else {
                                announcementListAdapter.notifyDataSetChanged();
                            }

                            if (tempView.current_page >= tempView.last_page) {
                                //最後一次載入
                                isFinishLoad = true;

                                announcementListAdapter.setFooterId(R.layout.footer);
                            }
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    public class AnnouncementListAdapter extends RecyclerView.Adapter<AnnouncementListAdapter.ViewHolder> {

        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        private View mFooterView;
        private int mFooterId;

        private boolean isAnimatingOut = false;

        public AnnouncementListAdapter() {
            rv_AnnouncementList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rv_AnnouncementList.getLayoutManager();
                        int visiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        if (visiblePosition == 0) {
                            app_bar.setExpanded(true, true);
                        }

                    }
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
            View view = LayoutInflater.from(context).inflate(R.layout.announcement_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (announcementView.data.size() == 0)
                    holder.tv_Footer.setText("還沒有任何公告!");
                else
                    holder.tv_Footer.setText("沒有更多公告囉!");
                return;
            }

            final AnnouncementView.Announcement announcement = announcementView.data.get(position);
            holder.tv_AnnCreateTime.setText(announcement.created_at);
            holder.atv_AnnTitle.setText(announcement.anTittle);

            holder.ll_AnnBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mainActivity, AnnDetailActivity.class);
                    intent.putExtra("Announcement", new Gson().toJson(announcement));
                    startActivity(intent);
                }
            });

            //避免重複請求
            if (position > announcementView.data.size() * 0.6 && !isFinishLoad && !isLoading) {
                isLoading = true;
                GetAnnouncementList();
            }
        }

        @Override
        public int getItemCount() {
            int NormalCount = announcementView.data.size();
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_AnnBlock;
            private TextView tv_AnnCreateTime;
            private AutofitTextView atv_AnnTitle;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }
                ll_AnnBlock = (LinearLayout) itemView.findViewById(R.id.ll_AnnBlock);
                tv_AnnCreateTime = (TextView) itemView.findViewById(R.id.tv_AnnCreateTime);
                atv_AnnTitle = (AutofitTextView) itemView.findViewById(R.id.atv_AnnTitle);
            }
        }
    }


}
