package com.tiny.gpsbay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity {


    @InjectView(R.id.btn_nav_map)
    Button btn_nav_map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btn_nav_map)
    void onBtnNavClick(View v) {
        Intent i = new Intent(this, MapActivity.class);
        startActivity(i);
    }
}
