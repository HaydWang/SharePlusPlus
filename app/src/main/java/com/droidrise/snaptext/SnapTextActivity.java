package com.droidrise.snaptext;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by F10210C on 21.03.2017.
 */
public class SnapTextActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snaptext);

        ButterKnife.bind(this);

        // TODO: start service at very begin
        startService(new Intent(this, ClipboardService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            Long id = extras.getLong(SnapData.ID);
            SnapData snap = TextSnaper.getSnapData(id);
            initSnapLayout(snap);
        }
    }

    @BindView(R.id.text_data_source) OneKeyClearEditText tvTitle;
    @BindView(R.id.text_second_title) TextView subTitle;
    @BindView(R.id.text_content) TextView tvContent;
    private void initSnapLayout(SnapData data) {
        tvTitle.setText(data.getTitle());
        tvContent.setText(data.getContent());
    }
}
