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

public class SMSLockFragment extends PreferenceFragment {

    public static final String PREFERENCES_LOCK_ACTIVATION_SMS = "lock_activation_sms";
    public static final String PREFERENCES_LOCK_PASSWORD = "lock_password";
    public static final String PREFERENCES_LOCK_SEND_PASSWORD_PREF = "lock_send_password_in_sms_pref";
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
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lockEnabled = preferences
                .getBoolean(PREFERENCES_LOCK_ENABLED, getActivity().getResources().getBoolean(R.bool.config_default_lock_enabled));

        devicePolicyManager = (DevicePolicyManager) getActivity()
                .getSystemService(getActivity().DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        inflateFullMenu(menu);
        Utils.showItem(R.id.lock_menu_settings, menu);
        mLockEnabledPreference = (Switch) menu
                .findItem(R.id.lock_menu_settings).getActionView()
                .findViewById(R.id.lock_toggle);
        mLockEnabledPreference.setChecked(false);

        if (devicePolicyManager != null && devicePolicyManager.getActiveAdmins() != null) {
            if (devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT) && lockEnabled) {
                    mLockEnabledPreference.setChecked(lockEnabled);
            }
        }

        mLockEnabledPreference.setOnCheckedChangeListener(lockPreferencesOnChangeListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
                    }
                    commitToShared();
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

    private void inflateFullMenu(Menu menu) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.full_menu, menu);
    }
}
