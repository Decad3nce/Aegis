package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;
import com.decad3nce.aegis.Fragments.SMSWipeFragment;
import com.decad3nce.aegis.Fragments.SplashMenuFragment;

import android.app.ActionBar;
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
import android.widget.CompoundButton;
import android.widget.Switch;

public class AegisActivity extends FragmentActivity {

    private static final String PREFERENCES_DESCRIPTION_DIALOG_SHOWN = "description_dialog_shown";
    
    public static final String PREFERENCES_ALARM_ENABLED = "alarm_toggle";
    public static final String PREFERENCES_WIPE_ENABLED = "wipe_toggle";
    public static final String PREFERENCES_LOCK_ENABLED = "lock_toggle";
    
    protected static boolean alarmEnabled;
    protected static boolean wipeEnabled;
    protected static boolean lockEnabled;

    private DevicePolicyManager mDevicePolicyManager;
    
    private Switch mAlarmEnabledPreference;
    private Switch mLockEnabledPreference;
    private Switch mWipeEnabledPreference;
    
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
        bar.setTitle(R.string.app_name);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.alarm_section),
                SMSAlarmFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.lock_section),
                SMSLockFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.wipe_section),
                SMSWipeFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.about_section),
                SplashMenuFragment.class, null);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (!preferences
                .getBoolean(PREFERENCES_DESCRIPTION_DIALOG_SHOWN, false)) {
            // TODO: HTML tutorial
        }
        
        alarmEnabled = preferences.getBoolean(PREFERENCES_ALARM_ENABLED,
                this.getResources().getBoolean(R.bool.config_default_alarm_enabled));
        wipeEnabled = preferences.getBoolean(PREFERENCES_WIPE_ENABLED,
                this.getResources().getBoolean(R.bool.config_default_wipe_enabled));
        lockEnabled = preferences.getBoolean(PREFERENCES_LOCK_ENABLED,
                this.getResources().getBoolean(R.bool.config_default_lock_enabled));
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

    CompoundButton.OnCheckedChangeListener deviceAdminPreferencesOnChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            
            switch(buttonView.getId()) {
            case R.id.alarm_toggle:
                
                if(isChecked) {
                    alarmEnabled = true;
                } else {
                    alarmEnabled = false;
                }
                
                break;
                
            case R.id.wipe_toggle:
                
                if(isChecked) {
                    wipeEnabled = true;
                } else {
                    wipeEnabled = false;
                }
                
                if (isChecked && !mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
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

                break;
                
            case R.id.lock_toggle:
                
                if(isChecked) {
                    lockEnabled = true;
                } else {
                    lockEnabled = false;
                }
                
                if (isChecked && !mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
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
                
                break;
            }
        }
    };
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVATION_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.device_admin_reason, Toast.LENGTH_LONG).show();
                    mLockEnabledPreference.setChecked(false);
                    mWipeEnabledPreference.setChecked(false);
                    lockEnabled = false;
                    wipeEnabled = false;
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        
        switch(mTabsAdapter.getCurrentTab()) {
        case 0:
            inflater.inflate(R.menu.alarm_menu, menu);
            mAlarmEnabledPreference = (Switch) menu.findItem(R.id.alarm_menu_settings).getActionView().findViewById(R.id.alarm_toggle);
            
            if(alarmEnabled) {
                mAlarmEnabledPreference.setChecked(true);
            }
            
            if (mAlarmEnabledPreference != null) {
                mAlarmEnabledPreference.setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
            break;
            
        case 1:
            inflater.inflate(R.menu.lock_menu, menu);
            mLockEnabledPreference = (Switch) menu.findItem(R.id.lock_menu_settings).getActionView().findViewById(R.id.lock_toggle);
            
            if(lockEnabled) {
                mLockEnabledPreference.setChecked(true);
            }
            
            if (mLockEnabledPreference != null) {
                mLockEnabledPreference.setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
            break;
            
        case 2:
            inflater.inflate(R.menu.wipe_menu, menu);
            mWipeEnabledPreference = (Switch) menu.findItem(R.id.wipe_menu_settings).getActionView().findViewById(R.id.wipe_toggle);
            
            if(wipeEnabled) {
                mWipeEnabledPreference.setChecked(true);
            }
            
            if (mWipeEnabledPreference != null) {           
                mWipeEnabledPreference.setOnCheckedChangeListener(deviceAdminPreferencesOnChangeListener);
            }
            break;
            
        case 3:
            break;
        }
        return true;
    }
    
    protected void saveSettings() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("alarm_toggle", alarmEnabled);
        editor.putBoolean("lock_toggle", lockEnabled);
        editor.putBoolean("wipe_toggle", wipeEnabled);
        editor.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
