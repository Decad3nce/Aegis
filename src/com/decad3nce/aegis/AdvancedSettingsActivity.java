package com.decad3nce.aegis;

import java.util.List;

import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;
import com.decad3nce.aegis.Fragments.InstallToSystemDialogFragment;
import eu.chainfire.libsuperuser.Shell;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class AdvancedSettingsActivity extends PreferenceActivity implements InstallToSystemDialogFragment.NoticeDialogListener {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         getActionBar().setDisplayHomeAsUpEnabled(true);
        
         getFragmentManager().beginTransaction().replace(android.R.id.content,
         new AdvancedSettingsFragment()).commit();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, AegisActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Shit was installed, dog",
                Toast.LENGTH_LONG).show();
        (new Startup()).setContext(this).execute();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(this, "Shit was cancelled, yo.",
                Toast.LENGTH_LONG).show();
    }
    
    private class Startup extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog = null;
        private Context context = null;
        private boolean suAvailable = false;
        private String suVersion = null;
        private String suVersionInternal = null;
        private List<String> suResult = null;

        public Startup setContext(Context context) {
            this.context = context;
            return this;
        }
        
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("aeGis");
            dialog.setMessage("Installng to system...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            suAvailable = Shell.SU.available();
            if (suAvailable) {
                suVersion = Shell.SU.version(false);
                suVersionInternal = Shell.SU.version(true);
                suResult = Shell.SU.run(new String[] {
                    "id",
                    "busybox mount -o remount,rw /system",
                    "busybox cp /data/app/com.decad3nce.aegis-*.apk /system/app/",
                    "busybox chmod 644 /system/app/com.decad3nce.aegis-*.apk",
                    "busybox pm uninstall com.decad3nce.aegis",
                    "busybox rm /data/app/com.decad3nce.aegis-*.apk",
                    "busybox mount -o remount,ro /system",
                    "busybox reboot"
                });
            }

            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }
}
