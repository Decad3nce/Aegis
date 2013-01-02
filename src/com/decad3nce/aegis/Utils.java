package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.AdvancedSettingsFragment;
import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

public class Utils {
    private static final String TAG = "aeGis";

    
    protected static void sendSMS(Context context, String address, String content)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        
        boolean confirmationSMSEnabled  = preferences.getBoolean(
                AdvancedSettingsFragment.PREFERENCES_CONFIRMATION_SMS,
                context.getResources().getBoolean(
                        R.bool.config_default_advanced_enabled_confirmation_sms));
 
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
            new Intent(DELIVERED), 0);
 
        SmsManager sms = SmsManager.getDefault();
        
        if (context instanceof PhoneTrackerActivity) {
            confirmationSMSEnabled = true;
        }
        
        if (confirmationSMSEnabled) {
            sms.sendTextMessage(address, null, content, sentPI, deliveredPI);
        }
    }
    
    @SuppressWarnings("deprecation")
    protected static void alarmNotification(Context context) {
        // Get AudioManager
        AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        NotificationManager mManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.drawable.ic_launcher,
                context.getResources().getString(R.string.receiver_alarm_override),
                System.currentTimeMillis());
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        boolean vibrate = preferences.getBoolean(
                SMSAlarmFragment.PREFERENCES_ALARM_VIBRATE,
                Boolean.parseBoolean(context.getResources().getString(
                        R.string.config_default_alarm_vibrate)));

        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        am.setStreamVolume(AudioManager.STREAM_RING, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

        notification.setLatestEventInfo(context, "aeGis",
                context.getResources().getString(R.string.receiver_alarm_override), pi);
        notification.flags |= Notification.PRIORITY_HIGH;
        notification.sound = Uri
                .parse("android.resource://com.decad3nce.aegis/raw/alarm");
        if (vibrate) {
            notification.vibrate = new long[] { 100, 200, 100, 500 };
        }

        mManager.notify(1336, notification);

    }
    
    protected static void lockDevice(Context context, String body, String activationLockSms, String activationLocateSms) {
        
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (devicePolicyManager
                .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
            String password = preferences.getString(
                    SMSLockFragment.PREFERENCES_LOCK_PASSWORD,
                    context.getResources().getString(
                            R.string.config_default_lock_password));

            if (!body.startsWith(activationLocateSms)) {
                if (body.length() > activationLockSms.length() + 1) {
                    password = body.substring(activationLockSms.length() + 1);
                    password = password
                            .replaceAll(
                                    "([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)",
                                    "").replaceAll("-+", "").trim();
                }
            } else {
                if (body.length() > activationLocateSms.length() + 1) {
                    password = body.substring(activationLocateSms.length() + 1);
                    password = password
                            .replaceAll(
                                    "([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)",
                                    "").replaceAll("-+", "").trim();
                }
            }

            if (password.length() > 0) {
                devicePolicyManager.resetPassword(password, 0);
            }

            try {
                Log.i(TAG, "Locking device");
                devicePolicyManager.lockNow();
                Utils.sendSMS(context, SMSReceiver.address,
                        context.getResources().getString(R.string.util_sendsms_lock_pass) + " " + password);
            } catch (Exception e) {
                Log.wtf(TAG, "Failed to lock device");
                Log.wtf(TAG, e.toString());
                Utils.sendSMS(context, SMSReceiver.address,
                        context.getResources().getString(R.string.util_sendsms_lock_fail) + " " + e.toString());
            }
        }
    }
}
