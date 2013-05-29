package com.decad3nce.aegis.Fragments;

import android.app.DialogFragment;
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


public class SMSDataFragment extends PreferenceFragment implements ChooseBackupProgramDialogFragment.ChooseBackupDialogListener {
    
    public static final String PREFERENCES_DATA_ACTIVATION_SMS = "data_activation_sms";
    public static final String PREFERENCES_BACKUP_CALL_LOGS = "data_backup_call_logs";
    public static final String PREFERENCES_BACKUP_SMS_LOGS = "data_backup_sms_logs";
    public static final String PREFERENCES_DATA_ENABLED = "data_toggle";

    protected static boolean dataEnabled;
    private Switch mDataEnabledPreference;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_preference);

        setHasOptionsMenu(true);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dataEnabled = preferences
                .getBoolean(PREFERENCES_DATA_ENABLED, getActivity().getResources().getBoolean(R.bool.config_default_data_enabled));
    }

    @Override
    public void onResume() {
        super.onResume();
        //
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(!isGoogleAuthed() && !isDropboxAuthed()) {
            if(mDataEnabledPreference != null) {
                mDataEnabledPreference.setChecked(false);
            }
        } else if (isGoogleAuthed() || isDropboxAuthed()) {
            if(mDataEnabledPreference != null) {
                mDataEnabledPreference.setChecked(dataEnabled);
            }
        }

        if (mDataEnabledPreference != null)
            mDataEnabledPreference.setOnCheckedChangeListener(dataPreferencesOnChangeListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Utils.showItem(R.id.data_menu_settings, menu);
        mDataEnabledPreference = (Switch) menu
                .findItem(R.id.data_menu_settings).getActionView()
                .findViewById(R.id.data_toggle);
    }

    CompoundButton.OnCheckedChangeListener dataPreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {

            switch (buttonView.getId()) {
                case R.id.data_toggle:

                    if (isChecked) {
                        if(isGoogleAuthed() || isDropboxAuthed()) {
                            commitToShared();
                        } else {
                            DialogFragment dialog = new ChooseBackupProgramDialogFragment();
                            dialog.show(getActivity().getFragmentManager(), "ChooseBackupProgramDialogFragment");
                            commitToShared();
                        }
                    } else {
                        commitToShared();
                    }
                    break;
            }
        }
    };

    private void commitToShared() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREFERENCES_DATA_ENABLED, mDataEnabledPreference.isChecked());
        editor.commit();
    }


    protected boolean isGoogleAuthed() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        boolean googleBackup = preferences.getBoolean(
                AdvancedSettingsFragment.PREFERENCES_GOOGLE_BACKUP_CHECKED,
                getResources().getBoolean(
                        R.bool.config_default_google_backup_enabled));
        if(googleBackup){
            return true;
        }
        return false;
    }

    protected boolean isDropboxAuthed() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        boolean dropboxBackup = preferences.getBoolean(
                AdvancedSettingsFragment.PREFERENCES_DROPBOX_BACKUP_CHECKED,
                getResources().getBoolean(
                        R.bool.config_default_dropbox_backup_enabled));
        if (dropboxBackup) {
            return true;
        }
        return false;
    }
}