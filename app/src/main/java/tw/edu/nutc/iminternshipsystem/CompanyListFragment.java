package tw.edu.nutc.iminternshipsystem;


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
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.CompanyView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompanyListFragment extends MySharedFragment {

    private MainActivity mainActivity;

    public CompanyView companyView;

    private boolean isFirstLoad = true;

    private SwipeRefreshLayout mSwipeLayout;
    public RecyclerView rv_CompanyList;
    private CompanyListAdapter companyListAdapter;

    public CompanyListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_company_list, container, false);
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
        rv_CompanyList = (RecyclerView) view.findViewById(R.id.rv_CompanyList);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_CompanyList);
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

        companyView = new CompanyView();
        //避免重新接上網路時重整導致崩潰
        if (!isFirstLoad) {
            companyListAdapter.mFooterId = 0;
            companyListAdapter.notifyDataSetChanged();
        }
        GetCompanyList();
    }

    private void GetCompanyList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/getCompanyList")
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
                            companyView = new Gson().fromJson(ResMsg, CompanyView.class);

                            if (isFirstLoad) {
                                isFirstLoad = false;
                                rv_CompanyList.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                companyListAdapter = new CompanyListAdapter();
                                rv_CompanyList.setAdapter(companyListAdapter);
                            } else {
                                companyListAdapter.notifyDataSetChanged();
                            }

                            companyListAdapter.setFooterId(R.layout.footer);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    public class CompanyListAdapter extends RecyclerView.Adapter<CompanyListAdapter.ViewHolder> {

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
                mFooterView = LayoutInflater.from(context).inflate(R.layout.footer, parent, false);
                return new ViewHolder(mFooterView);
            }

            View view = LayoutInflater.from(context).inflate(R.layout.company_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (companyView.CompanyList.size() == 0)
                    holder.tv_Footer.setText("還沒有任何廠商!");
                else
                    holder.tv_Footer.setText("沒有更多廠商囉!");
                return;
            }

            if (companyView.CompanyList.get(position).profilePic != null) {
                holder.iv_CompanyImage.setImageDrawable(null);
                holder.iv_CompanyImage.setTag(companyView.CompanyList.get(position).profilePic);
                showImage(holder.iv_CompanyImage, companyView.CompanyList.get(position).profilePic, true, null);
            } else {
                holder.iv_CompanyImage.setTag("");
                holder.iv_CompanyImage.setImageResource(R.drawable.defaultmimg);
            }
            holder.tv_CompanyName.setText(companyView.CompanyList.get(position).c_name);
            holder.tv_CompanyType.setText(companyView.CompanyList.get(position).ctypes);

            holder.ll_CompanyBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SharedService.identityView != null) {
                        Intent intent = new Intent(mainActivity, CompanyDetailActivity.class);
                        intent.putExtra("Company", new Gson().toJson(companyView.CompanyList.get(position)));
                        startActivity(intent);
                    } else {
                        SharedService.ShowTextToast("請登入查看廠商詳細資料", mainActivity);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            int NormalCount = companyView.CompanyList.size();
            if (mFooterId != 0)
                NormalCount++;
            return NormalCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_CompanyBlock;
            private ImageView iv_CompanyImage;
            private TextView tv_CompanyName;
            private TextView tv_CompanyType;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);
                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_CompanyBlock = (LinearLayout) itemView.findViewById(R.id.ll_CompanyBlock);
                iv_CompanyImage = (ImageView) itemView.findViewById(R.id.iv_CompanyImage);
                tv_CompanyName = (TextView) itemView.findViewById(R.id.tv_CompanyName);
                tv_CompanyType = (TextView) itemView.findViewById(R.id.tv_CompanyType);
            }
        }
    }
}
