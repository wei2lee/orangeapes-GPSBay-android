package com.tiny.gpsbay;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Created by leeyeechuan on 6/25/15.
 */
public class SearchableActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    void doMySearch(String query) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cell_searchtext, R.id.text_title);
        adapter.add("lee yee chuan");
        adapter.add("lara");
        adapter.add("mario");

        this.setListAdapter(adapter);
    }

}
