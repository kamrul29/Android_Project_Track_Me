package com.kamruzzaman.trackme;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView signOutButton;
    private Button signInButton;
    private Context context;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String loggedInUserID;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static Location userlocation;
    private Map<String,Boolean> requests;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        initialize();

    }

    public void initialize() {
        context = UserListActivity.this;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        signOutButton = findViewById(R.id.sign_out_button);

        requests = new HashMap<>();

        firebaseAuth = FirebaseAuth.getInstance();
        loggedInUserID = firebaseAuth.getCurrentUser().getUid();
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(context)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                initSignInButton();
                            }
                        });


            }
        });

        checkForNotification();


    }

    private void checkForNotification() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("requests");
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final Request request = child.getValue(Request.class);
                    if (request.toUser.equals(loggedInUserID)) {
                        DatabaseReference databaseReferenceForUserDetail = firebaseDatabase.getReference().child("users");
                        databaseReferenceForUserDetail.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    User user = child.getValue(User.class);
                                    if(user.getUserId().equals(request.fromUser))
                                    {
                                        if(requests.get(request.getRequestID())==null)
                                        {

                                            showAlert("You have a Location Request!",user.getName()+" Wants to show your location",request);
                                            requests.put(request.getRequestID(),true);
                                        }

                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
//                        Toast.makeText(context, "You Have a New Location Request", Toast.LENGTH_SHORT).show();

                        break;
                    }
                    else if(request.getRequestID().equals(RequestCode.lastRequestID))
                    {
                        double toLatitude = request.getToLatitude();
                        double toLongitude = request.getToLongitude();
                        if(toLatitude!=-1 && toLongitude!=-1){
                            {
                                showRequestedLocation("Location Request Accepted!","Longitude : "+toLongitude+" Latitude : "+toLatitude,toLatitude,toLongitude,request);
                            }
                        }
                        else
                        {
                            Toast.makeText(context,"Location Could not detected",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });
    }

    private void showAlert(String title, String message, final Request request) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(getLocation().getLongitude()!=-1 && getLocation().getLatitude()!=-1)
                        {
                            request.setToLatitude(UserListActivity.getLocation().getLatitude());
                            request.setToLongitude(UserListActivity.getLocation().getLongitude());
                            firebaseDatabase.getReference().child("requests").child(request.getRequestID()).setValue(request);
                        }
                        else {
                            showAlert("Error!!","Can not determine location. Try Again",request);
                        }

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        firebaseDatabase = FirebaseDatabase.getInstance();
                        firebaseDatabase.getReference().child("requests").child(request.getRequestID()).setValue(null);

                    }
                })
                .show();

    }

    private void showRequestedLocation(String title, String message, final double toLatitude, final double toLongitude, final Request request)
    {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context,MapsActivity.class);
                        intent.putExtra("toLongitude",toLongitude);
                        intent.putExtra("toLatitude",toLatitude);
                        intent.putExtra("fromLatitude",getLocation().getLatitude());
                        intent.putExtra("fromLongitude",getLocation().getLongitude());

                        startActivity(intent);
                        firebaseDatabase.getReference().child("requests").child(request.getRequestID()).setValue(null);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        firebaseDatabase.getReference().child("requests").child(request.getRequestID()).setValue(null);

                    }
                })
                .show();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new UserFragment(), "User");
        adapter.addFragment(new RequestFragment(), "Request");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void initSignInButton() {
        setContentView(R.layout.login_screen);
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                auth();
            }
        });

    }

    public void pushNewLocation(double longitude, double latitude) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        String userEmail = firebaseAuth.getCurrentUser().getEmail();
        String userName = firebaseAuth.getCurrentUser().getDisplayName();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users").child(userId);
        databaseReference.setValue(new User(userId, userEmail, userName, longitude, latitude)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                Toast.makeText(context,"Location Updated",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Location", "PermissionNot");

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        userlocation = location;
        pushNewLocation(longitude, latitude);
    }

    public static Location getLocation()
    {
        return userlocation;
    }

}
