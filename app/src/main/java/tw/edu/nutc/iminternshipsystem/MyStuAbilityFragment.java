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
public class MyStuAbilityFragment extends MySharedFragment {
    private MyStudentResumeActivity myStudentResumeActivity;

    public RecyclerView rv_MyStuAbilityList;
    private MyStuAbilityListAdapter myStuAbilityListAdapter;
    private String[] AbilityTypes = {"大數據", "伺服器架設", "資料庫", "後端程式設計", "前端程式設計", "多媒體", "文書處理"};

    public MyStuAbilityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_stu_ability, container, false);
        myStudentResumeActivity = (MyStudentResumeActivity) getActivity();
        super.client = myStudentResumeActivity.client;

        initView(view);
        DrawData();
        return view;
    }

    private void initView(View view) {
        rv_MyStuAbilityList = (RecyclerView) view.findViewById(R.id.rv_MyStuAbilityList);
    }

    public void DrawData() {
        rv_MyStuAbilityList.setLayoutManager(new LinearLayoutManager(myStudentResumeActivity, LinearLayoutManager.VERTICAL, false));
        myStuAbilityListAdapter = new MyStuAbilityListAdapter();
        rv_MyStuAbilityList.setAdapter(myStuAbilityListAdapter);
    }

    public class MyStuAbilityListAdapter extends RecyclerView.Adapter<MyStuAbilityListAdapter.ViewHolder> {

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

            View view = LayoutInflater.from(context).inflate(R.layout.studentability_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (getItemViewType(position) == TYPE_FOOTER) {
                if (myStudentResumeActivity.resumeView.stu_ability.size() == 0)
                    holder.tv_Footer.setText("沒有任何技術能力!");
                else
                    holder.tv_Footer.setText("無更多技術能力!");
                return;
            }

            holder.ll_AbilitySave.setVisibility(View.VISIBLE);
            holder.tv_AbilityType.setText(AbilityTypes[myStudentResumeActivity.resumeView.stu_ability.get(position).abiType]);
            holder.tv_AbilityIntro.setText(myStudentResumeActivity.resumeView.stu_ability.get(position).abiName);
            holder.iv_Delete.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return myStudentResumeActivity.resumeView.stu_ability.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_AbilitySave;
            private TextView tv_AbilityType;
            private TextView tv_AbilityIntro;
            private ImageView iv_Delete;

            private TextView tv_Footer;

            public ViewHolder(View itemView) {
                super(itemView);

                if (itemView == mFooterView) {
                    tv_Footer = (TextView) itemView.findViewById(R.id.tv_Footer);
                    return;
                }

                ll_AbilitySave = (LinearLayout) itemView.findViewById(R.id.ll_AbilitySave);
                tv_AbilityType = (TextView) itemView.findViewById(R.id.tv_AbilityType);
                tv_AbilityIntro = (TextView) itemView.findViewById(R.id.tv_AbilityIntro);
                iv_Delete = (ImageView) itemView.findViewById(R.id.iv_Delete);
            }
        }
    }
}
