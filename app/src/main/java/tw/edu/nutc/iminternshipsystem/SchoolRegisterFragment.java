package tw.edu.nutc.iminternshipsystem;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;

import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolRegisterFragment extends MySharedFragment {
    private EditText et_Account;
    private EditText et_Password;
    private EditText et_PasswordCheck;
    private EditText et_Name;
    private EditText et_Phone;

    private MainActivity mainActivity;
    private Spinner sp_Status;


    public SchoolRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_school_register, container, false);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        initView(view);
        return view;
    }

    private void initView(View view) {
        SetToolBar("", view);
        et_Account = (EditText) view.findViewById(R.id.et_Account);
        et_Password = (EditText) view.findViewById(R.id.et_Password);
        et_PasswordCheck = (EditText) view.findViewById(R.id.et_PasswordCheck);
        et_Name = (EditText) view.findViewById(R.id.et_Name);
        et_Phone = (EditText) view.findViewById(R.id.et_Phone);
        view.findViewById(R.id.bt_Register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });

        ArrayAdapter<CharSequence> arrayAdapter = new ArrayAdapter<CharSequence>(
                mainActivity,
                android.R.layout.simple_spinner_item,
                new String[]{"學生", "老師"}
        );

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_Status = (Spinner) view.findViewById(R.id.sp_Status);
        sp_Status.setAdapter(arrayAdapter);
    }


    public void Register() {

        RequestBody formBody = new FormBody.Builder()
                .add("account", et_Account.getText().toString())
                .add("u_name", et_Name.getText().toString())
                .add("u_tel", et_Phone.getText().toString())
                .add("password", et_Password.getText().toString())
                .add("conf_pass", et_PasswordCheck.getText().toString())
                .add("u_status", sp_Status.getSelectedItemPosition() + "")
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/Register")
                .post(formBody)
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
                mainActivity.runOnUiThread(new Runnable() {//这是Activity的方法，会在主线程执行任务
                    @Override
                    public void run() {

                        if (StatusCode == 200) {
                            new AlertDialog.Builder(mainActivity)
                                    .setTitle("註冊成功")
                                    .setMessage("請前往信箱驗證您的Email")
                                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mainActivity.GoHome(new View(mainActivity));
                                        }
                                    }).show();
                        } else {
                            et_Password.setText("");
                            et_PasswordCheck.setText("");
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });


    }

}
