package hangxu.finalproject.cs5520.hikerplus.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.model.Constants;

/**
 * Service class used for reverse geocoding and getting real-world address.
 */

public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "INTENT_SERVICE";

    protected ResultReceiver mReceiver;

    // Constructor for this service
    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String errorMessage;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Get the location passed to this service through an extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );
        } catch (IOException e) {
            // Catch network or other I/O problems
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, e);
        } catch (IllegalArgumentException e) {
            // Catch invalid latitude or longitude values
            errorMessage = getString(R.string.invalid_lat_lng_used);
            Log.e(TAG, errorMessage, e);
        }

        // Handle case where no address were found
        if (addresses == null || addresses.size() == 0) {
            errorMessage = getString(R.string.no_address_found);
            Log.e(TAG, errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread

            /*
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
                Log.i(TAG, getString(R.string.address_found));
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        TextUtils.join(" ", addressFragments));
            }*/


            // Only receive first line of the Address
            addressFragments.add(address.getAddressLine(0));
            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(" ", addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
