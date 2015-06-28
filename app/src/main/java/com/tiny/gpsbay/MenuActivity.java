package com.tiny.gpsbay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by leeyeechuan on 6/20/15.
 */



public class MenuActivity extends Activity implements View.OnClickListener{

    @InjectView(R.id.btn_gps)
    View btn_gps;

    @InjectView(R.id.btn_place)
    View btn_place;

    @InjectView(R.id.btn_hotel)
    View btn_hotel;

    @InjectView(R.id.btn_airport)
    View btn_airport;

    @InjectView(R.id.btn_travel)
    View btn_travel;

    @InjectView(R.id.btn_bahasa)
    View btn_bahasa;

    @InjectView(R.id.btn_contact)
    View btn_contact;

    @InjectView(R.id.btn_about)
    View btn_about;

    List<MenuBtn> menubtns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.inject(this);
        createMenuBtns();
    }

    void createMenuBtns() {
        MenuBtn menubtn;

        menubtns = new ArrayList<MenuBtn>();

        menubtn = new MenuBtn(btn_gps, this);
        menubtn.text_title.setText(getString(R.string.menubtn_gps_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_gps_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_gps_bg));
        menubtn.name = "gps";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_place, this);
        menubtn.text_title.setText(getString(R.string.menubtn_place_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_place_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_place_bg));
        menubtn.name = "place";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_hotel, this);
        menubtn.text_title.setText(getString(R.string.menubtn_hotel_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_hotel_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_hotel_bg));
        menubtn.name = "hotel";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_airport, this);
        menubtn.text_title.setText(getString(R.string.menubtn_airport_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_airport_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_airport_bg));
        menubtn.name = "airport";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_travel, this);
        menubtn.text_title.setText(getString(R.string.menubtn_travel_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_travel_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_travel_bg));
        menubtn.name = "travel";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_bahasa, this);
        menubtn.text_title.setText(getString(R.string.menubtn_bahasa_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_bahasa_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_bahasa_bg));
        menubtn.name = "bahasa";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_contact, this);
        menubtn.text_title.setText(getString(R.string.menubtn_contact_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_contact_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_contact_bg));
        menubtn.name = "contact";
        menubtns.add(menubtn);

        menubtn = new MenuBtn(btn_about, this);
        menubtn.text_title.setText(getString(R.string.menubtn_about_title));
        menubtn.text_desc.setText(getString(R.string.menubtn_about_desc));
        menubtn.view_body.setBackgroundColor(getResources().getColor(R.color.menubtn_about_bg));
        menubtn.name = "about";
        menubtns.add(menubtn);

        for(MenuBtn mb : menubtns) {
            mb.view_body.setOnClickListener(this);
        }
    }



    @Override
    public void onClick(View v) {
        MenuBtn menuBtn = null;
        for(MenuBtn mb : menubtns) if(mb.isClicked(v)) menuBtn = mb;
        if(menuBtn != null) {
            if(menuBtn.name == "gps") {
                Intent i = new Intent(this, MapActivity.class);
                startActivity(i);
            } if(menuBtn.name == "place") {
                Intent i = new Intent(this, BigItemListActivity.class);
                i.putExtra("source", R.raw.place);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_place_title));
                i.putExtra("subtitle", getString(R.string.menubtn_place_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_place_bg));
                startActivity(i);
            }else if(menuBtn.name == "hotel") {
                Intent i = new Intent(this, BigItemListActivity.class);
                i.putExtra("source", R.raw.hotel);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_hotel_title));
                i.putExtra("subtitle", getString(R.string.menubtn_hotel_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_hotel_bg));
                startActivity(i);
            }else if(menuBtn.name == "airport") {
                Intent i = new Intent(this, BigItemListActivity.class);
                i.putExtra("source", R.raw.airport);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_airport_title));
                i.putExtra("subtitle", getString(R.string.menubtn_airport_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_airport_bg));
                startActivity(i);
            }else if(menuBtn.name == "travel") {
                Intent i = new Intent(this, ImageListActivity.class);
                i.putExtra("source", R.raw.travel);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_travel_title));
                i.putExtra("subtitle", getString(R.string.menubtn_travel_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_travel_bg));
                startActivity(i);
            }else if(menuBtn.name == "bahasa") {
                Intent i = new Intent(this, ImageListActivity.class);
                i.putExtra("source", R.raw.bahasa);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_bahasa_title));
                i.putExtra("subtitle", getString(R.string.menubtn_bahasa_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_bahasa_bg));
                startActivity(i);
            }else if(menuBtn.name == "contact") {
                Intent i = new Intent(this, TextItemListActivity.class);
                i.putExtra("source", R.raw.contact);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_contact_title));
                i.putExtra("subtitle", getString(R.string.menubtn_contact_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_contact_bg));
                startActivity(i);
            }else if(menuBtn.name == "about") {
                Intent i = new Intent(this, AboutActivity.class);
                i.putExtra("navigationbar_title", getString(R.string.menubtn_about_title));
                i.putExtra("subtitle", getString(R.string.menubtn_about_desc));
                i.putExtra("color", getResources().getColor(R.color.menubtn_about_bg));
                startActivity(i);
            }
        }
    }


    static class MenuBtn {
        @InjectView(R.id.text_title)
        TextView text_title;

        @InjectView(R.id.text_desc)
        TextView text_desc;

        @InjectView(R.id.view_body)
        View view_body;

        public String name;

        public MenuBtn(View view, Context context) {
            ButterKnife.inject(this, view);
        }

        public void installClick(View.OnClickListener l) {
            view_body.setOnClickListener(l);
        }
        public boolean isClicked(View v) {
            return v == view_body;
        }
    }
}
