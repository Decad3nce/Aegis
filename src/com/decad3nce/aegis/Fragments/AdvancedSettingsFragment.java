package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.AegisActivity;
import com.decad3nce.aegis.R;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class AdvancedSettingsFragment extends PreferenceFragment {
    public static final String PREFERENCES_CONFIRMATION_SMS = "advanced_enable_confirmation_sms";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_preferences);

        final Preference removeAdmin = (Preference) findPreference("remove_admin");
        final DevicePolicyManager mDPM = (DevicePolicyManager) getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        if (mDPM.getActiveAdmins() == null) {
            PreferenceCategory mCategory = (PreferenceCategory) findPreference("advanced_category");
            mCategory.removePreference(removeAdmin);
        }

        removeAdmin.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                            if (mDPM.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                                mDPM.removeActiveAdmin(AegisActivity.DEVICE_ADMIN_COMPONENT);
                                removeAdmin.setTitle(R.string.preferences_advanced_remove_admin_completed);
                                removeAdmin.setSummary(null);
                            }
                        return false;
                    }
                });
    }
}
