package com.tiny.gpsbay;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

//import com.loopj.android.http.AsyncHttpClient;
//import com.loopj.android.http.JsonHttpResponseHandler;


/**
 * Created by leeyeechuan on 6/18/15.
 */



public class MapActivity extends Activity implements MapWrapperLayout.OnDragListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, CustomDialogOnClickListener, AdapterView.OnItemClickListener {
    static String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    static String LAST_UPDATED_TIME_STRING_KEY = "LAST_UPDATED_TIME_STRING_KEY";
    static String LOCATION_KEY = "LOCATION_KEY";
    static int PLACE_PICKER_REQUEST = 1001;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    @InjectView(R.id.view_navigationbar)
    View view_navigationbar;

    @InjectView(R.id.btn_locate)
    ImageButton btn_locate;

//    @InjectView(R.id.searchview)
//    SearchView searchview;
    PlaceAutocompleteAdapter placeAdapter;

    @InjectView(R.id.autocompleteview)
    AutoCompleteTextView mAutocompleteView;

    @InjectView(R.id.cyclist_label)
    ImageView cyclist_label;

    CustomMapFragment map_fragment;
    public View view;

//    @InjectView(R.id.progressbar)
//    SmoothProgressBar progressbar;

    TimerHandler updateTracking;

    GoogleApiClient mGoogleApiClient;
    Location mPrevLastLocation;
    Location mLastLocation;
    Location stableLastLocation;


    float lerp(float damp, float min, float max) {
        return damp * (max-min) + min;
    }
    float inverseLerp(float val, float min, float max) {
        if(max == min) return 0;
        float ret = (val-min) / (max-min);
        ret = Math.max(Math.min(1, ret), 0);
        return ret;
    }

    void setMLastLocation(Location l) {
        mPrevLastLocation = mLastLocation != null ? new Location(mLastLocation) : null;
        mLastLocation = l;
        if(stableLastLocation == null) {
            stableLastLocation = mLastLocation != null ? new Location(mLastLocation) : null;
        }else{
            float damp = inverseLerp(mLastLocation.getSpeed(), 1.5f, 5f);
            float bearingDelta = (mLastLocation.getBearing() - stableLastLocation.getBearing()) * damp;
            float bearing = bearingDelta + stableLastLocation.getBearing();

            Log.d(Application.APPTAG, String.format("setMLastLocation@damp=%.2f,beringDe=%.2f,bearing=%.2f", damp, bearingDelta, bearing));

            stableLastLocation.setBearing(bearing);

            stableLastLocation.setLatitude(mLastLocation.getLatitude());
            stableLastLocation.setLongitude(mLastLocation.getLongitude());
            stableLastLocation.setAccuracy(mLastLocation.getAccuracy());
            stableLastLocation.setSpeed(mLastLocation.getSpeed());
        }
    }
    Location getMLastLocation() {
        return mLastLocation;
    }


    connectAsyncTask getDirectionTask;
    DateTime mFirstUpdateTime;
    Location mCurrentLocation;

    String mLastUpdateTime;
    LocationRequest mLocationRequest;
    boolean startedLocationUpdate = false;
    boolean firedOnWithinVeryNearDistance;
    int startZoom = 17;
    Polyline routePolyline;
    List<Tracking> otherTrackings;
    Tracking tracking;
    HashMap<Tracking, Marker> trackingMarkerHashMap = new HashMap<Tracking, Marker>();

    BitmapDescriptor driverBitmapDescriptor;
    BitmapDescriptor cyclistBitmapDescriptor;

    boolean enabledCameraFollow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Application.APPTAG, "MapActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.inject(this);


        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(cyclist_label, View.ALPHA, 1f, 0f);
        fadeOut.setDuration(1);
        fadeOut.start();


        driverBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_cartopview_2);
        cyclistBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_cyclist);

        tracking = new Tracking();
        tracking.setUser(ParseUser.getCurrentUser());
        otherTrackings = new ArrayList<Tracking>();
        trackingMarkerHashMap = new HashMap<Tracking, Marker>();


        startZoom = getResources().getInteger(R.integer.map_start_zoom);
        getDirectionTask = null;
        view_navigationbar.setBackgroundColor(getResources().getColor(R.color.color_theme_bg_lightgreen));
        routePolyline = null;
        cyclistInRange = false;
        enabledCameraFollow = false;
        mLocationRequest = null;
        mLastLocation = null;
        stableLastLocation = null;
        mPrevLastLocation = null;
        mGoogleApiClient = null;
        firedOnWithinVeryNearDistance = false;
        startedLocationUpdate = false;
        onLocationAvailableAndTypeSelectedCondition = 0;
        onLocationAvailableAndMapReadyCondition = 0;
        map_fragment = (CustomMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map);
        map_fragment.getMapAsync(this);
        buildGoogleApiClient();
        //updateValuesFromBundle(savedInstanceState);

        placeAdapter = new PlaceAutocompleteAdapter(this, R.layout.cell_searchtext,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteView.setAdapter(placeAdapter);
        mAutocompleteView.setOnItemClickListener(this);

        showLoginPanel();
    }


    @Override
    protected void onPause() {
        Log.d(Application.APPTAG, "MapActivity.onPause");
        super.onPause();
        if(mGoogleApiClient != null) {
            stopLocationUpdates();
        }
        if(getDirectionTask != null) {
            getDirectionTask.cancel(true);
            getDirectionTask = null;
        }
    }

    @Override
    public void onResume() {
        Log.d(Application.APPTAG, "MapActivity.onResume");
        super.onResume();
        if(mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(Application.APPTAG, "MapActivity.onSaveInstanceState");
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(Application.APPTAG, "MapActivity.savedInstanceState");
        if (savedInstanceState != null) {

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            //updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d(Application.APPTAG, "MapActivity.buildGoogleApiClient");
        if(mGoogleApiClient != null)return;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        mGoogleApiClient.connect();

        Log.d(Application.APPTAG, "MapActivity.buildGoogleApiClient.end");



    }

    protected void createLocationRequest() {
        Log.d(Application.APPTAG, "MapActivity.createLocationRequest");
        if(mLocationRequest != null) return;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(getResources().getInteger(R.integer.tracking_update_interval));
        mLocationRequest.setFastestInterval(getResources().getInteger(R.integer.tracking_update_interval));
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d(Application.APPTAG, "MapActivity.startLocationUpdates");
        if(startedLocationUpdate == false) {
            startedLocationUpdate = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

            updateTracking = new TimerHandler(new Runnable() {

                @Override
                public void run() {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("type", tracking.getType());
                    map.put("location", tracking.getLocation());
                    ParseCloud.callFunctionInBackground("updateMyTracking", map, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object o, ParseException e) {
                            try {
                                if (o != null) {
                                    ArrayList<Tracking> array = (ArrayList<Tracking>) o;
                                    updateMarkers(array);
                                    updateCyclistLabel();
                                } else {
                                    Log.d(Application.APPTAG, "e = " + e);
                                }
                            }catch(Exception ex) {

                            }
                        }
                    });
                }
            }, getResources().getInteger(R.integer.trackings_query_interval));
        }
    }

    private void updateMarkers(ArrayList<Tracking> array) {
        if(map_fragment == null || map_fragment.getMap() == null) {
            Log.d(Application.APPTAG, "MapActivity.updateMarkers@ma_fragment or getMap is null");
            return;
        }

        for(Marker marker : trackingMarkerHashMap.values()) {
            marker.remove();
        }
        trackingMarkerHashMap.clear();
        otherTrackings = new ArrayList<Tracking>();
        for(Tracking ele : array) {
            if(ele != null && ele.getUser().getObjectId().equals(tracking.getUser().getObjectId())) {
                Marker marker = map_fragment.getMap().addMarker(new MarkerOptions()
                        .position(createLatLng(stableLastLocation))
                        .rotation(stableLastLocation.getBearing())
                        .flat(ele.getType() == 0)
                        .anchor(0.5f,0.5f)
                        .icon(ele.getType() == 0 ? driverBitmapDescriptor : cyclistBitmapDescriptor));
                trackingMarkerHashMap.put(ele, marker);
            }else if(ele != null && ele.getObjectId() != null && ele.getType() == 1){
                otherTrackings.add(ele);
                Marker marker = map_fragment.getMap().addMarker(new MarkerOptions()
                        .position(new LatLng(ele.getLocation().getLatitude(), ele.getLocation().getLongitude()))
                        .rotation(0)
                        .flat(ele.getType() == 0)
                        .anchor(0.5f, 0.5f)
                        .icon(ele.getType() == 0 ? driverBitmapDescriptor : cyclistBitmapDescriptor));
                trackingMarkerHashMap.put(ele, marker);
            }

        }


    }

    LatLng createLatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }


    boolean cyclistInRange = false;
    private AnimatorSet cyclist_label_animation;
    void setCyclistInRange(boolean b) {
        Log.d(Application.APPTAG, "MapActivity.setCyclistInRange val =" + b);
        if(b == cyclistInRange)return;

        boolean oldval = cyclistInRange;
        boolean newval = b;

        cyclistInRange = newval;

        Log.d(Application.APPTAG, "MapActivity.setCyclistInRange1");
        if(!oldval && newval) {
            MapActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");

                    Log.d(Application.APPTAG, "MapActivity.setCyclistInRange2");
                    if (tracking.getType() == 0) {

                        Log.d(Application.APPTAG, "MapActivity.setCyclistInRange3");

                        if (cyclist_label_animation != null) {
                            cyclist_label_animation.cancel();
                            cyclist_label_animation = null;
                        }
                        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(cyclist_label, View.ALPHA, 1f, 0f);
                        fadeOut.setDuration(500);
                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(cyclist_label, View.ALPHA, cyclist_label.getAlpha(), 1f);
                        fadeIn.setDuration(500);
                        cyclist_label_animation = new AnimatorSet();
                        cyclist_label_animation.play(fadeIn);
                        cyclist_label_animation.play(fadeOut).after(5000 + 500);
                        cyclist_label_animation.start();
                    }


                }
            });

        }
    }

    void updateCyclistLabel() {

        boolean inRange = false;
        for(Tracking ele : otherTrackings) {
            if(ele.getUser().getObjectId().equals(tracking.getUser().getObjectId())) {

            }else if(ele.getObjectId() != null){
                double d = ele.getLocation().distanceInKilometersTo(tracking.getLocation());
                Log.d(Application.APPTAG, "distance = " + d);
                if(d < 0.5f) {
                    inRange = true;
                    break;
                }
            }
        }
        Log.d(Application.APPTAG, "inRange=" + inRange);
        setCyclistInRange(inRange);
    }

    protected void stopLocationUpdates() {
        Log.d(Application.APPTAG, "MapActivity.stopLocationUpdates");
        if(startedLocationUpdate == true) {
            startedLocationUpdate = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

            updateTracking.timer.cancel();
            updateTracking = null;
        }
    }

    //////interface ConnectionCallbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Application.APPTAG, "MapActivity.onConnected");

        setMLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));



        if (getMLastLocation() != null) {
            mFirstUpdateTime = new DateTime();

            onLocationAvailableAndMapReadyCondition++;
            if(onLocationAvailableAndMapReadyCondition == 2) onLocationAvailableAndMapReady();
            onLocationAvailableAndTypeSelectedCondition++;
            if(onLocationAvailableAndTypeSelectedCondition == 2) onLocationAvailableAndTypeSelected();
            tracking.setLocation(new ParseGeoPoint(getMLastLocation().getLatitude(), getMLastLocation().getLongitude()));
        }
        createLocationRequest();
        startLocationUpdates();
    }


    int onLocationAvailableAndTypeSelectedCondition = 0;
    private void onLocationAvailableAndTypeSelected() {
        ArrayList<Tracking> array = new ArrayList<Tracking>();
        array.add(tracking);
        updateMarkers(array);
//        Marker marker = map_fragment.getMap().addMarker(new MarkerOptions()
//                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
//                .icon(tracking.getType() == 0 ? driverBitmapDescriptor : cyclistBitmapDescriptor));
//        trackingMarkerHashMap.put(tracking, marker);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Application.APPTAG, "MapActivity.onConnectionSuspended");
    }
    //////end interface ConnectionCallbacks

    //////interface OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(Application.APPTAG, "MapActivity.onConnectionFailed");
    }

    //////end interface OnConnectionFailedListener

    int onLocationChangedState = 0;
    //////interface LocationListener
    @Override
    public void onLocationChanged(Location location) {
        String toastText = String.format("%.3f,%.3f : acc=%.3f : b=%.3f : spd=%.3s", location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getBearing(), location.getSpeed());
        //Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        Log.d(Application.APPTAG, "MapActivity.onLocationChanged enabledCameraFollow=" + enabledCameraFollow + ",location=(" + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + ")");

        DateTime now = new DateTime();

        if(getMLastLocation() == null) {
            mFirstUpdateTime = new DateTime();

            setMLastLocation(location);
            onLocationAvailableAndMapReadyCondition++;
            if(onLocationAvailableAndMapReadyCondition == 2) onLocationAvailableAndMapReady();
            onLocationAvailableAndTypeSelectedCondition++;
            if(onLocationAvailableAndTypeSelectedCondition == 2) onLocationAvailableAndTypeSelected();

            tracking.setLocation(new ParseGeoPoint(getMLastLocation().getLatitude(), getMLastLocation().getLongitude()));
        }else if(location.getAccuracy() < getResources().getInteger(R.integer.map_min_accuracy_for_location_update) && now.compareTo(mFirstUpdateTime.plus(5)) > 0 && onLocationAvailableAndTypeSelectedCondition >= 2 && onLocationAvailableAndMapReadyCondition >= 2){
            setMLastLocation(location);
            map_fragment.setOnDragListener(this);
            tracking.setLocation(new ParseGeoPoint(getMLastLocation().getLatitude(), getMLastLocation().getLongitude()));
            if(enabledCameraFollow) {
                LatLng ll = new LatLng(getMLastLocation().getLatitude(), getMLastLocation().getLongitude());
                map_fragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(ll));
            }
        }


    }
    void onWithinVeryNearDistance() {
        LatLng ll = new LatLng(getMLastLocation().getLatitude(), getMLastLocation().getLongitude());
        map_fragment.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(ll, startZoom));
    }
    //////end interface LocationListener


    //////interface OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(Application.APPTAG, "MapActivity.onMapReady");
        UiSettings setting = map_fragment.getMap().getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);

        onLocationAvailableAndMapReadyCondition++;
        if(onLocationAvailableAndMapReadyCondition == 2) onLocationAvailableAndMapReady();
    }
    //////end interface OnMapReadyCallback

    //////MapWrapperLayout.OnDragListener
    @Override
    public void onDrag(MotionEvent motionEvent) {
        enabledCameraFollow = false;
        //searchview.clearFocus();
        //mAutocompleteView.clearFocus();
        //mAutocompleteView.dismissDropDown();
        if(mAutocompleteView != null) {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(mAutocompleteView.getWindowToken(), 0);
        }
    }
    //////end - MapWrapperLayout.OnDragListener


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //if(view == mAutocompleteView) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = placeAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(Application.APPTAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + item.description,
                    Toast.LENGTH_SHORT).show();
            Log.i(Application.APPTAG, "Called getPlaceById to get Place details for " + item.placeId);



        //}
    }

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            Log.d(Application.APPTAG, ""+places);
            try {
                if (places.getCount() > 0) {
                    LatLng to = places.get(0).getLatLng();
                    LatLng from = createLatLng(mLastLocation);
                    String urlTopass = makeURL(
                            from.latitude, from.longitude,
                            to.latitude, to.longitude);
                    getDirectionTask = new connectAsyncTask(urlTopass);
                    getDirectionTask.execute();
                }
            }finally {
                places.release();
            }
        }
    };

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
//        private ProgressDialog progressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
//            progressDialog = new ProgressDialog(getBaseContext());
//            progressDialog.setMessage("Fetching route, Please wait...");
//            progressDialog.setIndeterminate(true);
//            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            progressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }

    public void drawPath(String result) {
        try {
            if(routePolyline != null) {
                routePolyline.remove();
            }

            // Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> waypoints = PolyUtil.decode(encodedString);
            routePolyline = map_fragment.getMap().addPolyline(
                    new PolylineOptions()
                            .color(getResources().getColor(R.color.color_map_route))
                            .width(getResources().getInteger(R.integer.map_route_width))
                            .addAll(waypoints));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class JSONParser {

        InputStream is = null;
        JSONObject jObj = null;
        String json = "";

        // constructor
        public JSONParser() {
        }

        public String getJSONFromUrl(String url) {

            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            return json;

        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        return urlString.toString();
    }


    //////CustomDialogOnClickListener
    public void onCustomDialogClick(String name) {
        if(name.equals("driver")) {
            tracking.setType(0);
        }else{
            tracking.setType(1);
        }
        onLocationAvailableAndTypeSelectedCondition++;
        if(onLocationAvailableAndTypeSelectedCondition == 2) onLocationAvailableAndTypeSelected();
    }
    //////end CustomDialogOnClickListener

    @OnClick(R.id.btn_locate)
    void onBtnLocateClick(View v) {
        enabledCameraFollow = true;

        LatLng ll = new LatLng(getMLastLocation().getLatitude(), getMLastLocation().getLongitude());
        map_fragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(ll));
    }



    int onLocationAvailableAndMapReadyCondition = 0;
    void onLocationAvailableAndMapReady() {
        LatLng ll = new LatLng(getMLastLocation().getLatitude(), getMLastLocation().getLongitude());
        map_fragment.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(ll, startZoom));
    }

    private void showLoginPanel() {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.customDialogOnClickListener = this;
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlacePicker.getPlace(data, this);
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
//            }
//        }
    }


    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        marker.setPosition(toPosition);

        return;
//        final Handler handler = new Handler();
//        final long start = SystemClock.uptimeMillis();
//        Projection proj = map_fragment.getMap().getProjection();
//        Point startPoint = proj.toScreenLocation(marker.getPosition());
//        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
//        final long duration = 500;
//
//        final LinearInterpolator interpolator = new LinearInterpolator();
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long elapsed = SystemClock.uptimeMillis() - start;
//                float t = interpolator.getInterpolation((float) elapsed
//                        / duration);
//                double lng = t * toPosition.longitude + (1 - t)
//                        * startLatLng.longitude;
//                double lat = t * toPosition.latitude + (1 - t)
//                        * startLatLng.latitude;
//                marker.setPosition(new LatLng(lat, lng));
//
//                if (t < 1.0) {
//                    // Post again 16ms later.
//                    handler.postDelayed(this, 16);
//                } else {
//                    if (hideMarker) {
//                        marker.setVisible(false);
//                    } else {
//                        marker.setVisible(true);
//                    }
//                }
//            }
//        });
    }

}

interface CustomDialogOnClickListener {
    public void onCustomDialogClick(String name);
}

class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public View driver, cyclist;
    public CustomDialogOnClickListener customDialogOnClickListener;

    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_panel);
        driver = (View) findViewById(R.id.btn_driver);
        cyclist = (View) findViewById(R.id.btn_cyclist);
        driver.setOnClickListener(this);
        cyclist.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_driver:
                if(customDialogOnClickListener != null) customDialogOnClickListener.onCustomDialogClick("driver");
                dismiss();
                break;
            case R.id.btn_cyclist:
                if(customDialogOnClickListener != null) customDialogOnClickListener.onCustomDialogClick("cyclist");
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}