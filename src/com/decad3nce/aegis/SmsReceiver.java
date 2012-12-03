package com.decad3nce.aegis;


import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;
import com.decad3nce.aegis.Fragments.SMSWipeFragment;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    // Statics
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String EXTRA_SMS_PDUS = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                SmsMessage[] messages = getMessagesFromIntent(intent);
                for (SmsMessage sms : messages) {
                    String body = sms.getMessageBody();
                    // TODO: whitelist/blacklist of allowed senders
                    // String address = sms.getOriginatingAddress();

                    boolean alarmEnabled = AegisActivity.alarmEnabled;
                    boolean lockEnabled = AegisActivity.lockEnabled;
                    boolean wipeEnabled = AegisActivity.wipeEnabled;
                    
                    String activationAlarmSms = preferences.getString(SMSAlarmFragment.PREFERENCES_ALARM_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_alarm_activation_sms));
                    String activationLockSms = preferences.getString(SMSLockFragment.PREFERENCES_LOCK_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_lock_activation_sms));
                    String activationWipeSms = preferences.getString(SMSWipeFragment.PREFERENCES_WIPE_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_wipe_activation_sms));
                    
                    if (alarmEnabled && body.startsWith(activationAlarmSms)) {
                        Intent alarmIntent = new Intent(context, AlarmDialogActivity.class);
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        context.startActivity(alarmIntent);
                    }

                    if (lockEnabled && body.startsWith(activationLockSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                            String password = preferences.getString(SMSLockFragment.PREFERENCES_LOCK_PASSWORD,
                                    context.getResources().getString(R.string.config_default_lock_password));
                            if (body.length() > activationLockSms.length() + 1) {
                                password = body.substring(activationLockSms.length() + 1);
                            }
                            if (password.length() > 0) {
                                devicePolicyManager.resetPassword(password, 0);
                            }
                            devicePolicyManager.lockNow();
                        }
                    }

                    if (wipeEnabled && body.startsWith(activationWipeSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                            devicePolicyManager.wipeData(0);
                        }
                    }
                }
            }
        }
    }

    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra(EXTRA_SMS_PDUS);
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
}
