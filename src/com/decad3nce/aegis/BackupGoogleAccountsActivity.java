package com.decad3nce.aegis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockActivity;
import com.decad3nce.aegis.Fragments.BackupAccountsDialogFragment;
import com.decad3nce.aegis.Fragments.SMSDataFragment;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class BackupGoogleAccountsActivity extends SherlockActivity implements BackupAccountsDialogFragment.NoticeDialogListener{
    private static final String TAG = "aeGis";
    
    final static private String ACCOUNT_PREFS_NAME = "google_prefs";
    final static private String CHOSEN_GOOGLE_ACCOUNT = "chosen_google_account_name";
            
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final int REQUEST_AUTHORIZATION = 2;
    static final int UPLOAD_CALL_LOGS = 3;

    private ProgressBar progressBar;
    private Context context;
    private static Uri callLogFileUri;
    private static Uri smsLogFileUri;
    private static Drive service;
    private boolean callLogs;
    private boolean smsLogs;
    private String address;
    private ContentResolver cr;
    private GoogleAccountCredential credential;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.backup_layout);
      
      progressBar = (ProgressBar) findViewById(R.id.progressBar);
      progressBar.setVisibility(ProgressBar.VISIBLE);
      
      context = this;
      Intent intent;
      cr = getContentResolver();
      credential = GoogleAccountCredential.usingOAuth2(this,DriveScopes.DRIVE);
      
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
          
      } catch(Exception e) {
          recoverData();
      }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
      switch (requestCode) {
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
          final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          if (accountName != null) {
            Log.i(TAG, "REQUEST ACCOUNT PICKER");
            credential.setSelectedAccountName(accountName);
            service = getDriveService(credential);
            storeGoogleAccounts(accountName);
            Log.i(TAG, "Account saved: " + CHOSEN_GOOGLE_ACCOUNT + " " + accountName);
            getFirstAuthInAsync();
          }
        }
        break;
      case REQUEST_AUTHORIZATION:
        if (resultCode == Activity.RESULT_OK) {
          saveFileToDrive();
        } else {
          startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
        break;
      }
    }

    void getFirstAuthInAsync() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                Log.i(TAG, "Getting first auth");
                getFirstAuth();
                return null;
            }
        };
        task.execute((Void)null);
    }

    void getFirstAuth() {
        String token; 
        
        Log.i(TAG, "Authing with account: " + getGoogleAccount());
        try {
            token = GoogleAuthUtil.getToken(BackupGoogleAccountsActivity.this, getGoogleAccount(),
                    "Backup data");
        } catch (IOException e) {
            Log.i(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        } catch (GoogleAuthException e) {;
            e.printStackTrace();
            Log.i(TAG, "Exception: " + e.toString());
        }
        if(!isAegisFolderAvailable()) {
            createAegisFolder();
        }
        finish();
    }

    private void createAegisFolder() {    
        Log.i(TAG, "Creating aeGis folder");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
              try {               
                  File body = new File();
                  body.setTitle("aeGis Backup");
                  body.setDescription("Backup stored by aeGis");
                  body.setMimeType("application/vnd.google-apps.folder");
                  File file = service.files().insert(body).execute();
                if (file != null) {
                  finish();
                }
              } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          });
          t.start();
    }
    
    private static boolean isAegisFolderAvailable() {
        
        if(getAegisFolder() == null) {
            return false;
        }
        
        return true;
    }
    
    private static String getAegisFolder() {
        Files.List request = null;
        String folderID = null;
            try {
                request = service.files().list().setQ("mimeType= 'application/vnd.google-apps.folder' and title = 'aeGis Backup' and trashed = false");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            
            do {
                    FileList files;
                    try {
                        files = request.execute();
                        for (File file : files.getItems()) {
                            folderID = file.getId();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
            } while (request.getPageToken() != null && request.getPageToken().length() > 0);
            
            //Log.i(TAG, "FolderID: " + folderID);
        return folderID;
    }

    private void recoverData() {
        Log.i(TAG, "Recovering data");
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        
        callLogs = preferences.getBoolean(SMSDataFragment.PREFERENCES_BACKUP_CALL_LOGS, this.getResources().getBoolean(R.bool.config_default_data_backup_call_logs));
        smsLogs = preferences.getBoolean(SMSDataFragment.PREFERENCES_BACKUP_SMS_LOGS, this.getResources().getBoolean(R.bool.config_default_data_backup_sms_logs));
        
        credential.setSelectedAccountName(getGoogleAccount());
        service = getDriveService(credential);
        
        if (callLogs) {
            Log.i(TAG, "Recovering call logs data");
            java.io.File internalFile = getFileStreamPath("call_logs_" + timeStamp + ".txt");
            Uri internalCallLogs = Uri.fromFile(internalFile);
            callLogFileUri = BackupUtils.getAllCallLogs(cr, internalCallLogs, this, timeStamp);
        }

        if (smsLogs) {
            Log.i(TAG, "Recovering sms logs data");
            java.io.File internalFile = getFileStreamPath("sms_logs_" + timeStamp + ".txt");
            Uri internalSMSLogs = Uri.fromFile(internalFile);
            smsLogFileUri = BackupUtils.getSMSLogs(cr, internalSMSLogs, this, timeStamp);
        }
        
        saveFileToDrive();
    }
    
    private void storeGoogleAccounts(String accountName) {
        SharedPreferences.Editor editor = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(CHOSEN_GOOGLE_ACCOUNT, accountName);
        editor.commit();
    }
    
    private String getGoogleAccount() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE);
        String googleAccount = prefs.getString(CHOSEN_GOOGLE_ACCOUNT, null);
        return googleAccount;
    }
    
    private void clearGoogleAccounts() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    private void saveFileToDrive() {
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
                if(!isAegisFolderAvailable()) {
                    createAegisFolder();
                }
                
                FileContent mediaContent = null;
                File body = new File();
                File file = null;
              
              if(callLogs) {
                  Log.i(TAG, "Generating new file to upload: " + callLogFileUri);
                  java.io.File callFileContent = new java.io.File(callLogFileUri.getPath());
                  mediaContent = new FileContent("text/plain", callFileContent);
                  
                  body.setTitle(callFileContent.getName());
                  body.setParents(Arrays.asList(new ParentReference().setId(getAegisFolder())));
                  body.setMimeType("text/plain");
                  
                  file = service.files().insert(body, mediaContent).execute();
                  
                  if (file != null) {
                      Log.i(TAG, "File uploaded successfully: " + file.getTitle());
                      Utils.sendSMS(context, address,
                              context.getResources().getString(R.string.util_sendsms_data_recovery_pass) + " "
                                      + file.getTitle() + " to Google Drive");
                      deleteFile(file.getTitle());
                      finish();
                    }
              }
              
              if(smsLogs) {
                  Log.i(TAG, "Generating new file to upload: " + smsLogFileUri);
                  java.io.File smsFileContent = new java.io.File(smsLogFileUri.getPath());
                  mediaContent = new FileContent("text/plain", smsFileContent);
                  
                  body.setTitle(smsFileContent.getName());
                  body.setParents(Arrays.asList(new ParentReference().setId(getAegisFolder())));
                  body.setMimeType("text/plain");
                  
                  file = service.files().insert(body, mediaContent).execute();
                  
                  if (file != null) {
                      Log.i(TAG, "File uploaded successfully: " + file.getTitle());
                      Utils.sendSMS(context, address,
                              context.getResources().getString(R.string.util_sendsms_data_recovery_pass) + " "
                                      + file.getTitle() + " to Google Drive");
                      deleteFile(file.getTitle());
                      finish();
                    }
              }

            } catch (UserRecoverableAuthIOException e) {
              Log.i(TAG, "Exception: " + e.toString());
              startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
              Log.i(TAG, "Exception: " + e.toString());
              e.printStackTrace();
              Utils.sendSMS(context, address,
                      context.getResources().getString(R.string.util_sendsms_data_recovery_fail));
            }
          }
        });
        t.start();
    }

      private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
            .build();
      }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Generate new login session
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        clearGoogleAccounts();
        finish();
    }
}
