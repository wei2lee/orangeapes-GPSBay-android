package com.tiny.gpsbay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by leeyeechuan on 6/21/15.
 */
public class TextItemListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    public JSONArray mJsonArray;
    public BitmapDownscale downscale;

    public TextItemListAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mJsonArray = new JSONArray();
    }


    @Override
    public int getCount() {
        return mJsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return mJsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.cell_textitem, parent, false);
            holder = new ViewHolder(convertView, mContext, downscale);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        JSONObject data = getItem(position);
        holder.update(data);
        return convertView;
    }


    public void updateData(JSONArray mJsonArray) {
        this.mJsonArray = mJsonArray;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        @InjectView(R.id.text_title)
        TextView text_title;

        @InjectView(R.id.text_desc)
        TextView text_desc;

        Transformation bitmapTransformation;
        JSONObject data;

        Context context;

        public ViewHolder(View view, Context context, Transformation bitmapTransformation) {
            ButterKnife.inject(this, view);
            this.context = context;
            this.bitmapTransformation = bitmapTransformation;
        }

        public void update(JSONObject data) {
            text_title.setText(data.optString("title"));
            text_desc.setText(data.optString("description"));
        }

    }
}
