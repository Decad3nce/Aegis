package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SMSDataFragment extends PreferenceFragment {
    
    public static final String PREFERENCES_DATA_ACTIVATION_SMS = "data_activation_sms";
    public static final String PREFERENCES_BACKUP_CALL_LOGS = "data_backup_call_logs";
    public static final String PREFERENCES_BACKUP_SMS_LOGS = "data_backup_sms_logs";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_preference);
    }
}