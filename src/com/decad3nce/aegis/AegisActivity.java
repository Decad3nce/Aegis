package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;
import com.decad3nce.aegis.Fragments.SMSWipeFragment;
import com.decad3nce.aegis.Fragments.SMSLocateFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class AegisActivity extends FragmentActivity {

    public static final String PREFERENCES_ALARM_ENABLED = "alarm_toggle";
    public static final String PREFERENCES_WIPE_ENABLED = "wipe_toggle";
    public static final String PREFERENCES_LOCK_ENABLED = "lock_toggle";
    public static final String PREFERENCES_LOCATE_ENABLED = "locate_toggle";
    public static final String PREFERENCES_AEGIS_INITIALIZED = "initialized";

    protected static boolean alarmEnabled;
    protected static boolean wipeEnabled;
    protected static boolean lockEnabled;
    protected static boolean locateEnabled;
    private static boolean mInitialized;

    private DevicePolicyManager mDevicePolicyManager;

    private Switch mAlarmEnabledPreference;
    private Switch mLockEnabledPreference;
    private Switch mWipeEnabledPreference;
    private Switch mLocateEnabledPreference;

    TabsAdapter mTabsAdapter;
    ViewPager mViewPager;

    public static final ComponentName DEVICE_ADMIN_COMPONENT = new ComponentName(
            DeviceAdmin.class.getPackage().getName(),
            DeviceAdmin.class.getName());
    public static final int ACTIVATION_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        setContentView(mViewPager);

        final ActionBar bar = getActionBar();
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
        mTabsAdapter.addTab(bar.newTab().setText(R.string.wipe_section),
                SMSWipeFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.locate_section),
                SMSLocateFragment.class, null);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);  

        if(!isServiceRunning()) {
            startService(new Intent(this, SMSMonitorService.class));
        }
        
        mInitialized = preferences
                .getBoolean(PREFERENCES_AEGIS_INITIALIZED, this.getResources()
                        .getBoolean(R.bool.config_default_aegis_initialized));
        
        if(!mInitialized) {
            mInitialized = true;
            
            Intent initialIntent = new Intent(AegisActivity.this, InitializationActivity.class);
            initialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            initialIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(initialIntent);
        }
        
        alarmEnabled = preferences
                .getBoolean(PREFERENCES_ALARM_ENABLED, this.getResources()
                        .getBoolean(R.bool.config_default_alarm_enabled));
        wipeEnabled = preferences.getBoolean(PREFERENCES_WIPE_ENABLED, this
                .getResources().getBoolean(R.bool.config_default_wipe_enabled));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, R.string.text_stub,
                        Toast.LENGTH_LONG).show();
        }
        
        return false;
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
                    locateEnabled = true;
                } else {
                    locateEnabled = false;
                }
                
                if (isChecked
                        && !mDevicePolicyManager
                                .isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                    addAdmin();
                }

                break;

            case R.id.wipe_toggle:

                if (isChecked) {
                    wipeEnabled = true;
                } else {
                    wipeEnabled = false;
                }

                if (isChecked
                        && !mDevicePolicyManager
                                .isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                    addAdmin();
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

    @Override
    public void onResume() {
        super.onResume();

        if (!mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
            if (mLockEnabledPreference != null) {
                mLockEnabledPreference.setChecked(false);
                lockEnabled = false;
            }
            if (mWipeEnabledPreference != null) {
                mWipeEnabledPreference.setChecked(false);
                wipeEnabled = false;
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getMenuInflater();

        switch (mTabsAdapter.getCurrentTab()) {
        case 0:
            inflater.inflate(R.menu.alarm_menu, menu);
            mAlarmEnabledPreference = (Switch) menu
                    .findItem(R.id.alarm_menu_settings).getActionView()
                    .findViewById(R.id.alarm_toggle);

            if (alarmEnabled) {
                mAlarmEnabledPreference.setChecked(true);
            }

            if (mAlarmEnabledPreference != null) {
                mAlarmEnabledPreference
                        .setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
            break;

        case 1:
            inflater.inflate(R.menu.lock_menu, menu);
            mLockEnabledPreference = (Switch) menu
                    .findItem(R.id.lock_menu_settings).getActionView()
                    .findViewById(R.id.lock_toggle);

            if (lockEnabled && mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                mLockEnabledPreference.setChecked(true);
            }

            if (mLockEnabledPreference != null) {
                mLockEnabledPreference
                        .setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
            break;

        case 2:
            inflater.inflate(R.menu.wipe_menu, menu);
            mWipeEnabledPreference = (Switch) menu
                    .findItem(R.id.wipe_menu_settings).getActionView()
                    .findViewById(R.id.wipe_toggle);

            if (wipeEnabled && mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                mWipeEnabledPreference.setChecked(true);
            }

            if (mWipeEnabledPreference != null) {
                mWipeEnabledPreference
                        .setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
            break;

        case 3:
            inflater.inflate(R.menu.locate_menu, menu);
            mLocateEnabledPreference = (Switch) menu
                    .findItem(R.id.locate_menu_settings).getActionView()
                    .findViewById(R.id.locate_toggle);

            if (locateEnabled && mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                mLocateEnabledPreference.setChecked(true);
            }

            if (mLocateEnabledPreference != null) {
                mLocateEnabledPreference
                        .setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
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
                    mWipeEnabledPreference.setChecked(false);
                    lockEnabled = false;
                    wipeEnabled = false;
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
    
    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMSMonitorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void saveSettings() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("initialized", mInitialized);
        editor.putBoolean("alarm_toggle", alarmEnabled);
        editor.putBoolean("lock_toggle", lockEnabled);
        editor.putBoolean("wipe_toggle", wipeEnabled);
        editor.putBoolean("locate_toggle", locateEnabled);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
