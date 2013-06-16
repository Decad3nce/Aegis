package com.decad3nce.aegis.Fragments;

import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.R;

public class AdvancedSettingsFragment extends PreferenceFragment {
    private static final String TAG = "aeGis";
    private static String accountName;
    private static boolean dropboxAccess;
    public static final String PREFERENCES_GOOGLE_BACKUP_CHECKED = "google_account_chosen";
    public static final String PREFERENCES_DROPBOX_BACKUP_CHECKED = "dropbox_account_chosen";
    public static final String PREFERENCES_CONFIRMATION_SMS = "advanced_enable_confirmation_sms";
    public static final String PREFERENCES_ABORT_BROADCAST = "advanced_enable_abort_broadcast";
    public static final String PREFERENCES_HIDE_FROM_LAUNCHER = "advanced_hide_from_launcher";
    public static final String PREFERENCES_BACKUP_PHONE_NUMBER = "advanced_backup_phone_number";
    private static final String ADVANCED_PREFERENCES_REMOVE_ADMIN = "remove_admin";
    private static final String ADVANCED_PREFERENCES_INSTALL_TO_SYSTEM = "install_to_system";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_preferences);

        final Preference removeAdmin = findPreference("remove_admin");
        final Preference installToSystem = findPreference("install_to_system");
        
        final DevicePolicyManager mDPM = (DevicePolicyManager) getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        if (mDPM.getActiveAdmins() == null || !mDPM.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
            PreferenceCategory mCategory = (PreferenceCategory) findPreference("advanced_category");
            mCategory.removePreference(removeAdmin);
        }

        Preference.OnPreferenceClickListener preferenceListener = (new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if(preference.getKey().equals(ADVANCED_PREFERENCES_REMOVE_ADMIN)) {
                            if (mDPM.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                                removeAdmin(mDPM, removeAdmin);
                            }
                        }
                        if(preference.getKey().equals(ADVANCED_PREFERENCES_INSTALL_TO_SYSTEM)) {
                            DialogFragment dialog = new InstallToSystemDialogFragment();
                            dialog.show(getActivity().getFragmentManager(), "InstallToSystemDialogFragment");
                        }
                        return false;
                    }
                });
        
        removeAdmin.setOnPreferenceClickListener(preferenceListener);
        installToSystem.setOnPreferenceClickListener(preferenceListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getActivity().getSharedPreferences("google_prefs", 0);
        accountName = prefs.getString("chosen_google_account_name", null);

        SharedPreferences prefs1 = getActivity().getSharedPreferences("dropbox_prefs", 0);
        String key = prefs1.getString("dropbox_access_key", null);
        dropboxAccess = getKeyHonesty(key);

        final CheckBoxPreference googleAccount = (CheckBoxPreference) findPreference("google_account_chosen");
        final CheckBoxPreference dropboxAccount = (CheckBoxPreference) findPreference("dropbox_account_chosen");

        if(getAccountName() != null) {
            googleAccount.setSummary(getAccountName());
            googleAccount.setChecked(true);
        } else {
            googleAccount.setSummary(R.string.preferences_advanced_dropbox_account_summary_inactive);
            googleAccount.setChecked(false);
        }

        if (getDropboxAccess()) {
            dropboxAccount.setSummary(R.string.preferences_advanced_dropbox_account_summary_active);
            dropboxAccount.setChecked(true);
        } else {
            dropboxAccount.setSummary(R.string.preferences_advanced_dropbox_account_summary_inactive);
            dropboxAccount.setChecked(false);
        }
    }

    private void removeAdmin(DevicePolicyManager mDPM, Preference removeAdmin) {
        mDPM.removeActiveAdmin(AegisActivity.DEVICE_ADMIN_COMPONENT);
        removeAdmin.setTitle(R.string.preferences_advanced_remove_admin_completed);
        removeAdmin.setSummary(null);
        disableDependents();
    }

    private void disableDependents() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SMSLockFragment.PREFERENCES_LOCK_ENABLED, false);
        editor.putBoolean(SMSLocateFragment.PREFERENCES_LOCATE_ENABLED, false);
        editor.putBoolean(SMSWipeFragment.PREFERENCES_WIPE_ENABLED, false);
        editor.commit();
    }

    private boolean getKeyHonesty(String key) {
        if(key != null) {
            return true;
        }
        return false;
    }

    private static String getAccountName() {
        if(accountName == null) {
            return null;
        }
        return accountName;
    }

    private static boolean getDropboxAccess() {
        return dropboxAccess;
    }
}
