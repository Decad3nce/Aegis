package com.decad3nce.aegis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;

public class DialerCodeReceiver extends BroadcastReceiver {
    private static final String TAG = "aeGis";
    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            
            final SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            
            boolean hideFromLauncher  = preferences.getBoolean(
                    AdvancedSettingsFragment.PREFERENCES_HIDE_FROM_LAUNCHER,
                    context.getResources().getBoolean(
                            R.bool.config_default_advanced_hide_from_launcher));

            if(hideFromLauncher) {
                Intent aegisIntent = new Intent(context, LoginActivity.class);
                aegisIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(aegisIntent);
            }
        }

    }
}
