package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyStuJobExFragment extends MySharedFragment {

    private MyStudentResumeActivity myStudentResumeActivity;

    public RecyclerView rv_MyStuJobExList;
    private MyStuJobExListAdapter myStuJobExListAdapter;

    public MyStuJobExFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_stu_job_ex, container, false);
        myStudentResumeActivity = (MyStudentResumeActivity) getActivity();
        super.client = myStudentResumeActivity.client;

        initView(view);
        DrawData();
        return view;
    }

    private void initView(View view) {
        rv_MyStuJobExList = (RecyclerView) view.findViewById(R.id.rv_MyStuJobExList);
    }

    public void DrawData() {
        rv_MyStuJobExList.setLayoutManager(new LinearLayoutManager(myStudentResumeActivity, LinearLayoutManager.VERTICAL, false));
        myStuJobExListAdapter = new MyStuJobExListAdapter();
        rv_MyStuJobExList.setAdapter(myStuJobExListAdapter);
    }


    public class MyStuJobExListAdapter extends RecyclerView.Adapter<MyStuJobExListAdapter.ViewHolder> {

        public final int TYPE_FOOTER = 1;  //说明是带有Footer的
        public final int TYPE_NORMAL = 2;  //说明是不带有header和footer的
        private View mFooterView;

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            }
            return TYPE_NORMAL;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            if (viewType == TYPE_FOOTER) {
                mFooterView = LayoutInflater.from(context).inflate(R.layout.footer, parent, false);
                return new ViewHolder(mFooterView);
            }

            View view = LayoutInflater.from(context).inflate(R.layout.jobexperience_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (myStudentResumeActivity.resumeView.stu_jobExperience.size() == 0)
                    holder.tv_Footer.setText("沒有任何工作經驗!");
                else
                    holder.tv_Footer.setText("無更多工作經驗!");
                return;
            }

            holder.ll_JobExSave.setVisibility(View.VISIBLE);
            holder.tv_JobName.setText(myStudentResumeActivity.resumeView.stu_jobExperience.get(position).jobTitle);
            holder.tv_CompanyName.setText(myStudentResumeActivity.resumeView.stu_jobExperience.get(position).comName);
            holder.iv_Delete.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return myStudentResumeActivity.resumeView.stu_jobExperience.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_JobExSave;
            private TextView tv_JobName;
            private TextView tv_CompanyName;
            private ImageView iv_Delete;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);

                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_JobExSave = (LinearLayout) itemView.findViewById(R.id.ll_JobExSave);
                tv_JobName = (TextView) itemView.findViewById(R.id.tv_JobName);
                tv_CompanyName = (TextView) itemView.findViewById(R.id.tv_CompanyName);
                iv_Delete = (ImageView) itemView.findViewById(R.id.iv_Delete);
            }
        }
    }

}
