package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLocateFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;
import com.decad3nce.aegis.Fragments.SMSWipeFragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String EXTRA_SMS_PDUS = "pdus";
    private static String address;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);

                Log.i("aeGis", "Received SMS");

                SmsMessage[] messages = getMessagesFromIntent(intent);
                for (SmsMessage sms : messages) {
                    String body = sms.getMessageBody();
                    address = sms.getOriginatingAddress();
                    // TODO: whitelist/blacklist of allowed senders

                    boolean alarmEnabled = preferences.getBoolean(
                            AegisActivity.PREFERENCES_ALARM_ENABLED,
                            context.getResources().getBoolean(
                                    R.bool.config_default_alarm_enabled));
                    boolean lockEnabled = preferences.getBoolean(
                            AegisActivity.PREFERENCES_LOCK_ENABLED,
                            context.getResources().getBoolean(
                                    R.bool.config_default_lock_enabled));
                    boolean wipeEnabled = preferences.getBoolean(
                            AegisActivity.PREFERENCES_WIPE_ENABLED,
                            context.getResources().getBoolean(
                                    R.bool.config_default_wipe_enabled));
                    boolean locateEnabled = preferences.getBoolean(
                            AegisActivity.PREFERENCES_LOCATE_ENABLED,
                            context.getResources().getBoolean(
                                    R.bool.config_default_locate_enabled));

                    String activationAlarmSms = preferences
                            .getString(
                                    SMSAlarmFragment.PREFERENCES_ALARM_ACTIVATION_SMS,
                                    context.getResources()
                                            .getString(
                                                    R.string.config_default_alarm_activation_sms));
                    String activationLockSms = preferences
                            .getString(
                                    SMSLockFragment.PREFERENCES_LOCK_ACTIVATION_SMS,
                                    context.getResources()
                                            .getString(
                                                    R.string.config_default_lock_activation_sms));
                    String activationWipeSms = preferences
                            .getString(
                                    SMSWipeFragment.PREFERENCES_WIPE_ACTIVATION_SMS,
                                    context.getResources()
                                            .getString(
                                                    R.string.config_default_wipe_activation_sms));
                    String activationLocateSms = preferences
                            .getString(
                                    SMSLocateFragment.PREFERENCES_LOCATE_ACTIVATION_SMS,
                                    context.getResources()
                                            .getString(
                                                    R.string.config_default_locate_activation_sms));
                    boolean locateLockPref = preferences.getBoolean(
                            SMSLocateFragment.PREFERENCES_LOCATE_LOCK_PREF,
                            context.getResources().getBoolean(
                                    R.bool.config_default_locate_lock_pref));

                    if (alarmEnabled && body.startsWith(activationAlarmSms)) {
                        try {
                            alarmNotification(context);
                            Log.i("aeGis", "Alarm successfully started");
                        } catch (Exception e) {
                            Log.e("aeGis", "Failed to alarm");
                            Log.e("aeGis", e.toString());
                        }
                    }

                    if (lockEnabled && body.startsWith(activationLockSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                                .getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (devicePolicyManager
                                .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                            String password = preferences
                                    .getString(
                                            SMSLockFragment.PREFERENCES_LOCK_PASSWORD,
                                            context.getResources()
                                                    .getString(
                                                            R.string.config_default_lock_password));
                            if (body.length() > activationLockSms.length() + 1) {
                                password = body.substring(activationLockSms
                                        .length() + 1);
                                // Because fuck your email
                                password = password
                                        .replaceAll(
                                                "([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)",
                                                "").replaceAll("-+", "").trim();
                            }
                            if (password.length() > 0) {
                                devicePolicyManager.resetPassword(password, 0);
                            }
                            try {
                                Log.i("aeGis", "Locking device");
                                devicePolicyManager.lockNow();
                            } catch (Exception e) {
                                Log.i("aeGis", "Failed to lock device");
                                Log.v("aeGis", e.toString());
                            }
                        }
                    }

                    if (wipeEnabled && body.startsWith(activationWipeSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                                .getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (devicePolicyManager
                                .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                            try {
                                Log.i("aeGis", "Wiping device");
                                devicePolicyManager.wipeData(0);
                            } catch (Exception e) {
                                Log.e("aeGis", "Failed to wipe device");
                                Log.e("aeGis", e.toString());
                            }
                        }
                    }

                    if (locateEnabled && body.startsWith(activationLocateSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                                .getSystemService(Context.DEVICE_POLICY_SERVICE);

                        if (locateLockPref
                                && devicePolicyManager
                                        .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                            String password = preferences
                                    .getString(
                                            SMSLockFragment.PREFERENCES_LOCK_PASSWORD,
                                            context.getResources()
                                                    .getString(
                                                            R.string.config_default_lock_password));
                            try {
                                Log.i("aeGis", "Locking device");
                                devicePolicyManager.resetPassword(password, 0);
                                devicePolicyManager.lockNow();
                            } catch (Exception e) {
                                Log.e("aeGis", "Failed to lock device");
                                Log.e("aeGis", e.toString());
                            }
                        }

                        try {
                            Intent locateIntent = new Intent(context,
                                    PhoneTrackerActivity.class);
                            locateIntent
                                    .addFlags(
                                            Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(
                                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                                    .putExtra("address", address);
                            context.startActivity(locateIntent);

                            Log.i("aeGis", "Intent sent");
                        } catch (Exception e) {
                            Log.e("aeGis", "Failed to locate device");
                            Log.e("aeGis", e.toString());
                        }
                    }
                }
            }
        }
    }

    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent
                .getSerializableExtra(EXTRA_SMS_PDUS);
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

    @SuppressWarnings("deprecation")
    private void alarmNotification(Context context) {
        // Get AudioManager
        AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        NotificationManager mManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.drawable.ic_launcher,
                "aeGis has overriden sound settings",
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

        Intent i = new Intent(context, AegisActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

        notification.setLatestEventInfo(context, "aeGis",
                "Sound settings overriden", pi);
        notification.flags |= Notification.PRIORITY_HIGH;
        notification.sound = Uri
                .parse("android.resource://com.decad3nce.aegis/raw/alarm");
        if (vibrate) {
            notification.vibrate = new long[] { 100, 200, 100, 500 };
        }

        mManager.notify(1336, notification);

    }
}
