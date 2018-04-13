package com.update.myupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class UpdateAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("update")) {
            Toast.makeText(context, "update alarm", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "update alarm", Toast.LENGTH_LONG).show();
        }
    }
}