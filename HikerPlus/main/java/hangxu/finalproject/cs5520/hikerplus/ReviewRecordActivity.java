package hangxu.finalproject.cs5520.hikerplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.model.MLatLng;

public class ReviewRecordActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "REVIEW_RECORD_ACTIVITY";

    private static final String EXTRA_RECORD_ID = "hangxu.finalproject.cs5520.hikerplus.record_id";

    private GoogleMap mMap;
    private Polyline mRoutePolyline;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mRecordDatabase;
    private DatabaseReference mPointsDatabase;

    private CircleImageView mProfileIv;
    private TextView userNameTv;
    private TextView distanceTv;
    private TextView dateTv;
    private TextView addrTv;
    private TextView timeSpanTv;
    private TextView stepNumTv;

    // Variables of Record class
    private String recordDate;
    private String endPosAddr;
    private String recordTime;
    private String stepCount;
    private double recordDistance;
    private LatLng startLatLng;
    private LatLng stopLatLng;
    private List<LatLng> mRoutePoints = new ArrayList<>();
    // TODO:
    private double mStartLat;
    private double mStartLng;
    private double mStopLat;
    private double mStopLng;

    public static Intent newIntent(Context packageContext, String recordId) {
        Intent intent = new Intent(packageContext, ReviewRecordActivity.class);
        intent.putExtra(EXTRA_RECORD_ID, recordId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_record);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.review_map);
        mapFragment.getMapAsync(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        String uid = mCurrentUser.getUid();

        // Retrieving extra
        String recordId = this.getIntent().getStringExtra(this.EXTRA_RECORD_ID);

        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid);

        mRecordDatabase = mUserDatabase
                .child("MRecords")
                .child(recordId);

        mPointsDatabase = mRecordDatabase.child("routePoints");

        // Initialize Views
        mProfileIv = (CircleImageView) findViewById(R.id.reviewUserProfile);
        userNameTv = (TextView) findViewById(R.id.review_username);
        distanceTv = (TextView) findViewById(R.id.review_distance);
        dateTv = (TextView) findViewById(R.id.review_date);
        addrTv = (TextView) findViewById(R.id.review_destination);
        timeSpanTv = (TextView) findViewById(R.id.review_timeSpan);
        stepNumTv = (TextView) findViewById(R.id.review_stepNum);

        // Add value event listener to mUserDatabase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("username").getValue().toString();
                String profile = dataSnapshot.child("profile").getValue().toString();

                // Update UI
                userNameTv.setText(userName);

                // Use picasso to update profile image
                if (!profile.equals("default")) {
                    Picasso.with(ReviewRecordActivity.this)
                            .load(profile)
                            .placeholder(R.drawable.profile_img1)
                            .into(mProfileIv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Add value event listener to mRecordDatabase
        mRecordDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recordDate = dataSnapshot.child("date").getValue().toString();
                endPosAddr = dataSnapshot.child("destinationAddress").getValue().toString();
                recordTime = dataSnapshot.child("timeSpan").getValue().toString();
                stepCount = dataSnapshot.child("stepNum").getValue().toString();
                recordDistance = Double.valueOf(dataSnapshot.child("distance").getValue().toString());

                mStartLat = Double.valueOf(dataSnapshot.child("startLocation").child("latitude").getValue().toString());
                mStartLng = Double.valueOf(dataSnapshot.child("startLocation").child("longitude").getValue().toString());
                mStopLat = Double.valueOf(dataSnapshot.child("stopLocation").child("latitude").getValue().toString());
                mStopLng = Double.valueOf(dataSnapshot.child("stopLocation").child("longitude").getValue().toString());

                startLatLng = new LatLng(mStartLat, mStartLng);
                stopLatLng = new LatLng(mStopLat, mStopLng);

                // Convert recordDistance to String with specific decimal format
                DecimalFormat decimalFormat = new DecimalFormat("##.##");
                String mDistance = String.valueOf(decimalFormat.format(recordDistance));

                // Update UI
                dateTv.setText(recordDate);
                addrTv.setText("AT " + endPosAddr);
                timeSpanTv.setText(recordTime);
                stepNumTv.setText(stepCount);
                distanceTv.setText(mDistance + " KM");

                // Zoom-in map
                // Use LatLngBounds
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(startLatLng)
                        .include(stopLatLng)
                        .build();
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 350, 400, 0);
                mMap.animateCamera(update);

                // Mark start and stop location
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(startLatLng)
                        .alpha(0.7f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.addMarker(startMarkerOptions);

                MarkerOptions stopMarkerOptions = new MarkerOptions()
                        .position(stopLatLng)
                        .alpha(0.7f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                mMap.addMarker(stopMarkerOptions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Add child event listener to mPointsDatabase
        mPointsDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MLatLng mLatLng = dataSnapshot.getValue(MLatLng.class);

                // TODO: change it to Google's LatLng list
                LatLng mPoint = new LatLng(mLatLng.getLatitude(), mLatLng.getLongitude());
                mRoutePoints.add(mPoint);

                // Draw record route on map
                mRoutePolyline.setPoints(mRoutePoints);
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
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add PolylineOptions
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.rgb(204, 153, 0));
        polylineOptions.width(6);
        mRoutePolyline = mMap.addPolyline(polylineOptions);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);
    }
}
