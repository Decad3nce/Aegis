package com.decad3nce.aegis.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.decad3nce.aegis.R;
import com.decad3nce.aegis.Utils;

public class SMSAlarmFragment extends PreferenceFragment {
    public static final String PREFERENCES_ALARM_ENABLED = "alarm_toggle";
    public static final String PREFERENCES_ALARM_VIBRATE = "alarm_vibrate";
    public static final String PREFERENCES_ALARM_DURATION = "alarm_duration";
    public static final String PREFERENCES_ALARM_ACTIVATION_SMS = "alarm_activation_sms";

    private Switch mAlarmEnabledPreference;
    protected static boolean alarmEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.alarm_preference);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        alarmEnabled = preferences
                .getBoolean(PREFERENCES_ALARM_ENABLED, getActivity().getResources().getBoolean(R.bool.config_default_alarm_enabled));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Utils.showItem(R.id.alarm_menu_settings, menu);
        mAlarmEnabledPreference = (Switch) menu
                .findItem(R.id.alarm_menu_settings).getActionView()
                .findViewById(R.id.alarm_toggle);
        mAlarmEnabledPreference.setChecked(alarmEnabled);
        mAlarmEnabledPreference.setOnCheckedChangeListener(alarmPreferencesOnChangeListener);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    CompoundButton.OnCheckedChangeListener alarmPreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());

            switch (buttonView.getId()) {
                case R.id.alarm_toggle:
                    SharedPreferences.Editor editor = preferences.edit();;

                    if (isChecked) {
                        editor.putBoolean(PREFERENCES_ALARM_ENABLED, true);
                        editor.commit();
                    } else {
                        editor.putBoolean(PREFERENCES_ALARM_ENABLED, false);;
                        editor.commit();
                    }
                    break;
            }
        }
    };
}