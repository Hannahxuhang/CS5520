package hangxu.finalproject.cs5520.hikerplus;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hangxu.finalproject.cs5520.hikerplus.model.Constants;
import hangxu.finalproject.cs5520.hikerplus.model.MLatLng;
import hangxu.finalproject.cs5520.hikerplus.service.FetchAddressIntentService;


public class TrackHikingActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static final String TAG = "TRACK_HIKING_ACTIVITY";

    private static final String TOAST_CLIENT_CONNECTED = "GoogleApiClient has connected";
    private static final String TOAST_CLIENT_NOT_CONNECTED = "GoogleApiClient not connected";
    private static final String TOAST_PAUSE_BUTTON = "Click Start To Continue, Long Click Stop To Stop";
    private static final String TOAST_STOP_BUTTON = "Stop Tracking";
    private static final String TOAST_STEP_SENSOR_NOT_AVAILABLE = "This device is not available for step sensor";
    private static final long POLLING_FREQ = 1000 * 3;
    private static final long FASTEST_UPDATE_FREQ = 1000;
    private static final String INITIAL_TIME = "00:00:00";
    private static final String SIMPLE_DATE_FORMAT = "yyyy/MM/dd";
    private static final double EARTH_RADIUS = 6371.01;

    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private Polyline mRoutePolyline;
    private LatLng mCurrentLatLng;

    private DatabaseReference mRecordDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private TextView distanceTv;
    private TextView stepNumTv;
    private TextView timeTv;
    private Button startButton;
    private Button stopButton;

    private long timeSpan = 0;
    private Handler mHandler;
    private Runnable mRunnable;

    // Variables for Record class
    private String recordDate;
    private String endPosAddr = "";

    private List<LatLng> mRoutePoints = new ArrayList<>();
    private List<MLatLng> mRouteLatLngs = new ArrayList<>();

    private String timeSpanText;
    private int mSteps = 0;
    private double distanceDouble = 0d;
    //private String mDistance;

    // Value of the step counter sensor when the listener was registered
    // Total steps are calculated from this value
    private int mCounterSteps = 0;

    private AddressResultReceiver mResultReceiver;

    private Location stopLocation;
    private MLatLng mStartLatLng;
    private MLatLng mStopLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_hiking);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize Views
        distanceTv = (TextView) findViewById(R.id.hiking_distance);
        stepNumTv = (TextView) findViewById(R.id.step_num);
        timeTv = (TextView) findViewById(R.id.time_span);
        startButton = (Button) findViewById(R.id.start_track_button);
        stopButton = (Button) findViewById(R.id.stop_track_button);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Get current FirebaseUser
        mCurrentUser = mAuth.getCurrentUser();

        // Get uid
        String uid = mCurrentUser.getUid();

        // Initialize FirebaseDatabase
        mRecordDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid)
                .child("MRecords");

        // Initialize mResultReceiver
        mResultReceiver = new AddressResultReceiver(new Handler());

        // Initialize Handler
        mHandler = new Handler();

        // Create GoogleApiClient
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "GoogleApiClient has been connected");

                        Toast.makeText(TrackHikingActivity.this, TOAST_CLIENT_CONNECTED, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.w(TAG, "GoogleApiClient connect has failed");

                        Toast.makeText(TrackHikingActivity.this, TOAST_CLIENT_NOT_CONNECTED, Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .build();

        // Wire up startButton
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get start location and mark it on the map
                Location startLocation = getLastKnownLocation();
                LatLng startLatLng = new LatLng(startLocation.getLatitude(), startLocation.getLongitude());
                mStartLatLng = new MLatLng(startLocation.getLatitude(), startLocation.getLongitude());

                if (startLatLng != null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(startLatLng)
                            .alpha(0.7f)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mMap.addMarker(markerOptions);
                } else {
                    Log.w(TAG, "No data for start location found");
                }

                // Start updating user's location
                if (mClient.isConnected()) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(TrackHikingActivity.this, TOAST_CLIENT_NOT_CONNECTED, Toast.LENGTH_SHORT)
                            .show();
                }

                // Start timer
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        timeSpanText = getTime(timeSpan);
                        timeTv.setText(timeSpanText);
                        timeSpan += 1000;
                        mHandler.postDelayed(this, 1000);
                    }
                };
                mHandler.postDelayed(mRunnable, 1000);

                // Start step counter
                if (isKitKatWithStepSensor()) {
                    registerSensorListener(Sensor.TYPE_STEP_COUNTER);
                } else {
                    Toast.makeText(TrackHikingActivity.this, TOAST_STEP_SENSOR_NOT_AVAILABLE, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // wire up pauseButton
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pause
                stopLocationUpdates();

                // Stop timer
                mHandler.removeCallbacks(mRunnable);

                Toast.makeText(TrackHikingActivity.this, TOAST_PAUSE_BUTTON, Toast.LENGTH_SHORT)
                        .show();

                // Unregister sensor listener
                unRegisterSensorListener();
            }
        });

        // Wire up stopButton
        stopButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Stop
                stopLocationUpdates();

                Toast.makeText(TrackHikingActivity.this, TOAST_STOP_BUTTON, Toast.LENGTH_SHORT)
                        .show();

                // Unregister sensor listener
                unRegisterSensorListener();

                // Stop timer
                mHandler.removeCallbacks(mRunnable);

                // Get end location LatLng and mark it on the map
                stopLocation = getLastKnownLocation();
                LatLng stopLatLng = new LatLng(stopLocation.getLatitude(), stopLocation.getLongitude());
                mStopLatLng = new MLatLng(stopLocation.getLatitude(), stopLocation.getLongitude());

                if (stopLatLng != null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(stopLatLng)
                            .alpha(0.7f)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    mMap.addMarker(markerOptions);
                } else {
                    Log.w(TAG, "No data for stop position found");
                }

                // Convert mRoutePoints into mRouteLatLngs
                for (LatLng point : mRoutePoints) {
                    MLatLng mLatLng = new MLatLng(point.latitude, point.longitude);
                    mRouteLatLngs.add(mLatLng);
                }

                // Get stop position location address
                if (stopLocation != null && Geocoder.isPresent()) {
                    startIntentService(stopLocation);
                    Log.d(TAG, "stop position address: " + endPosAddr);
                }

                startActivity(new Intent(TrackHikingActivity.this, PostListActivity.class));

                return true;
            }
        });
    }

    // Helper function for getting current date with specific SimpleDateFormat
    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Helper function for getting last known location
    private Location getLastKnownLocation() {
         Location mLastKnownLocation = null;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return mLastKnownLocation;
        }

        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mClient);
        return mLastKnownLocation;
    }

    // Helper function for starting requesting Google FusedLocationServices for location
    protected void startLocationUpdates() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(POLLING_FREQ);
        request.setFastestInterval(FASTEST_UPDATE_FREQ);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, this);
    }

    private void updateTrack() {
        // Move map to current location
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 19f);
        mMap.animateCamera(update);

        mRoutePoints = mRoutePolyline.getPoints();

        LatLng mPreviousLatLng;
        // Get last LatLng in mRoutePoints
        if (mRoutePoints.size() != 0) {
            mPreviousLatLng = mRoutePoints.get(mRoutePoints.size() - 1);
        } else {
            mPreviousLatLng = new LatLng(mStartLatLng.getLatitude(), mStartLatLng.getLongitude());
        }

        // Update distance in UI
        double disTwoPoints = getDistanceFromTwoLatLng(mCurrentLatLng, mPreviousLatLng, 0, 0);

        distanceDouble += disTwoPoints;
        Log.d(TAG, "distanceDouble is: " + distanceDouble);

        DecimalFormat decimalFormat = new DecimalFormat("##.###");
        String mDistance = String.valueOf(decimalFormat.format(distanceDouble));
        distanceTv.setText(mDistance + " KM");

        // Update Polyline in UI
        mRoutePoints.add(mCurrentLatLng);
        mRoutePolyline.setPoints(mRoutePoints);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
    }

    // Helper function for changing milliseconds to String time format
    private String getTime(long timeSpan) {
        String resTime;

        if (timeSpan >= 1000) {
            int hours = (int)((timeSpan / (1000 * 60 * 60)) % 24);
            int minutes = (int)((timeSpan / (1000 * 60)) % 60);
            int seconds = (int)(timeSpan / 1000) % 60;

            String hourString = String.valueOf(hours);
            String minuteString = String.valueOf(minutes);
            String secondString = String.valueOf(seconds);

            if (hours < 10) hourString = "0" + hourString;
            if (minutes < 10) minuteString = "0" + minuteString;
            if (seconds < 10) secondString = "0" + secondString;

            resTime = hourString + ":" + minuteString + ":" + secondString;
        } else {
            resTime = INITIAL_TIME;
        }
        return resTime;
    }

    // Helper function for checking if TYPE_STEP_DETECTOR is available
    private boolean isKitKatWithStepSensor() {
        // Require at least Android KitKat Version
        int currentApiVersion = Build.VERSION.SDK_INT;

        // Check that the device support the step counter and step detector
        PackageManager pm = this.getPackageManager();
        return currentApiVersion >= Build.VERSION_CODES.KITKAT
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    // Helper function for registering sensor event listener
    private void registerSensorListener(int sensorType) {
        SensorManager sensorManager = (SensorManager) this.getSystemService(Activity.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);

        sensorManager.registerListener(mSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Helper function for unRegistering sensor event listener
    private void unRegisterSensorListener() {
        SensorManager sensorManager = (SensorManager) this.getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(mSensorListener);
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // A step detector event is received for each step.
                // This means we need to count steps ourselves
                mSteps += sensorEvent.values.length;

                Log.d(TAG, "New steps detected by Step Detector, total step count: " + mSteps);
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                /*
                A step counter event contains the total number of steps since the listener
                was first registered. We need to keep track of this initial value to calculate the
                number of steps taken, as the first value a listener receives is undefined.
                 */
                if (mCounterSteps < 1) {
                    mCounterSteps = (int) sensorEvent.values[0];
                }

                // Calculate steps taken based on first counter value received
                mSteps = (int) sensorEvent.values[0] - mCounterSteps;

                Log.d(TAG, "New steps detected by Step Counter, total step count: " + mSteps);
            }

            // Update stepNumTv with latest step count
            stepNumTv.setText(String.valueOf(mSteps));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Kilometers
     */
    public static double getDistanceFromTwoLatLng(LatLng pos1, LatLng pos2, double el1, double el2) {

        double lat1 = pos1.latitude;
        double lng1 = pos1.longitude;
        double lat2 = pos2.latitude;
        double lng2 = pos2.longitude;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c * 1000;
        double height = el1 - el2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance) / 1000;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "my current location" + mCurrentLatLng);
        updateTrack();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect GoogleApiClient
        mClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mClient.isConnected()) {
            startLocationUpdates();

            // Start timer
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    String timeSpanText = getTime(timeSpan);
                    timeTv.setText(timeSpanText);
                    timeSpan += 1000;
                    mHandler.postDelayed(this, 1000);
                }
            };
            mHandler.postDelayed(mRunnable, 1000);

            // Start step counter
            if (isKitKatWithStepSensor()) {
                registerSensorListener(Sensor.TYPE_STEP_COUNTER);
            } else {
                Toast.makeText(TrackHikingActivity.this, TOAST_STEP_SENSOR_NOT_AVAILABLE, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();

        // Unregister sensor listener
        unRegisterSensorListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient
        mClient.disconnect();

        // Unregister sensor listener
        unRegisterSensorListener();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

        // Zoom-in map
        LatLng spaceNeedle = new LatLng(47.620422, -122.349358);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(spaceNeedle, 15f);
        mMap.animateCamera(update);

        // Initialize Polyline
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

    private void startIntentService(Location mLastLocation) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    // Class which is a subclass of ResultReceiver
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string
            // or an error message received from the intent service
            endPosAddr = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.d(TAG, "endPosAddr is: " + endPosAddr);

            // Show a toast message if an address was found
            Toast.makeText(TrackHikingActivity.this, getString(R.string.address_found), Toast.LENGTH_SHORT)
                    .show();

            // Update FirebaseDatabase
            // Get current date for Record class
            recordDate = getCurrentDate();

            // Get start MLatLng
            LatLng startLocation = mRoutePoints.get(0);
            MLatLng mStartPos = new MLatLng(startLocation.latitude, startLocation.longitude);

            // Get a push id
            String mRecordId = mRecordDatabase.push().getKey();

            // Store record data in mRecordDatabase
            Map<String, Object> dataToSave = new HashMap<>();
            dataToSave.put("date", recordDate);
            dataToSave.put("destinationAddress", endPosAddr);
            dataToSave.put("routePoints", mRouteLatLngs);
            dataToSave.put("timeSpan", timeSpanText);
            dataToSave.put("stepNum", String.valueOf(mSteps));
            dataToSave.put("distance", distanceDouble);
            dataToSave.put("recordId", mRecordId);
            dataToSave.put("startLocation", mStartPos);
            dataToSave.put("stopLocation", mStopLatLng);

            mRecordDatabase.child(mRecordId).setValue(dataToSave);
        }
    }
}
