package tw.edu.nutc.iminternshipsystem;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tsaiweb.bottompopmenu.BottomMenuFragment;
import com.tsaiweb.bottompopmenu.MenuItem;
import com.tsaiweb.bottompopmenu.MenuItemOnClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import MyMethod.IMyImgCallBack;
import MyMethod.SharedService;
import ViewModel.VisitCourseView;
import me.grantland.widget.AutofitTextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class StudentManagementFragment extends MySharedFragment {

    private MainActivity mainActivity;
    private SwipeRefreshLayout mSwipeLayout;

    private VisitCourseView visitCourseView;

    private boolean isFirstLoad = true;

    public RecyclerView rv_VisitCourseList;
    private VisitCourseListAdapter visitCourseListAdapter;

    private boolean isLoadingImages = false;

    public StudentManagementFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_management, container, false);
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
        rv_VisitCourseList = (RecyclerView) view.findViewById(R.id.rv_VisitCourseList);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_VisitCourseList);
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

        visitCourseView = new VisitCourseView();

        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            visitCourseListAdapter.mFooterId = 0;
            visitCourseListAdapter.notifyDataSetChanged();
        }
        GetCourseList();
    }


    private void GetCourseList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getCourseList")
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
                            visitCourseView = new Gson().fromJson(ResMsg, VisitCourseView.class);
                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_VisitCourseList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                visitCourseListAdapter = new VisitCourseListAdapter();
                                rv_VisitCourseList.setAdapter(visitCourseListAdapter);
                            } else {
                                visitCourseListAdapter.notifyDataSetChanged();
                            }

                            visitCourseListAdapter.setFooterId(R.layout.footer);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }


    public class VisitCourseListAdapter extends RecyclerView.Adapter<VisitCourseListAdapter.ViewHolder> {

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
            View view = LayoutInflater.from(context).inflate(R.layout.visitcourse_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (visitCourseView.CourseList.size() == 0)
                    holder.tv_Footer.setText("您沒有負責任何實習課程!");
                else
                    holder.tv_Footer.setText("沒有更多實習課程囉!");
                return;
            }

            holder.atv_CourseName.setText(visitCourseView.CourseList.get(position).courseName);
            holder.iv_UpAndDown.setVisibility(View.VISIBLE);
            holder.pb_Loading.setVisibility(View.GONE);
            holder.ll_StudentListBlock.removeAllViews();
            if (visitCourseView.CourseList.get(position).IsOpen) {
                holder.ll_StudentListBlock.setVisibility(View.VISIBLE);
                holder.iv_UpAndDown.setImageResource(R.drawable.up);
                FillStudentListBlock(position,holder.ll_StudentListBlock);
//                for (final VisitCourseView.Student student : visitCourseView.CourseList.get(position).studentList) {
//                    LinearLayout ll_StudentBlock = GetStudentBlock(student.stuName, student.profilePic, null);
//                    ll_StudentBlock.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent intent = new Intent(mainActivity, VisitRecordActivity.class);
//                            intent.putExtra("SCid", student.SCid);
//                            startActivity(intent);
//                        }
//                    });
//                    holder.ll_StudentListBlock.addView(ll_StudentBlock);
//                }
            } else {
                holder.ll_StudentListBlock.setVisibility(View.GONE);
                holder.iv_UpAndDown.setImageResource(R.drawable.down);
            }
            holder.ll_VisitCourseBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (visitCourseView.CourseList.get(position).IsOpen) {
                        visitCourseView.CourseList.get(position).IsOpen = false;
                        holder.iv_UpAndDown.setImageResource(R.drawable.down);
                        holder.ll_StudentListBlock.animate()
                                .translationY(-holder.ll_StudentListBlock.getHeight())
                                .alpha(0.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        holder.ll_StudentListBlock.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        visitCourseView.CourseList.get(position).IsOpen = true;
                        if (visitCourseView.CourseList.get(position).IsFill) {
                            holder.ll_StudentListBlock.removeAllViews();
                            FillStudentListBlock(position,holder.ll_StudentListBlock);
//                            for (final VisitCourseView.Student student : visitCourseView.CourseList.get(position).studentList) {
//                                LinearLayout ll_StudentBlock = GetStudentBlock(student.stuName, student.profilePic, null);
//                                ll_StudentBlock.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Intent intent = new Intent(mainActivity, VisitRecordActivity.class);
//                                        intent.putExtra("SCid", student.SCid);
//                                        startActivity(intent);
//                                    }
//                                });
//                                holder.ll_StudentListBlock.addView(ll_StudentBlock);
//                            }
                            holder.iv_UpAndDown.setImageResource(R.drawable.up);
                            holder.ll_StudentListBlock.setVisibility(View.VISIBLE);
                            holder.ll_StudentListBlock.animate()
                                    .translationY(0)
                                    .alpha(1.0f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            holder.ll_StudentListBlock.setVisibility(View.VISIBLE);
                                        }
                                    });
                        } else {
                            if (!isLoadingImages) {
                                isLoadingImages = true;
                                holder.iv_UpAndDown.setVisibility(View.GONE);
                                holder.pb_Loading.setVisibility(View.VISIBLE);
                                FirstFillStudentListBlock(position, holder.ll_StudentListBlock, holder.iv_UpAndDown, holder.pb_Loading);
                            } else {
                                SharedService.ShowTextToast("請稍後再試", mainActivity);
                            }

                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            int NormalCount = visitCourseView.CourseList.size();
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_VisitCourseBlock;
            private AutofitTextView atv_CourseName;
            private ImageView iv_UpAndDown;
            private ProgressBar pb_Loading;
            private LinearLayout ll_StudentListBlock;
            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_VisitCourseBlock = (LinearLayout) itemView.findViewById(R.id.ll_VisitCourseBlock);
                atv_CourseName = (AutofitTextView) itemView.findViewById(R.id.atv_CourseName);
                iv_UpAndDown = (ImageView) itemView.findViewById(R.id.iv_UpAndDown);
                pb_Loading = (ProgressBar) itemView.findViewById(R.id.pb_Loading);
                ll_StudentListBlock = (LinearLayout) itemView.findViewById(R.id.ll_StudentListBlock);
            }
        }
    }

    private void FillStudentListBlock(final int Position, final LinearLayout ll_StudentListBlock){
        for (final VisitCourseView.Student student : visitCourseView.CourseList.get(Position).studentList) {
            LinearLayout ll_StudentBlock = GetStudentBlock(student.stuName, student.profilePic, null);
            ll_StudentBlock.setOnClickListener(new ClickStudentBlock(student));
            ll_StudentListBlock.addView(ll_StudentBlock);
        }
    }

    private void FirstFillStudentListBlock(final int Position, final LinearLayout ll_StudentListBlock, final ImageView iv_UpAndDown, final ProgressBar pb_Loading) {
        int ImageCount = 0;
        for (VisitCourseView.Student student : visitCourseView.CourseList.get(Position).studentList) {
            if (student.profilePic != null)
                ImageCount++;
        }
        MyImgCallback myImgCallback = new MyImgCallback(ImageCount, Position, ll_StudentListBlock, iv_UpAndDown, pb_Loading);
        for (final VisitCourseView.Student student : visitCourseView.CourseList.get(Position).studentList) {
            LinearLayout ll_StudentBlock = GetStudentBlock(student.stuName, student.profilePic, myImgCallback);
            ll_StudentBlock.setOnClickListener(new ClickStudentBlock(student));

            ll_StudentListBlock.addView(ll_StudentBlock);
        }
    }

    private LinearLayout GetStudentBlock(String StudentName, String profilePic, MyImgCallback myImgCallback) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, (int) SharedService.DipToPixels(mainActivity, 10), 0, 0);
        LinearLayout ll_StudentBlock = new LinearLayout(mainActivity);
        ll_StudentBlock.setOrientation(LinearLayout.HORIZONTAL);
        int pad = (int) SharedService.DipToPixels(mainActivity, 5);
        ll_StudentBlock.setPadding(pad, 0, pad, 0);
        ll_StudentBlock.setLayoutParams(layoutParams);
        ll_StudentBlock.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams((int) SharedService.DipToPixels(mainActivity, 40), (int) SharedService.DipToPixels(mainActivity, 40));
        layoutParams1.setMargins((int) SharedService.DipToPixels(mainActivity, 50), 0, 0, 0);
        ImageView iv_MImg = new ImageView(mainActivity);
        if (profilePic != null) {
            iv_MImg.setTag(profilePic);
            showImage(iv_MImg, profilePic, true, myImgCallback);
        } else
            iv_MImg.setImageResource(R.drawable.defaultmimg);
        iv_MImg.setLayoutParams(layoutParams1);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins((int) SharedService.DipToPixels(mainActivity, 40), 0, 0, 0);
        TextView tv_StudentName = new TextView(mainActivity);
        tv_StudentName.setText(StudentName);
        tv_StudentName.setTextColor(ContextCompat.getColor(mainActivity, android.R.color.black));
        tv_StudentName.setTextSize(22f);
        tv_StudentName.setLayoutParams(layoutParams2);

        ll_StudentBlock.addView(iv_MImg);
        ll_StudentBlock.addView(tv_StudentName);

        return ll_StudentBlock;
    }

    private class MyImgCallback implements IMyImgCallBack {
        public MyImgCallback(final int imageCount, final int Position, final LinearLayout ll_StudentListBlock, final ImageView iv_UpAndDown, final ProgressBar pb_Loading) {
            this.imageCount = imageCount;
            this.position = Position;
            this.ll_StudentListBlock = ll_StudentListBlock;
            this.iv_UpAndDown = iv_UpAndDown;
            this.pb_Loading = pb_Loading;
        }

        private int imageCount;
        private int runTimes = 0;
        private int position;
        private LinearLayout ll_StudentListBlock;
        private ImageView iv_UpAndDown;
        private ProgressBar pb_Loading;

        @Override
        public void CallBack() {
            runTimes++;
            if (runTimes == imageCount) {
                visitCourseView.CourseList.get(position).IsFill = true;
                iv_UpAndDown.setVisibility(View.VISIBLE);
                pb_Loading.setVisibility(View.GONE);
                SharedService.ShowAndHideBlock(ll_StudentListBlock, iv_UpAndDown);
                isLoadingImages = false;
            }

        }
    }

    private class ClickStudentBlock implements View.OnClickListener {
        private VisitCourseView.Student student;

        public ClickStudentBlock(VisitCourseView.Student student){
            this.student = student;
        }

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
                    intent.putExtra("Sid", student.sid);
                    intent.putExtra("StudentName", student.stuName);
                    startActivity(intent);
                }
            });

            MenuItem menuItem2 = new MenuItem();
            menuItem2.setText("週誌管理");
            menuItem2.setStyle(MenuItem.MenuItemStyle.COMMON);
            menuItem2.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                @Override
                public void onClickMenuItem(View v, MenuItem menuItem) {
                    Intent intent = new Intent(mainActivity, MyWebViewActivity.class);
                    intent.putExtra("URL", getString(R.string.FrontEndPath) + "weekly&SCId=" + student.SCid + "&Token=" + SharedService.token);
                    startActivity(intent);
                }
            });

            MenuItem menuItem3 = new MenuItem();
            menuItem3.setText("成績管理");
            menuItem3.setStyle(MenuItem.MenuItemStyle.COMMON);
            menuItem3.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                @Override
                public void onClickMenuItem(View v, MenuItem menuItem) {
                    Intent intent = new Intent(mainActivity, MyWebViewActivity.class);
                    intent.putExtra("URL", getString(R.string.FrontEndPath) + "grade_tea&SCId=" + student.SCid + "&Token=" + SharedService.token);
                    startActivity(intent);
                }
            });

            MenuItem menuItem4 = new MenuItem();
            menuItem4.setText("訪視管理");
            menuItem4.setStyle(MenuItem.MenuItemStyle.COMMON);
            menuItem4.setMenuItemOnClickListener(new MenuItemOnClickListener(bottomMenuFragment, menuItem1) {
                @Override
                public void onClickMenuItem(View v, MenuItem menuItem) {
                    Intent intent = new Intent(mainActivity, VisitRecordActivity.class);
                    intent.putExtra("SCid", student.SCid);
                    startActivity(intent);
                }
            });

            menuItemList.add(menuItem1);
            menuItemList.add(menuItem2);
            menuItemList.add(menuItem3);
            menuItemList.add(menuItem4);

            bottomMenuFragment.setMenuItems(menuItemList);

            bottomMenuFragment.show(getFragmentManager(), "BottomMenuFragment");
        }
    }
}
