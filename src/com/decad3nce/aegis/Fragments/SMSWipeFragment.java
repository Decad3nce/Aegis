package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSWipeFragment extends PreferenceFragment {
    
    public static final String PREFERENCES_WIPE_ACTIVATION_SMS = "wipe_activation_sms";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wipe_preference);
    }
}