package tw.edu.nutc.iminternshipsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import MyMethod.SharedService;
import ViewModel.AnnouncementView;
import me.grantland.widget.AutofitTextView;

public class AnnDetailActivity extends MySharedActivity {
    private AnnouncementView.Announcement announcement;
    private TextView tv_UpdateTime;
    private TextView tv_CreateTime;
    private LinearLayout ll_AnnFilesBlock;
    private TextView tv_AnnContent;
    private AutofitTextView atv_AnnTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ann_detail);
        announcement = new Gson().fromJson(getIntent().getStringExtra("Announcement"), AnnouncementView.Announcement.class);
        initView();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initView() {
        SetToolBar("公告", true);
        activity_Outer = findViewById(R.id.ll_ActivityOuter);

        atv_AnnTitle = (AutofitTextView) findViewById(R.id.atv_AnnTitle);
        tv_AnnContent = (TextView) findViewById(R.id.tv_AnnContent);
        ll_AnnFilesBlock = (LinearLayout) findViewById(R.id.ll_AnnFilesBlock);
        tv_CreateTime = (TextView) findViewById(R.id.tv_CreateTime);
        tv_UpdateTime = (TextView) findViewById(R.id.tv_UpdateTime);

        atv_AnnTitle.setText(announcement.anTittle);
        tv_AnnContent.setText(announcement.anContent);
        if (announcement.anFile != null) {
            ll_AnnFilesBlock.setVisibility(View.VISIBLE);
            String[] Files = announcement.anFile.split(",");
            for (String FileName : Files) {
                ll_AnnFilesBlock.addView(GetATV(FileName));
            }
        }
        tv_CreateTime.setText(announcement.created_at);
        tv_UpdateTime.setText(announcement.updated_at);
    }

    private AutofitTextView GetATV(final String FileName) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, (int) SharedService.DipToPixels(this, 5f), 0, 0);
        AutofitTextView autofitTextView = new AutofitTextView(this);
        autofitTextView.setText(FileName);
        autofitTextView.setTextSize(20);
        autofitTextView.setMaxTextSize(20);
        autofitTextView.setMinTextSize(5);
        autofitTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        autofitTextView.setMaxLines(1);
        autofitTextView.setLayoutParams(layoutParams);
        autofitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AnnDetailActivity.this)
                        .setMessage("確定要下載 " + FileName + " 嗎?")
                        .setNeutralButton("取消", null)
                        .setPositiveButton("下載", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse(getString(R.string.BackEndPath) + "api/downloadAnnouncementFileByFileName?fileName=" + FileName);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });
        return autofitTextView;
    }
}
