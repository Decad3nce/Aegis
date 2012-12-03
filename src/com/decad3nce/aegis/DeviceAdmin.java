package com.decad3nce.aegis;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
    }
}