package tw.edu.nutc.iminternshipsystem;


import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

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
public class LoginFragment extends MySharedFragment {
    private EditText et_Account;
    private EditText et_Password;
    private String account;
    private String password;
    private MainActivity mainActivity;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(view);
        mainActivity = (MainActivity) getActivity();
        super.client = mainActivity.client;
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return view;
    }


    private void initViews(View view) {
        SetToolBar("", view);
        et_Account = (EditText) view.findViewById(R.id.et_Account);
        et_Password = (EditText) view.findViewById(R.id.et_Password);
        et_Password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    SharedService.HideKeyboard(mainActivity);
                    mainActivity.activity_Outer.requestFocus();
                    Login();
                }
                return false;
            }
        });
        view.findViewById(R.id.bt_Login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        view.findViewById(R.id.tv_ForgetPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgetPassword();
            }
        });
    }


    public void Login() {
        account = et_Account.getText().toString();
        password = et_Password.getText().toString();
        RequestBody formBody = new FormBody.Builder()
                .add("account", account)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/Login")
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            String Token = new Gson().fromJson(ResMsg, String.class);
                            SharedService.sp_httpData.edit()
                                    .putString("Token", Token)
                                    .apply();
                            mainActivity.CheckLogon();
                            SharedService.ShowTextToast("登入成功", mainActivity);
                        } else {
                            if (ResMsg.contains("帳號未開通")) {
                                new AlertDialog.Builder(mainActivity)
                                        .setTitle("帳號未開通")
                                        .setMessage("請前往信箱收取驗證信")
                                        .setNegativeButton("重寄驗證信", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                et_Password.setText("");
                                                ReSendEmail();
                                            }
                                        })
                                        .setPositiveButton("知道了", null)
                                        .show();
                            } else {
                                et_Password.setText("");
                                SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                            }

                        }
                    }
                });
            }
        });

    }

    private void ReSendEmail(){
        RequestBody formBody = new FormBody.Builder()
                .add("account", account)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/resendVerificationLetter")
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("寄出成功", mainActivity);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });
    }

    public void ForgetPassword() {
        account = et_Account.getText().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("account", account)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/forgetPassword")
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("請至信箱收信", mainActivity);
                            View view = LayoutInflater.from(mainActivity).inflate(R.layout.forget_password_block, null, false);
                            final EditText et_Token = (EditText) view.findViewById(R.id.et_Token);
                            final EditText et_NewPassword = (EditText) view.findViewById(R.id.et_NewPassword);
                            final EditText et_NewPasswordCheck = (EditText) view.findViewById(R.id.et_NewPasswordCheck);

                            new AlertDialog.Builder(mainActivity)
                                    .setView(view)
                                    .setPositiveButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ResetPassword(et_Token.getText().toString(),
                                                            et_NewPassword.getText().toString(),
                                                            et_NewPasswordCheck.getText().toString());
                                                }
                                            })
                                    .setNegativeButton("取消", null)
                                    .show();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });

    }

    public void ResetPassword(String Token, String NewPassword, String NewPasswordCheck) {
        RequestBody formBody = new FormBody.Builder()
                .add("token", Token)
                .add("password", NewPassword)
                .add("conf_pass", NewPasswordCheck)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/resetPassword")
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

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StatusCode == 200) {
                            SharedService.ShowTextToast("修改成功", mainActivity);
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, mainActivity);
                        }
                    }
                });
            }
        });

    }
}
