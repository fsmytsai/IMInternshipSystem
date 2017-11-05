package tw.edu.nutc.iminternshipsystem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import MyMethod.MyRecyclerView;
import MyMethod.SharedService;
import ViewModel.IdentityView;
import ViewModel.MailView;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends MySharedActivity {

    private static final int READMAIL_CODE = 1110;
    public DrawerLayout drawer;
    private MailView mailView;
    private String[] mailTypes = {"收件匣", "送件匣", "收藏匣", "垃圾桶"};
    private Spinner sp_MailType;
    public int mailType = 0;

    private SwipeRefreshLayout mSwipeLayout;

    private boolean isFirstLoad = true;
    private boolean isLoading = true;
    private boolean isFinishLoad = false;
    private boolean IsFinishing = false;

    public MyRecyclerView rv_MailList;
    private MailListAdapter mailListAdapter;
    public boolean isDeleting;
    private View nav_end;
    private ImageView iv_DeleteMail;
    private CheckBox cb_DeleteAll;
    private int deletedCount;

    private boolean isRecovering = false;

    private List<String> contentFragmentList = new ArrayList<>();

    private int nowPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);

        initViews();
        //歡迎Loading畫面
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.MainFrameLayout, new SplashScreenFragment(), "SplashScreenFragment")
                .commit();

        CheckLogon();
    }

    private void initViews() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        activity_Outer = drawer;
//        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawer.isDrawerOpen(GravityCompat.END)) {

                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    public void CheckLogon() {

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/findUserDetailsByToken")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MainActivity.this);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.MainFrameLayout, new ReLinkFragment(), "ReLinkFragment")
                                .commit();
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
                            SharedService.identityView = new Gson().fromJson(ResMsg, IdentityView.class);

                            if (SharedService.identityView.u_status == 3) {
                                //系辦
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("系辦僅提供網頁版，是否開啟?")
                                        .setNeutralButton("否", null)
                                        .setPositiveButton("開啟", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Uri uri = Uri.parse("http://tsaiweb.southeastasia.cloudapp.azure.com/aa9453aa/#Page=home_Log&Token=" + SharedService.token);
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            }
                                        })
                                        .show();
                            }

                            SetColor();
                            contentFragmentList = new ArrayList<>();
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.MainFrameLayout, new HomeFragment(), "HomeFragment")
                                    .commit();

                            if (nav_end == null && SharedService.identityView.u_status != 3)
                                initViewsAfterLogin();

                        } else if (StatusCode == 401) {
                            SharedService.identityView = null;
                            SetColor();
                            if (isLogout) {
                                isLogout = false;
                                isFirstLoad = true;
                                isFinishLoad = false;
                                SharedService.ShowTextToast("登出成功", MainActivity.this);
                                contentFragmentList.clear();
                                drawer.removeView(nav_end);
                                nav_end = null;
                            }
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.MainFrameLayout, new HomeFragment(), "HomeFragment")
                                    .commit();
                        } else {
                            SharedService.ShowTextToast("ERROR:" + StatusCode, MainActivity.this);
                        }
                    }
                });
            }
        });
    }

    private void initViewsAfterLogin() {

        nav_end = LayoutInflater.from(this).inflate(R.layout.nav_end, drawer, false);

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        NavigationView.LayoutParams layoutParams = new NavigationView.LayoutParams(NavigationView.LayoutParams.MATCH_PARENT, NavigationView.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, statusBarHeight, 0, 0);
        nav_end.findViewById(R.id.ll_MailList).setLayoutParams(layoutParams);

        cb_DeleteAll = (CheckBox) nav_end.findViewById(R.id.cb_DeleteAll);

        cb_DeleteAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (MailView.Mail mail : mailView.data) {
                    mail.isDelete = isChecked;
                }
                mailListAdapter.notifyDataSetChanged();
            }
        });


        sp_MailType = (Spinner) nav_end.findViewById(R.id.sp_MailType);
        ArrayAdapter<CharSequence> MailTypesArrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, mailTypes);
        MailTypesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_MailType.setAdapter(MailTypesArrayAdapter);
        sp_MailType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != mailType) {
                    mailType = position;
                    isDeleting = false;
                    Refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        iv_DeleteMail = (ImageView) nav_end.findViewById(R.id.iv_DeleteMail);
        iv_DeleteMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rv_MailList.state == 1) {
                    rv_MailList.scroller.startScroll(rv_MailList.ll_MailBlockAll.getScrollX(), 0, -rv_MailList.deleteWidth, 0, 100);//弹性滑动
                    rv_MailList.invalidate();
                    rv_MailList.state = 0;
                }
                if (isDeleting) {
                    int Count = 0;
                    for (MailView.Mail mail : mailView.data) {
                        if (mail.isDelete)
                            Count++;
                    }
                    if (Count > 0) {
                        final int DeleteCount = Count;
                        if (mailType == 0 || mailType == 2) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("確定要刪除這" + Count + "封信嗎?")
                                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isDeleting = false;
                                            if (cb_DeleteAll.isChecked())
                                                cb_DeleteAll.setChecked(false);
                                            else {
                                                for (MailView.Mail mail : mailView.data) {
                                                    mail.isDelete = false;
                                                }
                                                mailListAdapter.notifyDataSetChanged();
                                            }
                                            cb_DeleteAll.setVisibility(View.GONE);
                                        }
                                    })
                                    .setNegativeButton("丟垃圾桶", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DeleteMail(mailView.data.size(), false, DeleteCount);
                                        }
                                    })
                                    .setPositiveButton("永久刪除", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DeleteMail(mailView.data.size(), true, DeleteCount);
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("確定要刪除這" + Count + "封信嗎?")
                                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isDeleting = false;
                                            if (cb_DeleteAll.isChecked())
                                                cb_DeleteAll.setChecked(false);
                                            else {
                                                for (MailView.Mail mail : mailView.data) {
                                                    mail.isDelete = false;
                                                }
                                                mailListAdapter.notifyDataSetChanged();
                                            }
                                            cb_DeleteAll.setVisibility(View.GONE);
                                        }
                                    })
                                    .setPositiveButton("永久刪除", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DeleteMail(mailView.data.size(), true, DeleteCount);
                                        }
                                    })
                                    .show();
                        }

                        return;
                    }
                }
                isDeleting = !isDeleting;
                mailListAdapter.notifyDataSetChanged();
                cb_DeleteAll.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
            }
        });


        rv_MailList = (MyRecyclerView) nav_end.findViewById(R.id.rv_MailList);
        rv_MailList.SetMainActivity(this);

        mSwipeLayout = (SwipeRefreshLayout) nav_end.findViewById(R.id.srl_MailList);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //檢查網路連線
                if (!SharedService.CheckNetWork(MainActivity.this)) {
                    SharedService.ShowTextToast("請檢察網路連線", MainActivity.this);
                    mSwipeLayout.setRefreshing(false);
                    return;
                }
                Refresh();
            }
        });
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.colorTransparent));
        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeLayout.setDistanceToTriggerSync(400);
        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);

        drawer.addView(nav_end);

        SetCache((int) Runtime.getRuntime().maxMemory() / 10);

        Refresh();
    }

    private void Refresh() {
        mSwipeLayout.setRefreshing(true);

        mailView = new MailView();
        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            isFinishLoad = false;
            mailListAdapter.mFooterId = 0;
            mailListAdapter.notifyDataSetChanged();
        }
        GetMailList();
    }

    private void GetMailList() {
        String url = getString(R.string.BackEndPath);
        if (mailType == 0)
            url += "api/getMailByToken";
        else if (mailType == 1)
            url += "api/getSentMailByToken";
        else if (mailType == 2)
            url += "api/getFavouriteFolder";
        else if (mailType == 3)
            url += "api/getTrashFolder";
        url += "?page=" + (mailView.current_page + 1);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 停止刷新
                        mSwipeLayout.setRefreshing(false);
                        SharedService.ShowTextToast("請檢察網路連線", MainActivity.this);
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
                            MailView tempView = new Gson().fromJson(ResMsg, MailView.class);

                            mailView.current_page = tempView.current_page;
                            mailView.data.addAll(tempView.data);

                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_MailList.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                                mailListAdapter = new MailListAdapter();
                                rv_MailList.setAdapter(mailListAdapter);
                            } else {
                                mailListAdapter.notifyDataSetChanged();
                            }

                            if (tempView.current_page >= tempView.last_page) {
                                //最後一次載入
                                isFinishLoad = true;
                                mailListAdapter.setFooterId(R.layout.footer);
                            }
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MainActivity.this);
                        }
                    }
                });

            }

        });
    }

    public class MailListAdapter extends RecyclerView.Adapter<MailListAdapter.ViewHolder> {

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
            View view = LayoutInflater.from(context).inflate(R.layout.mail_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (mailView.data.size() == 0)
                    holder.tv_Footer.setText("您沒有任何信件!");
                else
                    holder.tv_Footer.setText("沒有更多信件囉!");
                return;
            }

            final MailView.Mail mail = mailView.data.get(position);

            if (mail.read) {
                holder.ll_MailBlock.setBackgroundColor(Color.WHITE);
            } else {
                holder.ll_MailBlock.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorGray));
            }

            holder.ll_MailBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDeleting)
                        holder.cb_IsDelete.setChecked(!holder.cb_IsDelete.isChecked());
                    else {
                        Log.e("MyClick", "isSwiping " + rv_MailList.isSwiping + " isClosing " + rv_MailList.isClosing);
                        if (!rv_MailList.isSwiping && !rv_MailList.isClosing) {
                            nowPosition = position;
                            Intent intent = new Intent(MainActivity.this, MailDetailActivity.class);
                            intent.putExtra("Mail", new Gson().toJson(mail));
                            startActivityForResult(intent, READMAIL_CODE);
                        }
                        rv_MailList.isSwiping = false;
                        rv_MailList.isClosing = false;
                    }
                }
            });

            if (isDeleting)
                holder.cb_IsDelete.setVisibility(View.VISIBLE);
            else
                holder.cb_IsDelete.setVisibility(View.GONE);

            holder.cb_IsDelete.setChecked(mail.isDelete);
            holder.cb_IsDelete.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mail.isDelete = isChecked;
                }
            });

            if (mail.senderPic != null) {
                holder.iv_SenderImg.setTag(mail.senderPic);
                showImage(holder.iv_SenderImg, mail.senderPic, true, null);
            } else {
                holder.iv_SenderImg.setTag("");
                holder.iv_SenderImg.setImageResource(R.drawable.defaultmimg);
            }

            holder.tv_MailSender.setText(mail.lSenderName);
            holder.tv_MailTitle.setText(mail.lTitle);

            if (mailType == 3) {
                holder.iv_MarkMail.setVisibility(View.GONE);
                holder.tv_DeleteMail.setVisibility(View.GONE);
                holder.iv_RecoveryMail.setVisibility(View.VISIBLE);
                holder.iv_RecoveryMail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecovering) {
                            SharedService.ShowTextToast("請稍後再試", MainActivity.this);
                        } else {
                            RecoveryAnimate(holder.iv_RecoveryMail);
                            RecoveryMail(position);
                        }
                    }
                });
            } else {

                holder.tv_DeleteMail.setVisibility(View.VISIBLE);
                holder.iv_RecoveryMail.setVisibility(View.GONE);

                if (mailType != 1) {
                    holder.iv_MarkMail.setVisibility(View.VISIBLE);
                    if (mail.favourite) {
                        holder.iv_MarkMail.setImageResource(R.drawable.mark);
                    } else {
                        holder.iv_MarkMail.setImageResource(R.drawable.unmark);
                    }
                    holder.iv_MarkMail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!rv_MailList.isSwiping && !rv_MailList.isClosing) {
                                if (mail.favourite) {
                                    FavouriteMail(position, false, holder.iv_MarkMail);
                                } else {
                                    FavouriteMail(position, true, holder.iv_MarkMail);
                                }
                            }
                            rv_MailList.isSwiping = false;
                            rv_MailList.isClosing = false;
                        }
                    });
                } else {
                    holder.iv_MarkMail.setVisibility(View.GONE);
                }

                holder.tv_DeleteMail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rv_MailList.scroller.startScroll(holder.ll_MailBlockAll.getScrollX(), 0, -holder.tv_DeleteMail.getWidth(), 0, 100);
                        rv_MailList.invalidate();
                        rv_MailList.state = 0;
                        mail.isDelete = true;

                        if (mailType != 1) {
                            DeleteMail(position + 1, false, 1);
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("確定要刪除這封信嗎?")
                                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            for (MailView.Mail mail : mailView.data) {
                                                mail.isDelete = false;
                                            }
                                            isDeleting = false;
                                            mailListAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .setPositiveButton("永久刪除", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DeleteMail(position + 1, true, 1);
                                        }
                                    })
                                    .show();
                        }
                    }
                });
            }

            //避免重複請求
            if (position > mailView.data.size() * 0.6 && !isFinishLoad && !isLoading) {
                isLoading = true;
                GetMailList();
            }
        }

        @Override
        public int getItemCount() {
            int NormalCount = mailView.data.size();
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout ll_MailBlockAll;
            private LinearLayout ll_MailBlock;
            private CheckBox cb_IsDelete;
            private ImageView iv_SenderImg;
            private TextView tv_MailSender;
            private TextView tv_MailTitle;
            private ImageView iv_MarkMail;
            private ImageView iv_RecoveryMail;
            public TextView tv_DeleteMail;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_MailBlockAll = (LinearLayout) itemView.findViewById(R.id.ll_MailBlockAll);
                ll_MailBlock = (LinearLayout) itemView.findViewById(R.id.ll_MailBlock);
                cb_IsDelete = (CheckBox) itemView.findViewById(R.id.cb_IsDelete);
                iv_SenderImg = (ImageView) itemView.findViewById(R.id.iv_SenderImg);
                tv_MailSender = (TextView) itemView.findViewById(R.id.tv_MailSender);
                tv_MailTitle = (TextView) itemView.findViewById(R.id.tv_MailTitle);
                iv_MarkMail = (ImageView) itemView.findViewById(R.id.iv_MarkMail);
                iv_RecoveryMail = (ImageView) itemView.findViewById(R.id.iv_RecoveryMail);
                tv_DeleteMail = (TextView) itemView.findViewById(R.id.tv_DeleteMail);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READMAIL_CODE && resultCode == RESULT_OK && nowPosition != -1) {
            if (data != null) {
                int MailStatus = data.getIntExtra("MailStatus", -1);
                if (MailStatus == 1) {
                    drawer.closeDrawer(GravityCompat.END);
                    GoProcessResume(new View(this));
                } else if (MailStatus == 11) {
                    drawer.closeDrawer(GravityCompat.END);
                    GoMyStudent(new View(this));
                }
            } else {
                mailView.data.get(nowPosition).read = true;
                mailListAdapter.notifyDataSetChanged();
                nowPosition = -1;
            }
        }
    }

    private void FavouriteMail(final int Position, final boolean IsMark, final ImageView iv_MarkMail) {
        RequestBody formBody = new FormBody.Builder()
                .add("slId", mailView.data.get(Position).slId + "")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/favouriteMail")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MainActivity.this);
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
                            if (IsMark) {
                                mailView.data.get(Position).favourite = true;
                                iv_MarkMail.setImageResource(R.drawable.mark);
                            } else {
                                mailView.data.get(Position).favourite = false;
                                iv_MarkMail.setImageResource(R.drawable.unmark);
                            }

                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MainActivity.this);
                        }
                    }
                });
            }
        });
    }

    private void RecoveryAnimate(final ImageView iv_RecoveryMail) {
        iv_RecoveryMail.animate()
                .rotationBy(-360)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isRecovering)
                            RecoveryAnimate(iv_RecoveryMail);
                    }
                });
    }

    private void RecoveryMail(final int Position) {
        isRecovering = true;
        RequestBody formBody = new FormBody.Builder()
                .add("slId", mailView.data.get(Position).slId + "")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/mailRestoreDeleted")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", MainActivity.this);
                        isRecovering = false;
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
                            mailView.data.remove(Position);
                            mailListAdapter.notifyItemRemoved(Position);
                            mailListAdapter.notifyItemRangeChanged(0, mailView.data.size());
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, MainActivity.this);
                        }
                        isRecovering = false;
                    }
                });
            }
        });
    }

    private void DeleteMail(int StartPosition, final boolean IsForce, final int Count) {
        for (int i = StartPosition - 1; i >= 0; i--) {
            final int Position = i;
            if (mailView.data.get(Position).isDelete) {
                rv_MailList.scrollToPosition(Position);
                String url = getString(R.string.BackEndPath);
                if (IsForce) {
                    if (mailType == 1) {
                        url += "api/sendMailDeleted?slId=";
                    } else {
                        url += "api/mailForceDeleted?slId=";
                    }
                } else
                    url += "api/mailDeleted?slId=";
                Request request = new Request.Builder()
                        .url(url + mailView.data.get(Position).slId)
                        .delete()
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedService.ShowTextToast("請檢察網路連線", MainActivity.this);
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
                                    mailView.data.remove(Position);
                                    mailListAdapter.notifyItemRemoved(Position);
                                    deletedCount++;
                                    if (deletedCount == Count) {
                                        deletedCount = 0;
                                        isDeleting = false;
                                        mailListAdapter.notifyItemRangeChanged(0, mailView.data.size());
                                        if (cb_DeleteAll.isChecked())
                                            cb_DeleteAll.setChecked(false);
                                        else
                                            mailListAdapter.notifyItemRangeChanged(0, mailView.data.size());
                                        cb_DeleteAll.setVisibility(View.GONE);
                                    } else {
                                        DeleteMail(Position, IsForce, Count);
                                    }
                                } else {
                                    SharedService.HandleError(StatusCode, ResMsg, MainActivity.this);
                                }
                            }
                        });
                    }
                });
                return;
            }
        }
    }

    private void SetColor() {
        LinearLayout ll_NoLoginDrawer = (LinearLayout) findViewById(R.id.ll_NoLoginDrawer);
        LinearLayout ll_StudentDrawer = (LinearLayout) findViewById(R.id.ll_StudentDrawer);
        LinearLayout ll_TeacherDrawer = (LinearLayout) findViewById(R.id.ll_TeacherDrawer);
        LinearLayout ll_CompanyDrawer = (LinearLayout) findViewById(R.id.ll_CompanyDrawer);

        LinearLayout ll_NoLoginBackground = (LinearLayout) findViewById(R.id.ll_NoLoginBackground);
        LinearLayout ll_StudentBackground = (LinearLayout) findViewById(R.id.ll_StudentBackground);
        LinearLayout ll_TeacherBackground = (LinearLayout) findViewById(R.id.ll_TeacherBackground);
        LinearLayout ll_CompanyBackground = (LinearLayout) findViewById(R.id.ll_CompanyBackground);

        CircleImageView civ_StudentImage = (CircleImageView) findViewById(R.id.civ_StudentImage);
        CircleImageView civ_TeacherImage = (CircleImageView) findViewById(R.id.civ_TeacherImage);
        CircleImageView civ_CompanyImage = (CircleImageView) findViewById(R.id.civ_CompanyImage);

        TextView tv_StudentName = (TextView) findViewById(R.id.tv_StudentName);
        TextView tv_TeacherName = (TextView) findViewById(R.id.tv_TeacherName);
        TextView tv_CompanyName = (TextView) findViewById(R.id.tv_CompanyName);

        if (SharedService.identityView == null || SharedService.identityView.u_status == 3) {
            ll_NoLoginDrawer.setVisibility(View.VISIBLE);
            ll_StudentDrawer.setVisibility(View.GONE);
            ll_TeacherDrawer.setVisibility(View.GONE);
            ll_CompanyDrawer.setVisibility(View.GONE);
            ll_NoLoginBackground.setBackgroundResource(R.drawable.bg_unlog);
        } else if (SharedService.identityView.u_status == 0) {
            ll_NoLoginDrawer.setVisibility(View.GONE);
            ll_StudentDrawer.setVisibility(View.VISIBLE);
            ll_TeacherDrawer.setVisibility(View.GONE);
            ll_CompanyDrawer.setVisibility(View.GONE);
            ll_StudentBackground.setBackgroundResource(R.drawable.bg_student);

            if (SharedService.identityView.profilePic != null) {
                civ_StudentImage.setTag(SharedService.identityView.profilePic);
                showImage(civ_StudentImage, SharedService.identityView.profilePic, true, null);
            }
            tv_StudentName.setText(SharedService.identityView.u_name);
        } else if (SharedService.identityView.u_status == 1) {
            ll_NoLoginDrawer.setVisibility(View.GONE);
            ll_StudentDrawer.setVisibility(View.GONE);
            ll_TeacherDrawer.setVisibility(View.VISIBLE);
            ll_CompanyDrawer.setVisibility(View.GONE);
            ll_TeacherBackground.setBackgroundResource(R.drawable.bg_teacher);

            if (SharedService.identityView.profilePic != null) {
                civ_TeacherImage.setTag(SharedService.identityView.profilePic);
                showImage(civ_TeacherImage, SharedService.identityView.profilePic, true, null);
            }
            tv_TeacherName.setText(SharedService.identityView.u_name);
        } else if (SharedService.identityView.u_status == 2) {
            ll_NoLoginDrawer.setVisibility(View.GONE);
            ll_StudentDrawer.setVisibility(View.GONE);
            ll_TeacherDrawer.setVisibility(View.GONE);
            ll_CompanyDrawer.setVisibility(View.VISIBLE);
            ll_CompanyBackground.setBackgroundResource(R.drawable.bg_office);

            if (SharedService.identityView.profilePic != null) {
                civ_CompanyImage.setTag(SharedService.identityView.profilePic);
                showImage(civ_CompanyImage, SharedService.identityView.profilePic, true, null);
            }
            tv_CompanyName.setText(SharedService.identityView.u_name);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            if (contentFragmentList.size() == 0 && (getSupportFragmentManager().findFragmentByTag("HomeFragment") != null ||
                    getSupportFragmentManager().findFragmentByTag("SplashScreenFragment") != null ||
                    getSupportFragmentManager().findFragmentByTag("ReLinkFragment") != null)) {
                if (IsFinishing) {
                    super.onBackPressed();
                } else {
                    SharedService.ShowTextToast("再按一次退出", this);
                    IsFinishing = true;
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IsFinishing = false;
                    }
                }, 1500);
            } else {
                contentFragmentList.remove(contentFragmentList.size() - 1);
                if (contentFragmentList.size() == 0) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.MainFrameLayout, new HomeFragment(), "HomeFragment")
                            .commit();
                    return;
                }
                String FragmentName = contentFragmentList.get(contentFragmentList.size() - 1);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.MainFrameLayout, GetFragment(FragmentName), FragmentName)
                        .commit();

                //避免首頁被推到最底時重複HomeFragment
                if (contentFragmentList.size() == 1 && contentFragmentList.get(0).equals("HomeFragment"))
                    contentFragmentList.remove(0);
            }
        }
    }

    private Fragment GetFragment(String FragmentName) {
        if (FragmentName.equals("LoginFragment"))
            return new LoginFragment();
        else if (FragmentName.equals("CompanyRegisterFragment"))
            return new CompanyRegisterFragment();
        else if (FragmentName.equals("SchoolRegisterFragment"))
            return new SchoolRegisterFragment();
        else if (FragmentName.equals("ResumeFragment"))
            return new ResumeFragment();
        else if (FragmentName.equals("HomeFragment"))
            return new HomeFragment();
        else if (FragmentName.equals("MyJobFragment"))
            return new MyJobFragment();
        else if (FragmentName.equals("AllJobFragment"))
            return new AllJobFragment();
        else if (FragmentName.equals("CompanyListFragment"))
            return new CompanyListFragment();
        else if (FragmentName.equals("EditCompanyFragment"))
            return new EditCompanyFragment();
        else if (FragmentName.equals("InternCourseListFragment"))
            return new InternCourseListFragment();
        else if (FragmentName.equals("VisitCourseListFragment"))
            return new VisitCourseListFragment();
        else if (FragmentName.equals("MyStudentListFragment"))
            return new MyStudentListFragment();
        else if (FragmentName.equals("EditTeacherPicFragment"))
            return new EditTeacherPicFragment();
        else if (FragmentName.equals("ProcessResumeFragment"))
            return new ProcessResumeFragment();
        return new Fragment();
    }

    public void OpenDrawer(View v) {
        drawer.openDrawer(GravityCompat.START);
    }

    public void OpenEndDrawer(View v) {
        drawer.openDrawer(GravityCompat.END);
    }

    public void GoLogin(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("LoginFragment") == null) {
            if (contentFragmentList.contains("LoginFragment")) {
                contentFragmentList.remove("LoginFragment");
            }
            contentFragmentList.add("LoginFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new LoginFragment(), "LoginFragment")
                    .commit();
        }
    }

    private boolean isLogout = false;

    public void Logout(View v) {
        drawer.closeDrawer(GravityCompat.START);
        SharedService.sp_httpData.edit()
                .putString("Token", "")
                .apply();
        isLogout = true;
        CheckLogon();
    }

    public void GoCompanyRegister(View v) {
        if (contentFragmentList.contains("CompanyRegisterFragment")) {
            contentFragmentList.remove("CompanyRegisterFragment");
        }
        contentFragmentList.add("CompanyRegisterFragment");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.MainFrameLayout, new CompanyRegisterFragment(), "CompanyRegisterFragment")
                .commit();
    }

    public void GoSchoolRegister(View v) {
        if (contentFragmentList.contains("SchoolRegisterFragment")) {
            contentFragmentList.remove("SchoolRegisterFragment");
        }
        contentFragmentList.add("SchoolRegisterFragment");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.MainFrameLayout, new SchoolRegisterFragment(), "SchoolRegisterFragment")
                .commit();
    }

    public void GoResume(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("ResumeFragment") == null) {
            if (contentFragmentList.contains("ResumeFragment")) {
                contentFragmentList.remove("ResumeFragment");
            }
            contentFragmentList.add("ResumeFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new ResumeFragment(), "ResumeFragment")
                    .commit();
        }
    }

    public void GoHome(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("HomeFragment") == null) {
            if (contentFragmentList.contains("HomeFragment")) {
                contentFragmentList.remove("HomeFragment");
            }
            contentFragmentList.add("HomeFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new HomeFragment(), "HomeFragment")
                    .commit();
        }
    }

    public void GoMyJob(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("MyJobFragment") == null) {
            if (contentFragmentList.contains("MyJobFragment")) {
                contentFragmentList.remove("MyJobFragment");
            }
            contentFragmentList.add("MyJobFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new MyJobFragment(), "MyJobFragment")
                    .commit();
        }
    }

    public void GoAllJob(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("AllJobFragment") == null) {
            if (contentFragmentList.contains("AllJobFragment")) {
                contentFragmentList.remove("AllJobFragment");
            }
            contentFragmentList.add("AllJobFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new AllJobFragment(), "AllJobFragment")
                    .commit();
        }
    }

    public void GoCompanyList(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("CompanyListFragment") == null) {
            if (contentFragmentList.contains("CompanyListFragment")) {
                contentFragmentList.remove("CompanyListFragment");
            }
            contentFragmentList.add("CompanyListFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new CompanyListFragment(), "CompanyListFragment")
                    .commit();
        }
    }

    public void GoEditCompany(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("EditCompanyFragment") == null) {
            if (contentFragmentList.contains("EditCompanyFragment")) {
                contentFragmentList.remove("EditCompanyFragment");
            }
            contentFragmentList.add("EditCompanyFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new EditCompanyFragment(), "EditCompanyFragment")
                    .commit();
        }
    }

    public void GoInternCourse(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("InternCourseListFragment") == null) {
            if (contentFragmentList.contains("InternCourseListFragment")) {
                contentFragmentList.remove("InternCourseListFragment");
            }
            contentFragmentList.add("InternCourseListFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new InternCourseListFragment(), "InternCourseListFragment")
                    .commit();
        }
    }

    public void GoVisit(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("VisitCourseListFragment") == null) {
            if (contentFragmentList.contains("VisitCourseListFragment")) {
                contentFragmentList.remove("VisitCourseListFragment");
            }
            contentFragmentList.add("VisitCourseListFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new VisitCourseListFragment(), "VisitCourseListFragment")
                    .commit();
        }
    }

    public void GoMyStudent(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("MyStudentListFragment") == null) {
            if (contentFragmentList.contains("MyStudentListFragment")) {
                contentFragmentList.remove("MyStudentListFragment");
            }
            contentFragmentList.add("MyStudentListFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new MyStudentListFragment(), "MyStudentListFragment")
                    .commit();
        }
    }

    public void GoEditTeacherPic(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("EditTeacherPicFragment") == null) {
            if (contentFragmentList.contains("EditTeacherPicFragment")) {
                contentFragmentList.remove("EditTeacherPicFragment");
            }
            contentFragmentList.add("EditTeacherPicFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new EditTeacherPicFragment(), "EditTeacherPicFragment")
                    .commit();
        }
    }

    public void GoProcessResume(View v) {
        drawer.closeDrawer(GravityCompat.START);
        if (getSupportFragmentManager().findFragmentByTag("ProcessResumeFragment") == null) {
            if (contentFragmentList.contains("ProcessResumeFragment")) {
                contentFragmentList.remove("ProcessResumeFragment");
            }
            contentFragmentList.add("ProcessResumeFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.MainFrameLayout, new ProcessResumeFragment(), "ProcessResumeFragment")
                    .commit();
        }
    }

    public void GoResetPassword(View v) {
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    public void GoAboutUs(View v) {
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
    }
}
