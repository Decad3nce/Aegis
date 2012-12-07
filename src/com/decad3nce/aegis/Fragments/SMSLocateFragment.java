package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSLocateFragment extends PreferenceFragment {
    
    public static final String PREFERENCES_LOCATE_ACTIVATION_SMS = "locate_activation_sms";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.locate_preference);
    }
}
