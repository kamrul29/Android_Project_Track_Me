package com.kamruzzaman.trackme;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<User> users;
    private ProgressBar progressBar;
    private ListView userListView;
    UserListAdapter userListAdapter;
    private FirebaseAuth firebaseAuth;


    public UserFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        users = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        progressBar = view.findViewById(R.id.user_progress);
        userListView = view.findViewById(R.id.user_list_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getUserFromServer();

    }

    private void getUserFromServer()
    {
        firebaseAuth = FirebaseAuth.getInstance();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("users");
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String test = "";
                users = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren())
                {
                    User user = child.getValue(User.class);
                    if(!user.getUserId().equals(currentUserId))
                    {
                        users.add(user);
                    }
                    test+=user.toString()+"\n\n";
                }
                progressBar.setVisibility(View.GONE);
                if(users.size()>0)
                {
                    userListAdapter  = new UserListAdapter(getActivity().getApplicationContext(),R.layout.user_single_row,R.id.user_name,users);
                    userListView.setAdapter(userListAdapter);
                }

                Log.e("TotalUser",users.size()+" ");
//                userListAdapter.notifyDataSetChanged();
//                debugView.setText(test);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
