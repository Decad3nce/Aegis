package com.decad3nce.aegis;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class Utils {

    
    protected static void sendSMS(Context context, String address, String content)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
            new Intent(DELIVERED), 0);
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(address, null, content, sentPI, deliveredPI);        
    }
}
