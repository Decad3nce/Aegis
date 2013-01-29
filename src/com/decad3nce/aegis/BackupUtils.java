package com.decad3nce.aegis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class BackupUtils extends Utils {

    public static Uri getAllCallLogs(ContentResolver cr, Uri internal, Context context, String timeStamp) {
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Uri callUri = Uri.parse("content://call_log/calls");
        Cursor cur = cr.query(callUri, null, null, null, strOrder);
        FileOutputStream fOut = null;
        
        try {
            fOut = context.openFileOutput("call_logs_" + timeStamp + ".txt",
                    Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        
        while (cur.moveToNext()) {
            String callNumber = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
            String callName = cur
                    .getString(cur
                            .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
            String callDate = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.DATE));
            String callType = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.TYPE));
            String isCallNew = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.NEW));
            String duration = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.DURATION));

            String callLogArray[] = {callNumber, callName, callDate, callType,
                    isCallNew, duration};

            for (int i = 0; i < callLogArray.length; i++) {
                try {
                    osw.append(callLogArray[i] + " ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                osw.append("\n");
                osw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return internal;
    }
    
    public static Uri getSMSLogs(ContentResolver cr, Uri internal, Context context, String timeStamp) {
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cur= cr.query(uri, null, null ,null,null);
        FileOutputStream fOut = null;
        String formatStr = "%-20s %-15s %-15s %-15s %-15s%n";
        
        try {
            fOut = context.openFileOutput("sms_logs_" + timeStamp + ".txt",
                    Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        
        while (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndexOrThrow("address")).toString();
            String body = cur.getString(cur.getColumnIndexOrThrow("body")).toString();
            
            String smsLogArray[] = {number, body};
            
            for (int i = 0; i < smsLogArray.length; i++) {
                try {
                    osw.append(smsLogArray[i] + " ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                osw.append("\n");
                osw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return internal;
    }
}
