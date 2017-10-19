package tw.edu.nutc.iminternshipsystem;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import ViewModel.MyStudentView;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyStudentListFragment extends MySharedFragment {

    private MainActivity mainActivity;

    public MyStudentView myStudentView;

    public RecyclerView rv_MyStudentList;
    private MyStudentListAdapter myStudentListAdapter;

    public MyStudentListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_student_list, container, false);
        mainActivity = (MainActivity) getActivity();
        super.activity = mainActivity;
        super.client = mainActivity.client;
        super.imageClient = SharedService.GetClient(mainActivity);
        SetCache((int) Runtime.getRuntime().maxMemory() / 20);
        initView(view);
        GetMyStudentList();
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        rv_MyStudentList = (RecyclerView) view.findViewById(R.id.rv_MyStudentList);
    }

    private void GetMyStudentList() {
        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/teacherGetNotExpiredStudentList")
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
                            myStudentView = new Gson().fromJson(ResMsg, MyStudentView.class);

                            rv_MyStudentList.setLayoutManager(new GridLayoutManager(mainActivity, 2));
                            myStudentListAdapter = new MyStudentListAdapter();
                            rv_MyStudentList.setAdapter(myStudentListAdapter);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });

            }

        });
    }

    public class MyStudentListAdapter extends RecyclerView.Adapter<MyStudentListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            View view = LayoutInflater.from(context).inflate(R.layout.mystudent_block, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            int width = mainActivity.myWidth / 3;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
            holder.civ_StudentImage.setLayoutParams(layoutParams);
            holder.civ_StudentImage.setImageResource(R.drawable.defaultmimg);
            if (myStudentView.student_list.get(position).profilePic != null) {
                holder.civ_StudentImage.setTag(myStudentView.student_list.get(position).profilePic);
                showImage(holder.civ_StudentImage, myStudentView.student_list.get(position).profilePic, true, null);
            } else {
                holder.civ_StudentImage.setTag("");
            }
            holder.tv_StudentName.setText(myStudentView.student_list.get(position).u_name);

            holder.ll_MyStudent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mainActivity, MyStudentResumeActivity.class);
                    intent.putExtra("Sid", myStudentView.student_list.get(position).id);
                    intent.putExtra("StudentName", myStudentView.student_list.get(position).u_name);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return myStudentView.student_list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout ll_MyStudent;
            private CircleImageView civ_StudentImage;
            private TextView tv_StudentName;

            public ViewHolder(View itemView) {
                super(itemView);
                ll_MyStudent = (LinearLayout) itemView.findViewById(R.id.ll_MyStudent);
                civ_StudentImage = (CircleImageView) itemView.findViewById(R.id.civ_StudentImage);
                tv_StudentName = (TextView) itemView.findViewById(R.id.tv_StudentName);
            }
        }
    }
}
