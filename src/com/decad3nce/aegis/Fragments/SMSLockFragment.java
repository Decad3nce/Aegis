package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSLockFragment extends PreferenceFragment {

    public static final String PREFERENCES_LOCK_ACTIVATION_SMS = "lock_activation_sms";
    public static final String PREFERENCES_LOCK_PASSWORD = "lock_password";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lock_preference);
    }
}