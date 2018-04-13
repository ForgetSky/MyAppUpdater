package com.update.myupdater;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zs.update.UpdateHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intents = new Intent(MainActivity.this, UpdateService.class);
        startService(intents);
        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                checkUpdate();
                cancelUpdateAlarm();
            }
        });
    }

    private void cancelUpdateAlarm() {
        Intent intent =new Intent(this, UpdateService.class);
        intent.setAction("update");
        PendingIntent sender=PendingIntent
                .getService(this, 0, intent, 0);
        AlarmManager alarm=(AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.cancel(sender);
    }
}
