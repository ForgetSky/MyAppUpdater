package com.update.myupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ReBootReceiver","ReBootReceiver...");
        Intent intents = new Intent(context, UpdateService.class);
        context.startService(intents);
    }
}
