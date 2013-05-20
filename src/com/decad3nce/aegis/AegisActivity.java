package com.decad3nce.aegis;

import android.app.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.view.GravityCompat;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import com.decad3nce.aegis.Fragments.*;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;

import android.view.View;

import android.util.Log;

public class AegisActivity extends FragmentActivity implements InstallToSystemDialogFragment.NoticeDialogListener {
    
    private static final String TAG = "aeGis";

    private int mIndex;
    private String MENU_INDEX;

    private static final String PREFERENCES_AEGIS_INITIALIZED = "aegis_initialized";

    private DevicePolicyManager mDevicePolicyManager;

    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mMenuTitles;

    private boolean mInitialized;
    private Menu fullMenu;

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

        FragmentManager fragmentManager = getFragmentManager();
        
        if(!mInitialized) {
            Fragment helpFragment = new LicensesFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, helpFragment).commit();
            mInitialized = true;
        }

        setContentView(R.layout.drawer_layout);

        final ActionBar bar = getActionBar();
        bar.setDisplayShowHomeEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

        Fragment fragment = new SMSAlarmFragment();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        mTitle = mDrawerTitle = getTitle();
        mMenuTitles = getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        Log.v(TAG, mDrawerList.toString());
        Log.v(TAG, mMenuTitles.toString());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(getResources().getString(R.string.app_name));
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("Menu");
                invalidateOptionsMenu();
            }
        };

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MENU_INDEX, mIndex);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectItem(savedInstanceState.getInt(MENU_INDEX));
    }

    @Override
    public void onResume() {
        super.onResume();
        //
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == ACTIVATION_REQUEST) {
                if (resultCode != Activity.RESULT_OK) {
                    editor.putBoolean(SMSLockFragment.PREFERENCES_LOCK_ENABLED, false);
                    editor.putBoolean(SMSLocateFragment.PREFERENCES_LOCATE_ENABLED, false);
                    editor.commit();
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

    
    protected void saveSettings() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();;
        editor.putBoolean(PREFERENCES_AEGIS_INITIALIZED, mInitialized);
        editor.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.full_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mIndex = position;
        FragmentManager fragmentManager = getFragmentManager();

        switch(position) {
            case 0:
                Fragment alarmFragment = new SMSAlarmFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, alarmFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 1:
                Fragment lockFragment = new SMSLockFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, lockFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 2:
                Fragment dataFragment = new SMSDataFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, dataFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 3:
                Fragment locateFragment = new SMSLocateFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, locateFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 4:
                Fragment settingsFragment = new AdvancedSettingsFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, settingsFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 5:
                Fragment simListFragment = new SIMListFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, simListFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 6:
                Fragment licensesFragment = new LicensesFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, licensesFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 7:
                Fragment aboutFragment = new AboutFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, aboutFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 8:
                Fragment helpFragment = new HelpFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, helpFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
        }

        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        boolean installedAsSystem = isAppInstalledAsSystem("com.decad3nce.aegis");

        if (!installedAsSystem) {
            (new RootTask()).setContext(this).execute();
        } else {
            Toast.makeText(this, getResources().getString(R.string.advanced_install_to_system_fail), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    private boolean isAppInstalledAsSystem(String uri) {
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(uri, 0);
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }
}
