package com.update.myupdater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.zs.update.UpdateHelper;


/**
 * Created by Saber on 2015/9/15.
 */
public class UpdateService extends Service {
    private String TAG = "UpdateService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "update service onStart......");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("update")) {
                Log.d(TAG, "onStartCommand : " + action);
                Toast.makeText(this, "update alarm", Toast.LENGTH_LONG).show();
                checkUpdate();
            } else {
                updateAlarm();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "ClientService onDestroy");
        Intent localIntent = new Intent();
        localIntent.setClass(this, UpdateService.class); //销毁时重新启动Service
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startService(localIntent);
    }

    private void checkUpdate() {
        Toast.makeText(this, "checkUpdate", Toast.LENGTH_SHORT).show();
        UpdateHelper updateHelper = new UpdateHelper.Builder(this).
                checkUrl("http://192.168.191.1:8080/check.jsp").
                isAutoInstall(true) //设置为false需在下载完手动点击安装;默认值为true，下载后自动安装。
                .build();
        updateHelper.check();
    }

    private void updateAlarm() {
        Log.d(TAG, "updateAlarm");
        Intent intent = new Intent(this, UpdateService.class);
        intent.setAction("update");
        PendingIntent sender = PendingIntent.getService(this, 0, intent, 0);

        //设置一个PendingIntent对象，发送广播
//        AlarmManager.INTERVAL_DAY
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60 * 1000, sender);
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
