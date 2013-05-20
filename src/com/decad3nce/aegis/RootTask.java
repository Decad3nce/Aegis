package com.decad3nce.aegis;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import eu.chainfire.libsuperuser.Shell;

import java.util.List;

/**
 *
 */
public class RootTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog dialog = null;
    public Dialog dialog1 = null;
    private Context context = null;
    private boolean suAvailable = false;
    private String suVersion = null;
    private String suVersionInternal = null;
    private List<String> suResult = null;

    public RootTask setContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getResources().getString(R.string.app_name));
        dialog.setMessage(context.getResources().getString(R.string.advanced_dialog_installing));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        dialog1 = new Dialog(context);
        dialog1.setTitle(context.getResources().getString(R.string.app_name));
        dialog1.setContentView(R.layout.dialog_view);
        dialog1.setCancelable(true);
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
                    "busybox cp /data/app/com.decad3nce.aegis*.apk /system/app/",
                    "busybox chmod 644 /system/app/com.decad3nce.aegis*.apk",
                    "busybox pm uninstall com.decad3nce.aegis",
                    "busybox rmdir /data/app-lib/com.decad3nce*",
                    "busybox rm /data/app/com.decad3nce.aegis*.apk",
                    "busybox mount -o remount,ro /system",
                    "busybox reboot"
            });
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        dialog.dismiss();

        if(!suAvailable) {
            dialog1.show();
        }
    }
}
