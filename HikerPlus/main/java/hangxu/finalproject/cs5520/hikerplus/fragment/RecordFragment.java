package hangxu.finalproject.cs5520.hikerplus.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.TrackHikingActivity;
import hangxu.finalproject.cs5520.hikerplus.adapter.RecordRecyclerAdapter;
import hangxu.finalproject.cs5520.hikerplus.model.Record;

/**
 * Fragment class used for user's hiking records feature.
 */

public class RecordFragment extends Fragment {

    private static final String TAG = "RECORD_FRAGMENT";

    private static final String LET_US_START = "START YOUR HIKE!";

    private DatabaseReference mUserDatabase;
    private DatabaseReference mRecordDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    private Button startHikingButton;
    private CircleImageView userProfileImage;
    private TextView userNameTv;

    private RecordRecyclerAdapter recordRecyclerAdapter;
    private List<Record> recordList;
    private RecyclerView recyclerView;

    public static RecordFragment newInstance() {
        RecordFragment fragment = new RecordFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recordList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        final String uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid);

        mRecordDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid)
                .child("MRecords");

        mRecordDatabase.keepSynced(true);

        startHikingButton = (Button) view.findViewById(R.id.startHikingButton);
        userProfileImage = (CircleImageView) view.findViewById(R.id.recordUserProfile);
        userNameTv = (TextView) view.findViewById(R.id.recordUserName);

        recyclerView = (RecyclerView) view.findViewById(R.id.record_list_container);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // add value event listener on mUserDatabase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("username").getValue().toString();
                String profile = dataSnapshot.child("profile").getValue().toString();

                userNameTv.setText(userName);

                if (!profile.equals("default")) {
                    Picasso.with(getActivity())
                            .load(profile)
                            .placeholder(R.drawable.profile_img1)
                            .into(userProfileImage);
                }

                Log.d(TAG, "Read value successfully");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value", databaseError.toException());
            }
        });

        if (recordList.size() == 0) {
            Toast.makeText(getActivity(), LET_US_START, Toast.LENGTH_LONG)
                    .show();
        }

        // add value event listener on mRecordDatabase
        mRecordDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "added child to MRcords database");

                Record record = dataSnapshot.getValue(Record.class);

                recordList.add(record);

                // reverse the order of records
                // Collections.reverse(recordList);

                recordRecyclerAdapter = new RecordRecyclerAdapter(getActivity(), recordList);
                recyclerView.setAdapter(recordRecyclerAdapter);
                recordRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // wire up startHikingButton
        startHikingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TrackHikingActivity.class);
                startActivity(intent);
            }
        });
    }
}
