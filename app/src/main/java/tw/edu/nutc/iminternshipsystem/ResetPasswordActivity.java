package tw.edu.nutc.iminternshipsystem;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.IOException;

import MyMethod.SharedService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetPasswordActivity extends MySharedActivity {

    private EditText et_OldPassword;
    private EditText et_NewPassword;
    private EditText et_NewPasswordCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initViews();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initViews() {
        SetToolBar("修改密碼", true);

        activity_Outer = findViewById(R.id.ll_ActivityOuter);

        et_OldPassword = (EditText) findViewById(R.id.et_OldPassword);
        et_NewPassword = (EditText) findViewById(R.id.et_NewPassword);
        et_NewPasswordCheck = (EditText) findViewById(R.id.et_NewPasswordCheck);
    }

    public void ResetPassword(View v) {
        SharedService.HideKeyboard(this);
        activity_Outer.requestFocus();

        RequestBody formBody = new FormBody.Builder()
                .add("oldPassword", et_OldPassword.getText().toString())
                .add("newPassword", et_NewPassword.getText().toString())
                .add("conf_pass", et_NewPasswordCheck.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.BackEndPath) + "api/userResetPassword")
                .put(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedService.ShowTextToast("請檢察網路連線", ResetPasswordActivity.this);
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
                            SharedService.ShowTextToast("修改成功", ResetPasswordActivity.this);
                            finish();
                        } else {
                            SharedService.HandleError(StatusCode, ResMsg, ResetPasswordActivity.this);
                        }
                    }
                });
            }
        });

    }
}
