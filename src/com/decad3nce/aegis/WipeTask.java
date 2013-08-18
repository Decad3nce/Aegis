package com.decad3nce.aegis;

import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.decad3nce.aegis.Fragments.SMSWipeFragment;

import java.io.File;

/**
 * Created by Decad3nce on 8/14/13.
 */
public class WipeTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog dialog = null;
    private Context context;
    private File[] sdcards;
    private String address;
    private DevicePolicyManager devicePolicyManager;
    private SharedPreferences preferences;

    public WipeTask(Context context, File[] sdcards, String address) {
        this.address = address;
        this.context = context;
        this.sdcards = sdcards;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getResources().getString(R.string.app_name));
        dialog.setMessage(context.getResources().getString(R.string.aegis_wipe_in_progress));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        devicePolicyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        boolean wipesdcards  = preferences.getBoolean(
                SMSWipeFragment.PREFERENCES_WIPE_SDCARD_PREF,
                context.getResources().getBoolean(
                        R.bool.config_default_wipe_sdcard));

        if(wipesdcards) {
            for(int i = 0; i < sdcards.length; ++i){
                wipeSdcard(sdcards[i]);
            }
            Utils.sendSMS(context, address,
                    context.getResources().getString(R.string.util_sendsms_wipe_sdcard_pass));
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        dialog.dismiss();

        //Call after background worker to not interfere with system resources
        if (devicePolicyManager
                .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
            try {
                Utils.sendSMS(context, address,
                        context.getResources().getString(R.string.util_sendsms_wipe_pass));
                devicePolicyManager.wipeData(0);
            } catch (Exception e) {
                Utils.sendSMS(context, address,
                        context.getResources().getString(R.string.util_sendsms_wipe_fail) + " " + e.toString());
            }
        }
    }

    public void wipeSdcard(File locations) {
        Log.v("aeGis", "Wiping " + locations.getName());
        try {
            File[] filenames = locations.listFiles();
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());
                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                locations.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void wipeDirectory(String name) {
        File directoryFile = new File(name);
        File[] filenames = directoryFile.listFiles();
        if (filenames != null && filenames.length > 0) {
            for (File tempFile : filenames) {
                if (tempFile.isDirectory()) {
                    wipeDirectory(tempFile.toString());
                    tempFile.delete();
                } else {
                    tempFile.delete();
                }
            }
        } else {
            directoryFile.delete();
        }
    }
}
