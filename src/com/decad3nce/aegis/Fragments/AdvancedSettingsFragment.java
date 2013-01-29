package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.AdvancedSettingsActivity;
import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.BackupAccountsActivity;
import com.decad3nce.aegis.R;

import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class AdvancedSettingsFragment extends PreferenceFragment {
    public static final String PREFERENCES_CONFIRMATION_SMS = "advanced_enable_confirmation_sms";
    public static final String PREFERENCES_ABORT_BROADCAST = "advanced_enable_abort_broadcast";
    public static final String PREFERENCES_HIDE_FROM_LAUNCHER = "advanced_hide_from_launcher";
    private static final String ADVANCED_PREFERENCES_REMOVE_ADMIN = "remove_admin";
    private static final String ADVANCED_PREFERENCES_INSTALL_TO_SYSTEM = "install_to_system";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_preferences);

        final Preference removeAdmin = (Preference) findPreference("remove_admin");
        final Preference installToSystem = (Preference) findPreference("install_to_system");
        final Preference googleAccount = (Preference) findPreference("chosen_google_account");
        googleAccount.setSummary(AdvancedSettingsActivity.getAccountName());
        
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
