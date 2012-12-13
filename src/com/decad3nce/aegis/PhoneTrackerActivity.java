package com.decad3nce.aegis;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.decad3nce.aegis.Fragments.SMSLocateFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PhoneTrackerActivity extends Activity implements LocationListener {

    private static final String TAG = "aeGis";

    private String originatingAddress;
    private boolean mLocationTracking = false;
    private boolean mDisableTracking = false;
    private boolean mFirstTrack = true;

    private LocationManager mLocationManager;
    private String mBestProvider;

    protected Location mLocation;
    protected LocationListener mLocationListener;

    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.location_layout);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        originatingAddress = extras.getString("address");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        handler.post(getData);
    }

    private final Runnable getData = new Runnable() {
        public void run() {
            getDataFrame();
        }
    };

    private void getDataFrame() {
        Criteria criteria = new Criteria();
        final boolean gpsEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i(TAG, "getDataFrame");
        
        if (!mFirstTrack) {
            if (!mLocationTracking && !mDisableTracking) {
                if (!gpsEnabled && isGPSToggleable()) {
                    enableGPS();
                }

                if (gpsEnabled) {
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                } else {
                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                }

                mLocationTracking = true;
                mBestProvider = mLocationManager
                        .getBestProvider(criteria, true);
                startTracking();
            }
        } else {
            Log.i(TAG, "First Track");
            startTracking();
        }

        if (mLocationTracking && mDisableTracking) {
            stopTracking();
        }

        handler.postDelayed(getData, 10000);
    }

    public void stopTracking() {
        mLocationManager.removeUpdates(this);
        handler.removeCallbacksAndMessages(null);
    }

    public void startTracking() {
        final boolean gpsEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean networkEnabled = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (!mFirstTrack && gpsEnabled) {
            Log.i(TAG, "Tracking by GPS");
            
            int mLocateUpdateDuration = Integer.parseInt(preferences.getString(
                    SMSLocateFragment.PREFERENCES_LOCATE_UPDATE_DURATION, getResources()
                    .getString(R.string.config_default_locate_update_duration)));
            
            mLocateUpdateDuration = mLocateUpdateDuration * 1000;
            
            int mLocateMinimumDistance = Integer.parseInt(preferences.getString(
                    SMSLocateFragment.PREFERENCES_LOCATE_MINIMUM_DISTANCE, getResources()
                    .getString(R.string.config_default_locate_minimum_distance)));
            
            Log.i(TAG, "Location update interval is set at: " + mLocateUpdateDuration);
            Log.i(TAG, "Location minimum distance is set at: " + mLocateMinimumDistance);
            
            mLocationManager.requestLocationUpdates(
                    mBestProvider, mLocateUpdateDuration, mLocateMinimumDistance, this);
            mLocation = mLocationManager.getLastKnownLocation(mBestProvider);
            
        } else if (!mFirstTrack && networkEnabled) {
            Log.i(TAG, "Tracking by Network Location");
            
            int mLocateUpdateDuration = Integer.parseInt(preferences.getString(
                    SMSLocateFragment.PREFERENCES_LOCATE_UPDATE_DURATION, getResources()
                    .getString(R.string.config_default_locate_update_duration)));
            
            mLocateUpdateDuration = mLocateUpdateDuration * 1000;
            
            int mLocateMinimumDistance = Integer.parseInt(preferences.getString(
                    SMSLocateFragment.PREFERENCES_LOCATE_MINIMUM_DISTANCE, getResources()
                    .getString(R.string.config_default_locate_minimum_distance)));
            
            Log.i(TAG, "Location update interval is set at: " + mLocateUpdateDuration);
            Log.i(TAG, "Location minimum distance is set at: " + mLocateMinimumDistance);
            
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, mLocateUpdateDuration, mLocateMinimumDistance, this);
            mLocation = mLocationManager.getLastKnownLocation(mBestProvider);
            
        } else {
            mFirstTrack = false;
            Log.i(TAG, "First Track && Tracking by Network Location");
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    public void disableTracking(View view) {
        final Button disableButton = (Button) findViewById(R.id.disable_tracking_button);
        disableButton.setText("Tracking Disabled");
        mDisableTracking = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isGPSToggleable() {
        PackageManager pacman = getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings",
                    PackageManager.GET_RECEIVERS);
        } catch (NameNotFoundException e) {
            return false;
        }

        if (pacInfo != null) {
            for (ActivityInfo actInfo : pacInfo.receivers) {
                if (actInfo.name
                        .equals("com.android.settings.widget.SettingsAppWidgetProvider")
                        && actInfo.exported) {
                    return true;
                }
            }
        }

        return false;
    }

    private void enableGPS() {
        String provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    protected boolean isBetterLocation(Location location,
            Location currentBestLocation) {
        
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        
        int mLocateUpdateDurationOlder = Integer.parseInt(preferences.getString(
                SMSLocateFragment.PREFERENCES_LOCATE_UPDATE_DURATION, getResources()
                .getString(R.string.config_default_locate_update_duration)));
        
        //Multiply chosen value by 2 to return a location regardless if more accurate or not.
        mLocateUpdateDurationOlder = mLocateUpdateDurationOlder * 1000 * 2;
        
        Log.i(TAG, "Signficantly Older interval is set at: " + mLocateUpdateDurationOlder);
                
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > mLocateUpdateDurationOlder;
        boolean isSignificantlyOlder = timeDelta < mLocateUpdateDurationOlder;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        String geoCodedLocation;

        geoCodedLocation = geoCodeMyLocation(location.getLatitude(),
                location.getLongitude());

        if ((isBetterLocation(location, mLocation) && Geocoder.isPresent()) || mFirstTrack) {
            try {
                Log.i(TAG, "Sending SMS location update");
                Utils.sendSMS(this, originatingAddress, geoCodedLocation);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public String geoCodeMyLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,
                    longitude, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder(
                        "Address:\n");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress
                            .append(returnedAddress.getAddressLine(i)).append(
                                    "\n");
                }
                return strReturnedAddress.toString();
            } else {
                return "No Location Determined";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "No Location Determined";
        }
    }
}
