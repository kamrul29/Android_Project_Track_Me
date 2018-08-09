package com.kamruzzaman.trackme;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double toLongitude, toLatitude, fromLongitude, fromLatitude;
    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            toLongitude = getIntent().getDoubleExtra("toLongitude", 0);
            toLatitude = getIntent().getDoubleExtra("toLatitude", 0);
            fromLatitude = getIntent().getDoubleExtra("fromLatitude", 0);
            fromLongitude = getIntent().getDoubleExtra("fromLongitude", 0);
            LatLng fromLatLng = new LatLng(fromLatitude,fromLongitude);
            LatLng toLatLng= new LatLng(toLatitude,toLongitude);
            drawRoute(fromLatLng,toLatLng);


        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
        LatLng placeToShow = new LatLng(fromLatitude, fromLongitude);
        mMap.addMarker(new MarkerOptions().position(placeToShow).title("From"));
        placeToShow = new LatLng(toLatitude, toLongitude);
        mMap.addMarker(new MarkerOptions().position(placeToShow).title("To"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeToShow));
        mMap.addPolyline(polylineOptions);
    }

    public void drawRoute(LatLng from, LatLng to) {
        polylineOptions = new PolylineOptions();
        polylineOptions.geodesic(true).add(from)
                .add(to);

    }


}
