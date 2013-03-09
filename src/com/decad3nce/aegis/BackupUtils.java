package com.decad3nce.aegis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class BackupUtils extends Utils {

    public static Uri getAllCallLogs(ContentResolver cr, Uri internal, Context context, String timeStamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm");
        String[] callLogArray = new String[3];
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
            callLogArray[0] = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
            callLogArray[1] = cur.getString(cur
                    .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
            
            int thirdIndex = cur.getColumnIndex(android.provider.CallLog.Calls.DATE);
            long seconds = cur.getLong(thirdIndex);
            String dateString = formatter.format(new Date(seconds));
            callLogArray[2] = dateString;
        
            writeToOutputStreamArray(callLogArray, osw);
        }
        
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return internal;
    }
    
    public static Uri getSMSLogs(ContentResolver cr, Uri internal, Context context, String timeStamp) {
        String[] smsLogArray = new String[2];
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cur= cr.query(uri, null, null ,null,null);
        FileOutputStream fOut = null;
        
        try {
            fOut = context.openFileOutput("sms_logs_" + timeStamp + ".txt",
                    Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        
        while (cur.moveToNext()) {
            smsLogArray[0] = cur.getString(cur.getColumnIndexOrThrow("address")).toString();
            smsLogArray[1] = cur.getString(cur.getColumnIndexOrThrow("body")).toString();
            
            writeToOutputStreamArray(smsLogArray, osw);
        }
        
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return internal;
    }
    
    private static void writeToOutputStreamArray(String[] array, OutputStreamWriter oswriter) {
        for (int i = 0; i < array.length; i++) {
            try {
                oswriter.append(array[i] + "  ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            oswriter.append("\n");
            oswriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
