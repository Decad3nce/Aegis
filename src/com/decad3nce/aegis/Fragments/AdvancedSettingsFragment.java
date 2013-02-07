package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.AdvancedSettingsActivity;
import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.BackupDropboxAccountsActivity;
import com.decad3nce.aegis.R;

import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class AdvancedSettingsFragment extends PreferenceFragment {
    public static final String PREFERENCES_GOOGLE_BACKUP_CHECKED = "google_account_chosen";
    public static final String PREFERENCES_DROPBOX_BACKUP_CHECKED = "dropbox_account_chosen";
    public static final String PREFERENCES_CONFIRMATION_SMS = "advanced_enable_confirmation_sms";
    public static final String PREFERENCES_ABORT_BROADCAST = "advanced_enable_abort_broadcast";
    public static final String PREFERENCES_HIDE_FROM_LAUNCHER = "advanced_hide_from_launcher";
    private static final String ADVANCED_PREFERENCES_REMOVE_ADMIN = "remove_admin";
    private static final String ADVANCED_PREFERENCES_INSTALL_TO_SYSTEM = "install_to_system";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_preferences);

        final Preference removeAdmin = (Preference) findPreference("remove_admin");
        final Preference installToSystem = (Preference) findPreference("install_to_system");
        final CheckBoxPreference googleAccount = (CheckBoxPreference) findPreference("google_account_chosen");
        final CheckBoxPreference dropboxAccount = (CheckBoxPreference) findPreference("dropbox_account_chosen");
        
        if(AdvancedSettingsActivity.getAccountName() != null) {
            googleAccount.setSummary(AdvancedSettingsActivity.getAccountName());
            googleAccount.setChecked(true);
        } else {
            googleAccount.setSummary(R.string.preferences_advanced_dropbox_account_summary_inactive);
            googleAccount.setChecked(false);
        }
        
        if (AdvancedSettingsActivity.getDropboxAccess()) {
            dropboxAccount.setSummary(R.string.preferences_advanced_dropbox_account_summary_active);
            dropboxAccount.setChecked(true);
        } else {
            dropboxAccount.setSummary(R.string.preferences_advanced_dropbox_account_summary_inactive);
            dropboxAccount.setChecked(false);
        }
        
        final DevicePolicyManager mDPM = (DevicePolicyManager) getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        if (mDPM.getActiveAdmins() == null || !mDPM.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
            PreferenceCategory mCategory = (PreferenceCategory) findPreference("advanced_category");
            mCategory.removePreference(removeAdmin);
        }

        Preference.OnPreferenceClickListener preferenceListener = (new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        if(preference.getKey().equals(ADVANCED_PREFERENCES_REMOVE_ADMIN)) {
                            if (mDPM.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                                mDPM.removeActiveAdmin(AegisActivity.DEVICE_ADMIN_COMPONENT);
                                removeAdmin.setTitle(R.string.preferences_advanced_remove_admin_completed);
                                removeAdmin.setSummary(null);
                            }
                        }
                        if(preference.getKey().equals(ADVANCED_PREFERENCES_INSTALL_TO_SYSTEM)) {
                            DialogFragment dialog = new InstallToSystemDialogFragment();
                            dialog.show(getFragmentManager(), "InstallToSystemDialogFragment");
                        }
                        return false;
                    }
                });
        
        removeAdmin.setOnPreferenceClickListener(preferenceListener);
        installToSystem.setOnPreferenceClickListener(preferenceListener);
    }
}
