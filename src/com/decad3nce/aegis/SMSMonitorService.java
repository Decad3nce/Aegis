package com.decad3nce.aegis;

import com.decad3nce.aegis.Fragments.SMSAlarmFragment;
import com.decad3nce.aegis.Fragments.SMSLocateFragment;
import com.decad3nce.aegis.Fragments.SMSLockFragment;
import com.decad3nce.aegis.Fragments.SMSWipeFragment;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSMonitorService extends Service {

    private static final String TAG = "AEGIS";

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private BroadcastReceiver SmsReceiver;
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String EXTRA_SMS_PDUS = "pdus";
    protected static String address;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            oldNotification();
        } else {
            buildNewNotification();
        }

        return START_STICKY;
    }

    @TargetApi(16)
    private void buildNewNotification() {
        Intent i = new Intent(this, AegisActivity.class);
        String msgText = "SMSMonitor service is running";
        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        Builder builder = new Notification.Builder(this);
        builder.setContentTitle("aeGis").setContentText("SMSMonitor")
                .setSmallIcon(R.drawable.ic_launcher).setAutoCancel(false)
                .setContentIntent(pi);

        Notification notification = new Notification.BigTextStyle(builder)
                .bigText(msgText).build();

        mManager.notify(1337, notification);
        return;
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @SuppressWarnings("deprecation")
    public void oldNotification() {
        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_launcher,
                "aeGis service is running", System.currentTimeMillis());
        Intent i = new Intent(this, AegisActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        notification.setLatestEventInfo(this, "aeGis", "SMSMonitor", pi);
        notification.flags |= Notification.PRIORITY_HIGH;
        mManager.notify(1337, notification);
        return;
    }

    @SuppressWarnings("deprecation")
    private void alarmNotification() {
        // Get AudioManager
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.drawable.ic_launcher,
                "aeGis has overriden sound settings",
                System.currentTimeMillis());
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        boolean vibrate = preferences.getBoolean(
                SMSAlarmFragment.PREFERENCES_ALARM_VIBRATE,
                Boolean.parseBoolean(getResources().getString(
                        R.string.config_default_alarm_vibrate)));

        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        am.setStreamVolume(AudioManager.STREAM_RING, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        Intent i = new Intent(this, AegisActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        notification.setLatestEventInfo(this, "aeGis",
                "Sound settings overriden", pi);
        notification.flags |= Notification.PRIORITY_HIGH;
        notification.sound = Uri
                .parse("android.resource://com.decad3nce.aegis/raw/alarm");
        if (vibrate) {
            notification.vibrate = new long[] { 100, 200, 100, 500 };
        }

        mManager.notify(1336, notification);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "New SMSMonitor service has been started");

        final IntentFilter mFilter = new IntentFilter();
        mFilter.setPriority(999);
        mFilter.addAction(ACTION);

        this.SmsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        SharedPreferences preferences = PreferenceManager
                                .getDefaultSharedPreferences(context);

                        SmsMessage[] messages = getMessagesFromIntent(intent);
                        for (SmsMessage sms : messages) {
                            String body = sms.getMessageBody();
                            address = sms.getOriginatingAddress();
                            // TODO: whitelist/blacklist of allowed senders

                            boolean alarmEnabled = AegisActivity.alarmEnabled;
                            boolean lockEnabled = AegisActivity.lockEnabled;
                            boolean wipeEnabled = AegisActivity.wipeEnabled;
                            boolean locateEnabled = AegisActivity.locateEnabled;

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
                            boolean locateLockPref = preferences
                                    .getBoolean(
                                            SMSLocateFragment.PREFERENCES_LOCATE_LOCK_PREF,
                                            context.getResources()
                                                    .getBoolean(
                                                            R.bool.config_default_locate_lock_pref));

                            if (alarmEnabled && body.startsWith(activationAlarmSms)) {
                                alarmNotification();
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
                                    if (body.length() > activationLockSms
                                            .length() + 1) {
                                        password = body
                                                .substring(activationLockSms
                                                        .length() + 1);
                                    }
                                    if (password.length() > 0) {
                                        devicePolicyManager.resetPassword(
                                                password, 0);
                                    }
                                    devicePolicyManager.lockNow();
                                }
                            }

                            if (wipeEnabled && body.startsWith(activationWipeSms)) {
                                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                                        .getSystemService(Context.DEVICE_POLICY_SERVICE);
                                if (devicePolicyManager
                                        .isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                                    devicePolicyManager.wipeData(0);
                                }
                            }

                            if (locateEnabled && body.startsWith(activationLocateSms)) {
                                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                                        .getSystemService(Context.DEVICE_POLICY_SERVICE);
                                
                                if (locateLockPref && devicePolicyManager.isAdminActive(AegisActivity.DEVICE_ADMIN_COMPONENT)) {
                                    String password = preferences.getString(SMSLockFragment.PREFERENCES_LOCK_PASSWORD,
                                                    context.getResources().getString(R.string.config_default_lock_password));
                                    
                                    devicePolicyManager.resetPassword(password,0);
                                    devicePolicyManager.lockNow();
                                }
                                
                                Intent locateIntent = new Intent(context,
                                        PhoneTrackerActivity.class);
                                locateIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                locateIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                context.startActivity(locateIntent);
                            }
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.SmsReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "SMSMonitor service has been stopped");
        this.unregisterReceiver(this.SmsReceiver);
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
}
