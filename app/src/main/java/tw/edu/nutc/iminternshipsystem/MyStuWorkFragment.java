package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
public class MyStuWorkFragment extends MySharedFragment {
    private MyStudentResumeActivity myStudentResumeActivity;

    public RecyclerView rv_MyStuWorkList;
    private MyStuWorkListAdapter myStuWorkListAdapter;

    public MyStuWorkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_stu_work, container, false);
        myStudentResumeActivity = (MyStudentResumeActivity) getActivity();
        super.client = myStudentResumeActivity.client;
        initView(view);
        DrawData();
        return view;
    }

    private void initView(View view) {
        rv_MyStuWorkList = (RecyclerView) view.findViewById(R.id.rv_MyStuWorkList);
    }

    public void DrawData() {
        rv_MyStuWorkList.setLayoutManager(new LinearLayoutManager(myStudentResumeActivity, LinearLayoutManager.VERTICAL, false));
        myStuWorkListAdapter = new MyStuWorkListAdapter();
        rv_MyStuWorkList.setAdapter(myStuWorkListAdapter);
    }

    public class MyStuWorkListAdapter extends RecyclerView.Adapter<MyStuWorkListAdapter.ViewHolder> {

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

            View view = LayoutInflater.from(context).inflate(R.layout.studentwork_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (myStudentResumeActivity.resumeView.stu_works.size() == 0)
                    holder.tv_Footer.setText("沒有任何作品!");
                else
                    holder.tv_Footer.setText("無更多作品!");
                return;
            }

            holder.ll_StudentWorkSave.setVisibility(View.VISIBLE);
            holder.tv_WorkName.setText(myStudentResumeActivity.resumeView.stu_works.get(position).wName);
            holder.tv_WorkYear.setText(myStudentResumeActivity.resumeView.stu_works.get(position).wCreatedDate);
            holder.tv_WorkLink.setText(myStudentResumeActivity.resumeView.stu_works.get(position).wLink);
            holder.tv_WorkLink.setTextColor(ContextCompat.getColor(myStudentResumeActivity, android.R.color.holo_blue_dark));
            holder.tv_WorkLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(myStudentResumeActivity, MyWebViewActivity.class);
                    intent.putExtra("URL", holder.tv_WorkLink.getText().toString());
                    startActivity(intent);
                }
            });
            holder.iv_Delete.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return myStudentResumeActivity.resumeView.stu_works.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_StudentWorkSave;
            private TextView tv_WorkName;
            private TextView tv_WorkYear;
            private TextView tv_WorkLink;
            private ImageView iv_Delete;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);

                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_StudentWorkSave = (LinearLayout) itemView.findViewById(R.id.ll_StudentWorkSave);
                tv_WorkName = (TextView) itemView.findViewById(R.id.tv_WorkName);
                tv_WorkYear = (TextView) itemView.findViewById(R.id.tv_WorkYear);
                tv_WorkLink = (TextView) itemView.findViewById(R.id.tv_WorkLink);
                iv_Delete = (ImageView) itemView.findViewById(R.id.iv_Delete);

            }
        }
    }
}
