package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSLocateFragment extends PreferenceFragment {
    
    public static final String PREFERENCES_LOCATE_UPDATE_DURATION = "locate_update_duration";
    public static final String PREFERENCES_LOCATE_MINIMUM_DISTANCE = "locate_minimum_distance";
    public static final String PREFERENCES_LOCATE_ACTIVATION_SMS = "locate_activation_sms";
    public static final String PREFERENCES_LOCATE_LOCK_PREF = "locate_lock_pref";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.locate_preference);
    }
}
