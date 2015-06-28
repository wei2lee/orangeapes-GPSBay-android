package com.tiny.gpsbay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by leeyeechuan on 6/21/15.
 */
public class ImageListActivity extends Activity {
    public int source;

    public JSONArray items;
    public ImageListAdapter listAdapter;
    public View view;

    @InjectView(R.id.list_view)
    ListView list_view;

    @InjectView(R.id.text_subtitle)
    TextView text_subtitle;

    @InjectView(R.id.view_navigationbar)
    View view_navigationbar;

    @InjectView(R.id.text_title)
    TextView text_navigationbar_title;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if(view == null) {
            if (getIntent() != null) {
                source = getIntent().getIntExtra("source", 0);
            }

            view = getLayoutInflater().inflate(R.layout.activity_imagelist, null);
            setContentView(view);
            ButterKnife.inject(this);
            if (getIntent() != null) {
                text_subtitle.setText(getIntent().getStringExtra("subtitle"));
                text_subtitle.setTextColor(getIntent().getIntExtra("color", 0));
                view_navigationbar.setBackgroundColor(getIntent().getIntExtra("color", 0));
                text_navigationbar_title.setText(getIntent().getStringExtra("navigationbar_title"));
            }

            listAdapter = new ImageListAdapter(this, getLayoutInflater());
            listAdapter.downscale = new BitmapDownscale("540", 540);
            list_view.setAdapter(listAdapter);
            query();
        //}
    }

    void query() {
        try {
            Application app = (Application)getApplication();
            items = new JSONArray(app.loadJSONFromRaw(source));
        } catch(Exception ex) {
            items = new JSONArray();
        }
        listAdapter.updateData(items);
    }

    @OnClick(R.id.btn_back)
    void onClick(View v) {
        finish();
    }
}
