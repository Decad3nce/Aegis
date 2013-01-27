package com.decad3nce.aegis.Fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSAlarmFragment extends SherlockFragment {
    public static final String PREFERENCES_ALARM_VIBRATE = "alarm_vibrate";
    public static final String PREFERENCES_ALARM_ACTIVATION_SMS = "alarm_activation_sms";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.alarm_preference);
    }
}