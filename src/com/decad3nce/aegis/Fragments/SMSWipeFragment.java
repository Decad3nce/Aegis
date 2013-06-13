package com.decad3nce.aegis.Fragments;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.R;
import com.decad3nce.aegis.Utils;

/**
 * Created by adnan on 6/13/13.
 */
public class SMSWipeFragment extends PreferenceFragment {

    public static final String PREFERENCES_WIPE_ACTIVATION_SMS = "lock_activation_sms";
    public static final String PREFERENCES_WIPE_ENABLED = "lock_toggle";

    private static final String TAG = "aeGis";

    protected static boolean wipeEnabled;
    private Switch mWipeEnabledPreference;
    private DevicePolicyManager devicePolicyManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wipe_preference);
        setHasOptionsMenu(true);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        wipeEnabled = preferences
                .getBoolean(PREFERENCES_WIPE_ENABLED, getActivity().getResources().getBoolean(R.bool.config_default_wipe_enabled));

        devicePolicyManager = (DevicePolicyManager) getActivity()
                .getSystemService(getActivity().DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        //
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //TODO: This is a fucking mess.
        //Stop coding hungover
        if (devicePolicyManager != null && devicePolicyManager.getActiveAdmins() != null) {
            if (!devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                if (mWipeEnabledPreference != null) {
                    wipeEnabled = false;
                    mWipeEnabledPreference.setChecked(false);
                }
            } else {
                mWipeEnabledPreference.setChecked(wipeEnabled);
            }
        } else {
            wipeEnabled = false;
            mWipeEnabledPreference.setChecked(false);
        }

        if (mWipeEnabledPreference != null)
            mWipeEnabledPreference.setOnCheckedChangeListener(wipePreferencesOnChangeListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Utils.showItem(R.id.wipe_menu_settings, menu);
        mWipeEnabledPreference = (Switch) menu
                .findItem(R.id.wipe_menu_settings).getActionView()
                .findViewById(R.id.wipe_toggle);
    }

    CompoundButton.OnCheckedChangeListener wipePreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.wipe_toggle:
                    if (isChecked && !devicePolicyManager
                            .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                        addAdmin();
                        commitToShared();
                    } else {
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
        editor.putBoolean(PREFERENCES_WIPE_ENABLED, mWipeEnabledPreference.isChecked());
        editor.commit();
    }

    private void addAdmin() {
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
}
