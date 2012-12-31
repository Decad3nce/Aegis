package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AdvancedSettingsFragment extends PreferenceFragment {
    public static final String PREFERENCES_CONFIRMATION_SMS = "advanced_enable_confirmation_sms";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_preferences);
    }
}
