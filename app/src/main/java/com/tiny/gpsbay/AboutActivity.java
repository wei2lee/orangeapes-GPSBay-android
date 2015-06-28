package com.tiny.gpsbay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by leeyeechuan on 6/21/15.
 */
public class AboutActivity extends Activity {
    public View view;

    @InjectView(R.id.text_subtitle)
    TextView text_subtitle;

    @InjectView(R.id.view_navigationbar)
    View view_navigationbar;

    @InjectView(R.id.text_title)
    TextView text_navigationbar_title;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(view == null) {
            view = getLayoutInflater().inflate(R.layout.activity_about, null);
            setContentView(view);
            ButterKnife.inject(this);
            if (getIntent() != null) {
                text_subtitle.setText(getIntent().getStringExtra("subtitle"));
                view_navigationbar.setBackgroundColor(getIntent().getIntExtra("color", 0));
                text_navigationbar_title.setText(getIntent().getStringExtra("navigationbar_title"));
            }
        }
    }

    @OnClick(R.id.btn_back)
    void onClick(View v) {
        finish();
    }
}