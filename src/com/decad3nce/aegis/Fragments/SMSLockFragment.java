package com.decad3nce.aegis.Fragments;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.decad3nce.aegis.Utils;

public class SMSLockFragment extends PreferenceFragment {

    public static final String PREFERENCES_LOCK_ACTIVATION_SMS = "lock_activation_sms";
    public static final String PREFERENCES_LOCK_PASSWORD = "lock_password";
    public static final String PREFERENCES_LOCK_WIPE_PREF = "lock_wipe_pref";
    public static final String PREFERENCES_LOCK_ENABLED = "lock_toggle";

    private static final String TAG = "aeGis";

    protected static boolean lockEnabled;
    private Switch mLockEnabledPreference;
    private DevicePolicyManager devicePolicyManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lock_preference);
        setHasOptionsMenu(true);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lockEnabled = preferences
                .getBoolean(PREFERENCES_LOCK_ENABLED, getActivity().getResources().getBoolean(R.bool.config_default_lock_enabled));

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
        if (devicePolicyManager.getActiveAdmins() != null) {
            if (!devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                if (mLockEnabledPreference != null) {
                    lockEnabled = false;
                    mLockEnabledPreference.setChecked(false);
                }
            } else {
                mLockEnabledPreference.setChecked(lockEnabled);
            }
        } else {
            lockEnabled = false;
            mLockEnabledPreference.setChecked(false);
        }

        if (mLockEnabledPreference != null)
            mLockEnabledPreference.setOnCheckedChangeListener(lockPreferencesOnChangeListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Utils.showItem(R.id.lock_menu_settings, menu);
        mLockEnabledPreference = (Switch) menu
                .findItem(R.id.lock_menu_settings).getActionView()
                .findViewById(R.id.lock_toggle);
    }

    CompoundButton.OnCheckedChangeListener lockPreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.lock_toggle:
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
        editor.putBoolean(PREFERENCES_LOCK_ENABLED, mLockEnabledPreference.isChecked());
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
}