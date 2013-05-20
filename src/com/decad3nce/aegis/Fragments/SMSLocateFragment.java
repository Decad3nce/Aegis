package com.decad3nce.aegis.Fragments;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.decad3nce.aegis.Utils;

public class SMSLocateFragment extends PreferenceFragment {
    
    public static final String PREFERENCES_LOCATE_UPDATE_DURATION = "locate_update_duration";
    public static final String PREFERENCES_LOCATE_MINIMUM_DISTANCE = "locate_minimum_distance";
    public static final String PREFERENCES_LOCATE_ACTIVATION_SMS = "locate_activation_sms";
    public static final String PREFERENCES_LOCATE_LOCK_PREF = "locate_lock_pref";
    public static final String PREFERENCES_LOCATE_GEOCODE_PREF = "locate_geocode_pref";
    public static final String PREFERENCES_LOCATE_STOP_SMS = "locate_stop_sms";
    public static final String PREFERENCES_LOCATE_ENABLED = "locate_toggle";

    protected static boolean locateEnabled;
    private Switch mLocateEnabledPreference;
    private DevicePolicyManager devicePolicyManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.locate_preference);
        setHasOptionsMenu(true);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        locateEnabled = preferences
                .getBoolean(PREFERENCES_LOCATE_ENABLED, getActivity().getResources().getBoolean(R.bool.config_default_locate_enabled));

        devicePolicyManager = (DevicePolicyManager) getActivity()
                .getSystemService(getActivity().DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (devicePolicyManager.getActiveAdmins() != null) {
            if (!devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                if (mLocateEnabledPreference != null) {
                    locateEnabled = false;
                    mLocateEnabledPreference.setChecked(false);
                }
            } else {
                mLocateEnabledPreference.setChecked(locateEnabled);
            }
        } else {
            locateEnabled = false;
            mLocateEnabledPreference.setChecked(false);
        }

        if (mLocateEnabledPreference != null)
            mLocateEnabledPreference.setOnCheckedChangeListener(locatePreferencesOnChangeListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Utils.showItem(R.id.locate_menu_settings, menu);
        mLocateEnabledPreference = (Switch) menu
                .findItem(R.id.locate_menu_settings).getActionView()
                .findViewById(R.id.locate_toggle);
    }


    CompoundButton.OnCheckedChangeListener locatePreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.locate_toggle:

                    if (isChecked) {
                        if (!isLocationServicesEnabled()) {
                            showLocationServicesDialog();
                            buttonView.setChecked(false);
                            commitToShared();
                        } else {
                            locateEnabled = true;
                        }
                    }

                    if (isChecked
                            && !devicePolicyManager
                            .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT) && locateEnabled) {
                        addAdmin();
                        commitToShared();
                    }

                    break;
            }
        }
    };


    private void commitToShared() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREFERENCES_LOCATE_ENABLED, mLocateEnabledPreference.isChecked());
        editor.commit();
    }

    public void addAdmin() {
        Intent intent = new Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                AegisActivity.DEVICE_ADMIN_COMPONENT);
        intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getResources().getString(
                        R.string.device_admin_reason));
        startActivityForResult(intent, AegisActivity.ACTIVATION_REQUEST);
    }

    protected void showLocationServicesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(getResources().getString(R.string.aegis_location_services_not_enabled));
        dialog.setPositiveButton(getResources().getString(R.string.aegis_open_location_settings), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locIntent);
            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.advanced_dialog_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();

    }

    protected boolean isLocationServicesEnabled() {
        LocationManager mLM = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(!mLM.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        }
        return true;
    }
}
