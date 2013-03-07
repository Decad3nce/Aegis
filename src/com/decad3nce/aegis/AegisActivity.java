package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;
import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;
import com.decad3nce.aegis.Fragments.SMSDataFragment;
import com.decad3nce.aegis.Fragments.SMSLocateFragment;
import com.decad3nce.aegis.Fragments.ChooseBackupProgramDialogFragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class AegisActivity extends SherlockFragmentActivity implements ChooseBackupProgramDialogFragment.ChooseBackupDialogListener{
    
    private static final String TAG = "aeGis";

    private static final String PREFERENCES_AEGIS_INITIALIZED = "aegis_initialized";
    public static final String PREFERENCES_ALARM_ENABLED = "alarm_toggle";
    public static final String PREFERENCES_DATA_ENABLED = "data_toggle";
    public static final String PREFERENCES_LOCK_ENABLED = "lock_toggle";
    public static final String PREFERENCES_LOCATE_ENABLED = "locate_toggle";

    protected static boolean alarmEnabled;
    protected static boolean dataEnabled;
    protected static boolean lockEnabled;
    protected static boolean locateEnabled;

    private DevicePolicyManager mDevicePolicyManager;

    private Switch mAlarmEnabledPreference;
    private Switch mLockEnabledPreference;
    private Switch mDataEnabledPreference;
    private Switch mLocateEnabledPreference;
    
    private boolean mInitialized;
    private Menu fullMenu;

    TabsAdapter mTabsAdapter;
    ViewPager mViewPager;

    public static final ComponentName DEVICE_ADMIN_COMPONENT = new ComponentName(
            DeviceAdmin.class.getPackage().getName(),
            DeviceAdmin.class.getName());
    public static final int ACTIVATION_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        
        mInitialized = preferences
                .getBoolean(PREFERENCES_AEGIS_INITIALIZED, this.getResources()
                        .getBoolean(R.bool.config_default_aegis_initialized));
        
        if(!mInitialized) {
            launchActivity(HelpActivity.class);
            mInitialized = true;
        }

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        setContentView(mViewPager);

        final ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowHomeEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setTitle(R.string.app_name);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.alarm_section),
                SMSAlarmFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.lock_section),
                SMSLockFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.data_section),
                SMSDataFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.locate_section),
                SMSLocateFragment.class, null);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
        
        alarmEnabled = preferences
                .getBoolean(PREFERENCES_ALARM_ENABLED, this.getResources()
                        .getBoolean(R.bool.config_default_alarm_enabled));
        dataEnabled = preferences.getBoolean(PREFERENCES_DATA_ENABLED, this
                .getResources().getBoolean(R.bool.config_default_data_enabled));
        lockEnabled = preferences.getBoolean(PREFERENCES_LOCK_ENABLED, this
                .getResources().getBoolean(R.bool.config_default_lock_enabled));
        locateEnabled = preferences.getBoolean(PREFERENCES_LOCATE_ENABLED, this
                .getResources()
                .getBoolean(R.bool.config_default_locate_enabled));

        invalidateOptionsMenu();

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }
    
    @Override
    public boolean onKeyUp(int keycode, KeyEvent e) {
            switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                fullMenu.performIdentifierAction(R.id.full_menu_settings, 0);
                return true;
            }
        return super.onKeyUp(keycode, e);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.help:
                launchActivity(HelpActivity.class);
                return true;
            case R.id.settings:
                launchActivity(AdvancedSettingsActivity.class);
                return true;
            case R.id.licenses:
                launchActivity(LicensesActivity.class);
                return true;
            case R.id.about:
                launchActivity(AboutActivity.class);
                return true;
        }
        
        return false;
    }
    
    private void launchActivity(Class<?> mClass) {
        Intent intent = new Intent(AegisActivity.this,
                mClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    CompoundButton.OnCheckedChangeListener deviceAdminPreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            switch (buttonView.getId()) {
            case R.id.alarm_toggle:

                if (isChecked) {
                    alarmEnabled = true;
                } else {
                    alarmEnabled = false;
                }

                break;

            case R.id.locate_toggle:

                if (isChecked) {
                    if (isLocationServicesEnabled()) {
                        locateEnabled = true;
                    } else {
                        showLocationServicesDialog();
                    }
                } else {
                    locateEnabled = false;
                }
                
                if (isChecked
                        && !mDevicePolicyManager
                                .isAdminActive(DEVICE_ADMIN_COMPONENT) && locateEnabled) {
                    addAdmin();
                }

                break;

            case R.id.data_toggle:

                if (isChecked) {
                    if(isGoogleAuthed() || isDropboxAuthed()) {
                        dataEnabled = true;
                    } else {
                        DialogFragment dialog = new ChooseBackupProgramDialogFragment();
                        dialog.show(getFragmentManager(), "ChooseBackupProgramDialogFragment");
                    }
                } else {
                    dataEnabled = false;
                }

                break;

            case R.id.lock_toggle:

                if (isChecked) {
                    lockEnabled = true;
                } else {
                    lockEnabled = false;
                }

                if (isChecked
                        && !mDevicePolicyManager
                                .isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                    addAdmin();
                }

                break;
            }
        }
    };
    
    public void addAdmin() {
        Intent intent = new Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                DEVICE_ADMIN_COMPONENT);
        intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getResources().getString(
                        R.string.device_admin_reason));
        startActivityForResult(intent, ACTIVATION_REQUEST);
    }

    protected boolean isGoogleAuthed() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
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
                .getDefaultSharedPreferences(this);
        boolean dropboxBackup = preferences.getBoolean(
                AdvancedSettingsFragment.PREFERENCES_DROPBOX_BACKUP_CHECKED,
                getResources().getBoolean(
                        R.bool.config_default_dropbox_backup_enabled));
        if (dropboxBackup) {
            return true;
        }
        return false;
    }

    protected void showLocationServicesDialog() {
        Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.aegis_location_services_not_enabled));
        dialog.setPositiveButton(getResources().getString(R.string.aegis_open_location_settings), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locIntent);
            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.advanced_dialog_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
        
    }

    protected boolean isLocationServicesEnabled() {
        LocationManager mLM = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        if(!mLM.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if(!isGoogleAuthed() && !isDropboxAuthed()) {
            dataEnabled = false;
            if(mDataEnabledPreference != null) {
                mDataEnabledPreference.setChecked(false);
            }
        } else if (isGoogleAuthed() || isDropboxAuthed()) {
            dataEnabled = true;
            if(mDataEnabledPreference != null) {
                mDataEnabledPreference.setChecked(true);
            }
        }
        
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mDevicePolicyManager.getActiveAdmins() != null) {
            if (!mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                if (mLockEnabledPreference != null) {
                    mLockEnabledPreference.setChecked(false);
                    lockEnabled = false;
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.full_menu, menu);
        fullMenu = menu;

        switch (mTabsAdapter.getCurrentTab()) {
        case 0:       
            showItem(R.id.alarm_menu_settings, menu);
            mAlarmEnabledPreference = (Switch) menu
                    .findItem(R.id.alarm_menu_settings).getActionView()
                    .findViewById(R.id.alarm_toggle);
            addAdminListener(R.id.alarm_toggle, alarmEnabled, mAlarmEnabledPreference);
            break;

        case 1:
            showItem(R.id.lock_menu_settings, menu);
            mLockEnabledPreference = (Switch) menu
                    .findItem(R.id.lock_menu_settings).getActionView()
                    .findViewById(R.id.lock_toggle);
            addAdminListener(R.id.lock_toggle, lockEnabled, mLockEnabledPreference);
            break;

        case 2:
            showItem(R.id.data_menu_settings, menu);
            mDataEnabledPreference = (Switch) menu
                    .findItem(R.id.data_menu_settings).getActionView()
                    .findViewById(R.id.data_toggle);
            addAdminListener(R.id.data_toggle, dataEnabled, mDataEnabledPreference);
            break;

        case 3:
            showItem(R.id.locate_menu_settings, menu);
            mLocateEnabledPreference = (Switch) menu
                    .findItem(R.id.locate_menu_settings).getActionView()
                    .findViewById(R.id.locate_toggle);
            addAdminListener(R.id.locate_toggle, locateEnabled, mLocateEnabledPreference);
            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == ACTIVATION_REQUEST) {
                if (resultCode != Activity.RESULT_OK) {
                    mLockEnabledPreference.setChecked(false);
                    lockEnabled = false;
                }
                return;
            }
        } else {
            Toast.makeText(this, R.string.device_admin_reason,
                    Toast.LENGTH_LONG).show();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void showItem(int id, Menu menu)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }
    
    private void addAdminListener(int toggle, boolean what, Switch who) {
        switch(toggle) {
        case R.id.data_toggle:
        case R.id.alarm_toggle:
            if(what) {
                who.setChecked(true);
                }
            break;
        case R.id.locate_toggle:
        case R.id.lock_toggle:
            mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE); 
            if (mDevicePolicyManager.getActiveAdmins() != null) {
                if (what && mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                    who.setChecked(true);
                }
            }
            break;
            
        }

        if (who != null) {
            who.setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
        }
    }
    
    protected void saveSettings() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();;
        editor.putBoolean(PREFERENCES_ALARM_ENABLED, alarmEnabled);
        editor.putBoolean(PREFERENCES_LOCK_ENABLED, lockEnabled);
        editor.putBoolean(PREFERENCES_DATA_ENABLED, dataEnabled);
        editor.putBoolean(PREFERENCES_LOCATE_ENABLED, locateEnabled);
        editor.putBoolean(PREFERENCES_AEGIS_INITIALIZED, mInitialized);
        editor.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.full_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
