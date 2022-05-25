package com.example.maps2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Projection projection;
    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        root = findViewById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng mohamedia = new LatLng(33.7066, -7.3944);
        mMap.addMarker(new MarkerOptions().position(mohamedia).title("Marker in Mohamedia"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mohamedia));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mohamedia,12));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            final GoogleMap gmap=mMap;
            @Override
            public void onMapClick(LatLng latLng) {
                gmap.addMarker(new MarkerOptions().position(latLng));
                getAddress(latLng);
            }
        });


    }

    private void getAddress(LatLng latLng){

        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            String locality = addresses.get(0).getLocality();
            show(locality,latLng);
        }
    }

    private void show(String query, final LatLng latLng){
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="http://api.openweathermap.org/data/2.5/weather?q="
                +query+"&appid=e457293228d5e1465f30bcbe1aea456b";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {

                    JSONObject jsonObject=new JSONObject(response);
                    Date date=new Date(jsonObject.getLong("dt")*1000);
                    SimpleDateFormat simpleDateFormat=
                            new SimpleDateFormat("dd-MMM-yyyy' T 'HH:mm");
                    String dateString=simpleDateFormat.format(date);
                    JSONObject main=jsonObject.getJSONObject("main");
                    JSONArray weather=jsonObject.getJSONArray("weather");
                    String meteo=weather.getJSONObject(0).getString("main");
                    int Temp=(int)(main.getDouble("temp")-273.15);
                    showMsg(Temp+"°C",latLng);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.i("MyLog","-------Connection problem-------------------");
                        Toast.makeText(MapsActivity.this,
                                "City not fond",Toast.LENGTH_LONG).show();


                    }
                });

        queue.add(stringRequest);
    }

    private void showMsg(String meteo,LatLng latLng){
        final LinearLayout v = new LinearLayout(MapsActivity.this);
        projection = mMap.getProjection();
        Point p =  projection.toScreenLocation(latLng);
        v.setX(p.x);
        v.setY(p.y);
        v.setLayoutParams(new LinearLayout.LayoutParams(10,10));

        // create view
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View tooltipView = layoutInflater.inflate(R.layout.simple_map_item,root,false);

        root.addView(v);
        new SimpleTooltip.Builder(this)
                .anchorView(v)
                .text(meteo)
                .contentView(tooltipView,R.id.tmp)
                .gravity(Gravity.BOTTOM)
                .animated(true)
                .transparentOverlay(false)
                .onDismissListener(new SimpleTooltip.OnDismissListener() {
                    @Override
                    public void onDismiss(SimpleTooltip tooltip) {
                        root.removeView(v);
                    }
                })
                .build()
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
