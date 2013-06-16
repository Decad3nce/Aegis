package com.decad3nce.aegis;

import android.app.*;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import com.decad3nce.aegis.Fragments.*;

import java.util.ArrayList;
import java.util.Arrays;

public class AegisActivity extends FragmentActivity implements InstallToSystemDialogFragment.NoticeDialogListener {

    private static final String TAG = "aeGis";

    private int mIndex = 0;
    private String MENU_INDEX = "index";

    public static final String PREFERENCES_AEGIS_VERSION = "aegis_version";
    public static final String PREFERENCES_AEGIS = "aegis_pref";

    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mMenuTitles;
    private Menu thisMenu;

    private String mVersion;

    public static final ComponentName DEVICE_ADMIN_COMPONENT = new ComponentName(
            DeviceAdmin.class.getPackage().getName(),
            DeviceAdmin.class.getName());
    public static final int ACTIVATION_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences preferences = getSharedPreferences(PREFERENCES_AEGIS, MODE_PRIVATE);

        mVersion = preferences.getString(PREFERENCES_AEGIS_VERSION, "35");

        FragmentManager fragmentManager = getFragmentManager();

        setContentView(R.layout.drawer_layout);

        final ActionBar bar = getActionBar();
        bar.setDisplayShowHomeEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

        Fragment fragment = new SMSAlarmFragment();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        mTitle = mDrawerTitle = getTitle();
        mMenuTitles = getMenuTitles();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                ((BaseAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("Menu");
                ((BaseAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
                //invalidateOptionsMenu();
                Utils.hideItem(R.id.alarm_menu_settings, thisMenu);
                Utils.hideItem(R.id.lock_menu_settings, thisMenu);
                Utils.hideItem(R.id.locate_menu_settings, thisMenu);
                Utils.hideItem(R.id.wipe_menu_settings, thisMenu);
                Utils.hideItem(R.id.data_menu_settings, thisMenu);
                Utils.showItem(R.id.action_help, thisMenu);
            }
        };

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new DrawerLayoutAdapter(this, R.layout.drawer_list_item, new ArrayList(Arrays.asList(mMenuTitles))));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        int versionCode = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //shwat
        }

        if (savedInstanceState == null) {
            if(versionCode > Integer.parseInt(mVersion)) {
                mVersion = String.valueOf(versionCode);
                WebView webView = new WebView(this);
                webView.loadUrl("file:///android_asset/changelog.html");
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setView(webView);
                dialog.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(PREFERENCES_AEGIS_VERSION, mVersion); // value to store
                        editor.commit();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                selectItem(0);
            }
        } else {
            selectItem(mIndex);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_help:
                mDrawerLayout.closeDrawer(mDrawerList);
                Utils.createWebViewDialog("file:///android_asset/help.html", this);
                return true;
        }

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.full_menu, menu);
        thisMenu = menu;
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

    private String[] getMenuTitles() {
        return getResources().getStringArray(R.array.menu_array);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                Fragment wipeFragment = new SMSWipeFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, wipeFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 3:
                Fragment dataFragment = new SMSDataFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, dataFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 4:
                Fragment locateFragment = new SMSLocateFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, locateFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 5:
                Fragment settingsFragment = new AdvancedSettingsFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, settingsFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 6:
                Fragment simListFragment = new SIMListFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, simListFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 7:
                Fragment aboutFragment = new AboutFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, aboutFragment).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
        }

        mIndex = position;
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
