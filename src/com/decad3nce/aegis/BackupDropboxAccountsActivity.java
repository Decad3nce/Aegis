package com.decad3nce.aegis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;
import com.decad3nce.aegis.Fragments.BackupAccountsDialogFragment;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.app.Activity;

import com.decad3nce.aegis.Fragments.SMSDataFragment;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.DropboxAPI.Entry;

public class BackupDropboxAccountsActivity extends Activity implements BackupAccountsDialogFragment.NoticeDialogListener{
    private static final String TAG = "aeGis";
    
    final static private String APP_KEY = "kd1v3dkvtddpqm6";
    final static private String APP_SECRET = "XXXXXXXXXXXXXX";
    
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    
    final static private String ACCESS_KEY_NAME = "dropbox_access_key";
    final static private String ACCESS_SECRET_NAME = "dropbox_secret_key";
    final static private String ACCOUNT_PREFS_NAME = "dropbox_prefs";
    
    public static boolean isLoggedIn = false;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private static Uri callLogFileUri;
    private static Uri smsLogFileUri;
    private ProgressBar progressBar;
    private ContentResolver cr;
    private Context context;
    private String address;
    private boolean callLogs;
    private boolean smsLogs;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_layout);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        cr = getContentResolver();
        context = this;
        Intent intent;

        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        try {
            intent = getIntent();

            if (intent.hasExtra("fromReceiver")) {
                address = intent.getStringExtra("fromReceiver");
                Log.i(TAG, "Backup intent from receiver");
                recoverData();
            } else {
                Log.i(TAG, "Backup intent from elsewhere");
                DialogFragment dialog = new BackupAccountsDialogFragment();
                dialog.show(getFragmentManager(),
                        "BackupAccountsDialogFragment");
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "Resuming");
        
        if (mDBApi.getSession().authenticationSuccessful() && !isLoggedIn) {
            try {
                Log.e(TAG, "Authentication was successful");
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                Log.e(TAG, "Storing keys");
                isLoggedIn = true;
                
                Intent intent = getIntent();
                if (!(intent.hasExtra("fromReceiver"))) {
                    finish();
                }
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        } else {
        }
    }
    
    private void recoverData() {
        Log.i(TAG, "Recovering data");

        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_Dropbox";
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        
        callLogs = preferences.getBoolean(SMSDataFragment.PREFERENCES_BACKUP_CALL_LOGS, this.getResources().getBoolean(R.bool.config_default_data_backup_call_logs));
        smsLogs = preferences.getBoolean(SMSDataFragment.PREFERENCES_BACKUP_SMS_LOGS, this.getResources().getBoolean(R.bool.config_default_data_backup_sms_logs));
        
        
        if (callLogs) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Recovering call logs data");
                    java.io.File internalFile = getFileStreamPath("call_logs_"
                            + timeStamp + ".txt");
                    Uri internalCallLogs = Uri.fromFile(internalFile);
                    callLogFileUri = BackupUtils.getAllCallLogs(cr,
                            internalCallLogs, context, timeStamp);
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (smsLogs) {
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Recovering sms logs data");
                    java.io.File internalFile = getFileStreamPath("sms_logs_"
                            + timeStamp + ".txt");
                    Uri internalSMSLogs = Uri.fromFile(internalFile);
                    smsLogFileUri = BackupUtils.getSMSLogs(cr, internalSMSLogs,
                            context, timeStamp);
                }
            });
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        
        saveFilesToDropbox();
    }
    
    private String[] getStoredKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    private void saveFilesToDropbox() {
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {   
              // Uploading content.
              FileInputStream inputStream = null;
              try {
                  
                  if(callLogs) {
                      Log.i(TAG, "Generating new file to upload: " + callLogFileUri);
                      java.io.File file = new java.io.File(callLogFileUri.getPath());
                      inputStream = new FileInputStream(file);
                      Entry newEntry = mDBApi.putFile(callLogFileUri.getLastPathSegment(), inputStream, file.length(), null, null);
                      Log.i(TAG, "The uploaded file's rev is: " + newEntry.rev);
                      
                      if (file != null) {
                          Log.i(TAG, "File uploaded successfully: " + file.getName());
                          Utils.sendSMS(context, address,
                                  context.getResources().getString(R.string.util_sendsms_data_recovery_pass) + " "
                                          + file.getName() + " to Dropbox");
                          deleteFile(file.getName());
                          finish();
                        }
                  }
                  
                  if(smsLogs) {
                      Log.i(TAG, "Generating new file to upload: " + smsLogFileUri);
                      java.io.File file = new java.io.File(smsLogFileUri.getPath());
                      inputStream = new FileInputStream(file);
                      Entry newEntry = mDBApi.putFile(smsLogFileUri.getLastPathSegment(), inputStream, file.length(), null, null);
                      Log.i(TAG, "The uploaded file's rev is: " + newEntry.rev);
                      
                      if (file != null) {
                          Log.i(TAG, "File uploaded successfully: " + file.getName());
                          Utils.sendSMS(context, address,
                                  context.getResources().getString(R.string.util_sendsms_data_recovery_pass) + " "
                                          + file.getName() + " to Dropbox");
                          deleteFile(file.getName());
                          finish();
                        }
                  }     
              } catch (DropboxUnlinkedException e) {
                  Log.e(TAG, "User has unlinked.");
              } catch (DropboxException e) {
                  Log.e(TAG, "Something went wrong while uploading.");
              } catch (FileNotFoundException e) {
                  Log.e(TAG, "File not found.");
              } finally {
                  if (inputStream != null) {
                      try {
                          inputStream.close();
                      } catch (IOException e) {}
                  }
              }
          }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void clearKeys() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor1 = preferences.edit();;
        editor1.putBoolean(AdvancedSettingsFragment.PREFERENCES_DROPBOX_BACKUP_CHECKED, false);
        editor1.putBoolean(SMSDataFragment.PREFERENCES_DATA_ENABLED, false);
        editor1.commit();
        
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private void storeKeys(String key, String secret) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor1 = preferences.edit();;
        editor1.putBoolean(AdvancedSettingsFragment.PREFERENCES_DROPBOX_BACKUP_CHECKED, true);
        editor1.putBoolean(SMSDataFragment.PREFERENCES_DATA_ENABLED, true);
        editor1.commit();
        
        SharedPreferences.Editor editor = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE).edit();;
        editor.putString("dropbox_access_key", key);
        editor.putString("dropbox_secret_key", secret);
        editor.commit();
    }
    
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;
        
        String[] stored = getStoredKeys();
        if (stored != null) {
            Log.e(TAG, "Auth keys were stored, recovering");
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            Log.e(TAG, "Auth keys were not stored");
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }
        
        return session;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Generate new authentication
        mDBApi.getSession().startAuthentication(BackupDropboxAccountsActivity.this);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Logout
        mDBApi.getSession().unlink();
        clearKeys();
        finish();
    }

}
