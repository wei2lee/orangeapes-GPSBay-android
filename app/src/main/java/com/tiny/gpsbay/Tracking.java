package com.tiny.gpsbay;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("Tracking")
public class Tracking extends ParseObject {
    public int getType() {
        return getInt("type");
    }

    public void setType(int value) {
        put("type", value);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<Tracking> getQuery() {
        ParseQuery<Tracking> query = ParseQuery.getQuery(Tracking.class);
        query.orderByDescending("updatedAt");
        query.whereNotEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(1);
        return query;
    }
}
