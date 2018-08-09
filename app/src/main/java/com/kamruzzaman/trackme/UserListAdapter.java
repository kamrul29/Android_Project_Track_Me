package com.kamruzzaman.trackme;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    private View row;
    private TextView userNameView, userLogoView, userEmailView;
    private ImageView requestButton;
    private String userName, userLogo, userEmail;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    String loggedInUserID,requestedUserID;


    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull ArrayList<User> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        loggedInUserID = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.user_single_row, parent, false);
        }
        userEmailView = row.findViewById(R.id.user_email);
        userNameView =  row.findViewById(R.id.user_name);
        userLogoView =  row.findViewById(R.id.user_logo);
        requestButton =  row.findViewById(R.id.addButton);
        final User currentUser = getItem(position);
        requestedUserID = currentUser.getUserId();
        userLogo= currentUser.getUserLogo();
        userName = currentUser.getName();
        userEmail = currentUser.getEmail();

        userNameView.setText(userName);
        userEmailView.setText(userEmail);
        userLogoView.setText(userLogo);
        requestButton.setImageDrawable(context.getDrawable(R.drawable.add_black_24));
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request request = new Request(currentUser.getUserId(),loggedInUserID,false);
                String pushID = firebaseDatabase.getReference().child("requests").push().getKey();
                request.setRequestID(pushID);
                RequestCode.lastRequestID = pushID;
                if(UserListActivity.getLocation()==null)
                {
                    Toast.makeText(context,"Please wait until your location is available",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    request.setFromLatitude(UserListActivity.getLocation().getLatitude());
                    request.setFromLongitude(UserListActivity.getLocation().getLongitude());
                    firebaseDatabase.getReference().child("requests").child(pushID).setValue(request);
                    Toast.makeText(context,"Request Sent to "+currentUser.getName()+" "+currentUser.getUserId(),Toast.LENGTH_SHORT).show();

                }







            }
        });

        return row;
    }
}
