package com.example.map;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//import com.google.android.libraries.places.compat.Place; // NEW,

//import com.google.android.gms.places.api.Place;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    String API_KEY = "MY_API_KEY";

    private static GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    ProgressBar progressBar;
    ImageView mag_icon;
    TextView search_view;
    Button search_btn;
    ImageView clearView;
    Button done_btn;
    Button btn_settings;

    LinearLayout lin_v_layout;

    LocationManager locationManager;
    Context mContext;
    GetCurrentLocation mListen;
    Boolean isStopped = false;

    static List<String> PLACE_ID_LIST = new ArrayList<>();
    static List<String> CHECKED_INTERESTS_LIST = new ArrayList<>();

    static Boolean isOnLoad = true;

    SparseArray<Object> interest_map;

    int RADIUS = 1000;
    int count = 1;




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
       // setContentView(R.layout.interests_layout);
        StartUpRoutine();

        Button done_btn = findViewById(R.id.btn_done);
        done_btn.setOnClickListener(this);

        SetCheckBoxListeners();


        // parse this -> https://maps.googleapis.com/maps/api/place/search/xml?location=9.934866,76.267235&radius=50000&types=country%7Cairport%7Camusement_park%7Cbank%7Cbook_store%7Cbus_station%7Ccafe%7Ccar_rental%7Ccar_repair%7Cchurch%7Cdoctor%7Cfire_station%7Cfood%7Chindu_temple%7Chospital%7Clawyer%7Clibrary%7Cmosque%7Cmuseum%7Cpark%7Cparking%7Cpharmacy%7Cpolice%7Cpost_office%7Crestaurant%7Cschool%7Ctrain_station%7Czoo&sensor=true&key=your_API_Key

        // search by key words: https://maps.googleapis.com/maps/api/place/textsearch/xml?query=restaurants+in+Sydney&key=API_KEY

        // search by keyword with radius and lat/long-> https://maps.googleapis.com/maps/api/place/textsearch/xml?location=30.332184,-81.655647&query=bar+grill&key=API_KEY

        // search by place_id https://maps.googleapis.com/maps/api/place/details/json?place_id=ChIJN1t_tDeuEmsRUsoyG83frY4&fields=name,rating,formatted_phone_number&key=API_KEY
    }

    private void SetCheckBoxListeners() {
        // add listeners to checkboxes
        mContext = this;
        int number_of_checkboxes = 13;
        for (int i = 0; i <= number_of_checkboxes; i++) {
            String ch_x_id = "checkBox" + i;
            Log.d("ch", ch_x_id);
            int ch_x = getResources().getIdentifier(ch_x_id, "id", getPackageName()); //get id number by string id
            CheckBox box = findViewById(ch_x);
            box.setOnClickListener(this);
            Typeface typeface = ResourcesCompat.getFont(mContext, R.font.skinny_bold);
            box.setTypeface(typeface);


        }

    }

    private void StartUpRoutine() {
       // setContentView(R.layout.activity_maps);
        mContext = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageView refresh_button = findViewById(R.id.refresh_btn);
        refresh_button.setOnClickListener(this);

        ImageView interests_btn = findViewById(R.id.interest_icon);
        interests_btn.setOnClickListener(this);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Button btn1 = findViewById(R.id.button1);
        btn1.setVisibility(View.GONE);


        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);

//        search_view = findViewById(R.id.search_view);
//        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.luna);
//        search_view.setTypeface(typeface);



        clearView = findViewById(R.id.clear_x);
        clearView.setOnClickListener(this);

        btn_settings = findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(this);

        lin_v_layout = findViewById(R.id.lin_v);


        // SET FONTS
        TextView tv_string = findViewById(R.id.textview_string);
        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.sb);
        tv_string.setTypeface(typeface);
        TextView textView = findViewById(R.id.textView);
        typeface = ResourcesCompat.getFont(mContext, R.font.sb);
        textView.setTypeface(typeface);


//        refresh_button.setX(30);
//        refresh_button.setY(1025);

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(50);
        seekBar.setProgress(1);
        seekBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekBar.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                TextView tv = findViewById(R.id.textView);
                if (progress == 0) {
                    tv.setText("Radius: " + 500 + " Meters"); //set progress to 500
                    RADIUS = 500;
                } else {
                    tv.setText("Radius: " + progress * 1000 + " Meters");
                    RADIUS = progress * 1000;
                }

            }
        });
        Log.d("prog", String.valueOf(seekBar.getProgress()));
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d("onready", "ready");
        mMap = googleMap;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        mMap.setMyLocationEnabled(true);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        googleMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));



        // USE CURRENT LOCATION FOR THINGS
        mListen = new GetCurrentLocation(this);
        startGettingLocation();


        mag_icon = findViewById(R.id.mag_icon);
        mag_icon.setOnClickListener(this);

        search_view = findViewById(R.id.search_view);
        search_view.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.jordyblue, null), PorterDuff.Mode.SRC_ATOP);

        search_btn = findViewById(R.id.search_btn);

        // multiline snippets
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });


    }

    private StringBuilder CreateSearchString() {
        StringBuilder search_str = new StringBuilder();
        for (String string : CHECKED_INTERESTS_LIST) {
            search_str.append(string).append("|");
        }

        search_str.setLength(search_str.length() - 1);

        Log.d("str", String.valueOf(search_str));
        return search_str;
    }

    private void SetMarkers(double my_lat, double my_lng, String type_of_search) {

        PLACE_ID_LIST.clear();
        String search_string;
        String url;
        if (type_of_search.equals("OnLocationChanged") && !CHECKED_INTERESTS_LIST.isEmpty()){
            search_string = String.valueOf(CreateSearchString());
            url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + search_string + "&location=" + my_lat + "%2C" + my_lng + "&radius=" + RADIUS + "&key=" + API_KEY;
        }else if (type_of_search.equals("Keywords")){
            String search_for = ((TextView)search_view).getText().toString();
            url = "https://maps.googleapis.com/maps/api/place/textsearch/json?location=" + my_lat + "," + my_lng + "&query=" + search_for + "&key=" + API_KEY;
        }
        else{
            LatLng my_loctr = new LatLng(my_lat, my_lng);
            float zoomLevel = 15.0f; //up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(my_loctr, zoomLevel));
            isOnLoad = false;
            return;
        }

        String json = getJSON(url);
        Log.d("url1", url);

        JSONObject obj;
        try {
            obj = new JSONObject(json);
            JSONArray results_arr = obj.getJSONArray("results");
            // Log.d("results", String.valueOf(results_arr));
            // Log.d("results", String.valueOf(results_arr.length()));
            createPlaceIdList(obj);
            LatLng firstLctInList = null;

            for (int i = 0; i < PLACE_ID_LIST.size(); i++) {
                String place_id = PLACE_ID_LIST.get(i);
                url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=" + place_id + "&fields=opening_hours,vicinity,icon,geometry,name,rating,formatted_phone_number&key=" + API_KEY;
                json = getJSON(url);
                Log.d("url", url);

                obj = new JSONObject(json);
                JSONObject results_obj = obj.getJSONObject("result");
                Log.d("error", String.valueOf(results_obj));

                double place_lng = Double.valueOf(results_obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                double place_lat = Double.parseDouble(results_obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                String name_of_place = results_obj.getString("name");
                String rating = GetRating(results_obj);
                String vicinity = GetVicinity(results_obj);
                String isOpen = GetIsOpen(results_obj);
                //  String icon_url = results_obj.getString("icon");

                Bitmap image_item = null;
//                try {
//                    URL bm_url = new URL(icon_url);
//                    image_item = BitmapFactory.decodeStream(bm_url.openConnection().getInputStream());
//                } catch (IOException e) {
//                   // System.out.println(e);
//                }

                LatLng loctr = new LatLng(place_lat, place_lng);
                if (i==0){
                    firstLctInList = loctr;
                }

//                if (image_item != null) {
////                    Marker marker = mMap.addMarker(new MarkerOptions().position(loctr).title(name_of_place).snippet("1"+"\n"+"Rating: "+"\n"+"3").icon(BitmapDescriptorFactory.fromBitmap(image_item)));
////                    marker.showInfoWindow();
//                } else {
                if (PLACE_ID_LIST.size() == 1){
                    Marker marker = mMap.addMarker(new MarkerOptions().position(loctr).title(name_of_place).snippet("Rating: " + rating + "\n" + vicinity + "\n" + isOpen));
                    marker.showInfoWindow();
                    marker.setTag(place_id);
                    float zoomLevel = 15.0f; //up to 21
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loctr, zoomLevel));
                }else{
                    Marker marker = mMap.addMarker(new MarkerOptions().position(loctr).title(name_of_place).snippet("Rating: " + rating + "\n" + vicinity + "\n" + isOpen));
                    marker.showInfoWindow();
                    marker.setTag(place_id);
                    float zoomLevel = 15.0f; //up to 21
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLctInList, zoomLevel));
                }

                //      }

            }


            // zoom into my location
            if (isOnLoad) {
                LatLng my_loctr = new LatLng(my_lat, my_lng);
                float zoomLevel = 15.0f; //up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(my_loctr, zoomLevel));
                isOnLoad = false;
                Log.d("zoom", "zooming in");
            }


        } catch (JSONException e) {
            Log.d("error", "error");
            e.printStackTrace();
        }
    }

    private String GetIsOpen(JSONObject results_obj) {
        try {
            String opening_hours = results_obj.getJSONObject("opening_hours").getString("open_now");
            String is_open;
            if (opening_hours.equals("true")) {
                is_open = "Open Now";
            } else {
                is_open = "Closed";
            }
            return is_open;
        } catch (Exception e) {
            //pass
        }
        return "";
    }

    private String GetVicinity(JSONObject results_obj) {
        try {
            String vicinity = results_obj.getString("vicinity");
            return vicinity;
        } catch (Exception e) {
            //pass
        }
        return "";
    }

    private String GetRating(JSONObject results_obj) {
        try {
            String rating = results_obj.getString("rating");
            return rating;
        } catch (Exception e) {
            //pass
        }
        return "-";
    }


    private static void createPlaceIdList(JSONObject obj) {
        try {
            //  Log.d("url", json);
            //  JSONObject obj = new JSONObject(json);
            JSONArray results_arr = obj.getJSONArray("results");


            final int n = results_arr.length();
            for (int i = 0; i < n; ++i) {
                String place_id = results_arr.getJSONObject(i).getString("place_id");
                PLACE_ID_LIST.add(place_id);

                Log.d("ids", place_id);
                // title0.setText(movie.getString("Title"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getJSON(String url) {
        HttpsURLConnection con = null;
        try {
            URL u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();

            con.connect();


            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();


        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        double lat;
        double longi;
        LatLng latLng;
        String type_of_search;
        CheckBox box;
        ImageView clear_view;
        Location location;
        switch (v.getId()) {
            case R.id.refresh_btn: // the start searching button on the start-up page
                //  Button refrsh_btn = findViewById(R.id.refresh_btn);
                //  refrsh_btn.setText("Refreshing");
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                MyTask myTask = new MyTask();
                myTask.isRunning = true;
                myTask.execute(15);
                mMap.clear();
                //  stopGettingLocation();
                //  startGettingLocation();
                Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_SHORT).show();

                // LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //  Criteria criteria = new Criteria();
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                location  = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                lat = location.getLatitude();
                longi = location.getLongitude();
                latLng = new LatLng(lat, longi);
                type_of_search = "OnLocationChanged";
                Log.d("rad", String.valueOf(RADIUS));
                SetMarkers(lat, longi, type_of_search);
                myTask.isRunning = false;
                break;
            case R.id.btn_done:
                // hide sv
                LinearLayout linearLayout = findViewById(R.id.ll1);
                linearLayout.setVisibility(View.INVISIBLE);

                // LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //  Criteria criteria = new Criteria();
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                lat = location.getLatitude();
                longi = location.getLongitude();
                latLng = new LatLng(lat, longi);
                type_of_search = "OnLocationChanged";
                Log.d("rad", String.valueOf(RADIUS));
                SetMarkers(lat, longi, type_of_search);
                break;
            case R.id.btn_settings:
                lin_v_layout.setVisibility(View.VISIBLE);
                break;
            case R.id.interest_icon:
              //  setContentView(R.layout.interests_layout);
                linearLayout = findViewById(R.id.ll1);
                linearLayout.setVisibility(View.VISIBLE);
                done_btn = findViewById(R.id.btn_done);
                done_btn.setOnClickListener(this);
                search_btn.setVisibility(View.INVISIBLE);
                search_view.setVisibility(View.INVISIBLE);
                clearView.setVisibility(View.INVISIBLE);
                //  SetCheckBoxListeners();
                break;
            case R.id.mag_icon:
                Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                if(search_view.getVisibility()==View.INVISIBLE){
                    search_btn.startAnimation(slideUp);
                    search_view.startAnimation(slideUp);
                    search_btn.setVisibility(View.VISIBLE);
                    search_view.setVisibility(View.VISIBLE);
                    clearView.startAnimation(slideUp);
                    clearView.setVisibility(View.VISIBLE);
                }
                search_view.startAnimation(slideUp);
                search_btn.startAnimation(slideUp);
                clearView.startAnimation(slideUp);
                clearView.setVisibility(View.VISIBLE);
                break;
            case R.id.clear_x:
                Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                search_view.startAnimation(slideDown);
                search_btn.startAnimation(slideDown);
                clearView.startAnimation(slideDown);
                clearView.setVisibility(View.INVISIBLE);
                search_view.setVisibility(View.INVISIBLE);
                search_btn.setVisibility(View.INVISIBLE);
                break;
            case R.id.search_btn:
//                TextView loading_view = findViewById(R.id.loading_view);
//                loading_view.setVisibility(View.VISIBLE);
                Log.d("why", "clicked");
                Toast.makeText(mContext, "!!!!!!", Toast.LENGTH_LONG).show();
//                progressBar.setVisibility(View.VISIBLE);
//                progressBar.setProgress(0);
//                MyTask myTask = new MyTask();
//                myTask.isRunning = true;
//                myTask.execute(15);
                // HIDES KEYBOARD AFTER CLICK OF BUTTON
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                clear_view = findViewById(R.id.clear_x);
                clear_view.setVisibility(View.INVISIBLE);

                search_btn.setVisibility(View.INVISIBLE);
                search_view.setVisibility(View.INVISIBLE);

                //  LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //  Criteria criteria = new Criteria();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                lat = location.getLatitude();
                longi = location.getLongitude();
//               LatLng latLng = new LatLng(lat, longi);
                type_of_search = "Keywords";

                SetMarkers(lat, longi, type_of_search);
                // myTask.isRunning = false;
                break;
            case R.id.checkBox0:
                box = findViewById(R.id.checkBox0);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("airport");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("airport");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox1:
                box = findViewById(R.id.checkBox1);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("amusement_park");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("amusement_park");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox2:
                box = findViewById(R.id.checkBox2);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("aquarium");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("aquarium");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox3:
                box = findViewById(R.id.checkBox3);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("art_gallery");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("art_gallery");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox4:
                box = findViewById(R.id.checkBox4);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("atm");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("atm");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox5:
                box = findViewById(R.id.checkBox5);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("bakery");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("bakery");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox6:
                box = findViewById(R.id.checkBox6);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("bank");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("bank");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox7:
                box = findViewById(R.id.checkBox7);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("bar");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("bar");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox8:
                box = findViewById(R.id.checkBox8);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("beauty_salon");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("beauty_salon");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox9:
                box = findViewById(R.id.checkBox9);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("bicycle_store");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("bicycle_store");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox10:
                box = findViewById(R.id.checkBox10);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("book_store");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("book_store");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox11:
                box = findViewById(R.id.checkBox11);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("bowling_alley");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("bowling_alley");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.checkBox12:
                box = findViewById(R.id.checkBox12);
                if (box.isChecked()) {
                    CHECKED_INTERESTS_LIST.add("cafe");
                    Toast.makeText(getApplicationContext(), String.valueOf("added"), Toast.LENGTH_SHORT).show();

                } else {
                    CHECKED_INTERESTS_LIST.remove("cafe");
                    Toast.makeText(getApplicationContext(), String.valueOf("removed"), Toast.LENGTH_SHORT).show();

                }
                break;


        }
    }

    private void startGettingLocation() {
        mListen.startGettingLocation(new GetCurrentLocation.getLocation() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("tag", String.valueOf(location));

                //  Toast.makeText(mContext, "new location--setting markers", Toast.LENGTH_LONG).show();
                //mMap.clear(); // clears out all markers

                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "msg", Toast.LENGTH_SHORT).show();
                    }
                });
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                MyTask myTask = new MyTask();
                myTask.isRunning = true;
                myTask.execute(15); //keep
                String type_of_search = "OnLocationChanged";
                SetMarkers(latitude, longitude, type_of_search);
                myTask.isRunning = false;
                Log.d("updating", "new location");
            }

        });
    }

    public void stopGettingLocation() {
        mListen.stopGettingLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("tag", "clicked");
        // View view = findViewById(R.id.view);
        //  view.setVisibility(View.VISIBLE);
        //  stopGettingLocation();



        Toast.makeText(getApplicationContext(), String.valueOf(marker.getTag()), Toast.LENGTH_SHORT).show();


        return false;
    }


    class MyTask extends AsyncTask<Integer, Integer, String> {
        Boolean isRunning = false;
        @Override
        protected String doInBackground(Integer... params) {
            while (isRunning) {
                for (; count <= params[0]; count++) {
                    if (!isRunning) break;
                    try {
                        Thread.sleep(1000);
                        publishProgress(count);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return PLACE_ID_LIST.size() + " Results Found";
            }
            return PLACE_ID_LIST.size() + " Results Found";
        }
        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            cancel(true);
            //  txtJson.setText(result);
            //  btnHit.setText("Search Again");
        }
        @Override
        protected void onPreExecute() {
            // txtJson.setText("Task Starting...");
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            //  txtJson.setText("Running..."+ values[0]);
            progressBar.setProgress(values[0]);

        }

    }




}

class GetCurrentLocation implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "location-updates-sample";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 300000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final String LOCATION_KEY = "location-key";
    private final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Context mContext;
    private getLocation mGetCurrentLocation;

    public GetCurrentLocation(Context context) {
        mContext = context;

        buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        createLocationRequest();
    }

    public interface getLocation {
        public void onLocationChanged(Location location);
    }

    public void startGettingLocation(getLocation location) {
        mGetCurrentLocation = location;
        connect();
    }

    public void stopGettingLocation() {
        stopLocationUpdates();
        disconnect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    private void connect() {
        mGoogleApiClient.connect();
    }

    private void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        startLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        mGetCurrentLocation.onLocationChanged(location);
        Toast.makeText(mContext, "this message", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
}

