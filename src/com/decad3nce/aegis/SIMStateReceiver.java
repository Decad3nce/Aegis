package com.decad3nce.aegis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SIMStateReceiver extends BroadcastReceiver {
    private static final String TAG = "aeGis";
    
    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final String EXTRAS_SIM_STATUS = "ss";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            Log.v(TAG, "SIM STATUS CHANGED");
            Bundle extras = intent.getExtras();
            
            if(extras != null) {
                String ss = extras.getString(EXTRAS_SIM_STATUS);
                
                if(ss.equals("ABSENT")) {
                    Log.v(TAG, "SIM IS ABSENT");
                } else if (ss.equals("IMSI") || ss.equals("LOADED")) {
                    Log.v(TAG, "SIM IS PRIMED");
                    TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    String imsi = mTelephonyManager.getSubscriberId();
                    Log.v(TAG, "SIM IMSI " + imsi);
                } else {
                    Log.v(TAG, "SIM IS " + ss);
                }
            }
        }
        
    }

}
