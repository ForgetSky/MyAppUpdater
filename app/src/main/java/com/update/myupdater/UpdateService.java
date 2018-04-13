package com.update.myupdater;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.zs.update.UpdateHelper;


/**
 * Created by Saber on 2015/9/15.
 */
public class UpdateService extends Service {

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
        Log.e("jiahui", "update service onStart......");
        checkUpdate();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d("jiahui", "ClientService onDestroy");
        Intent localIntent = new Intent();
        localIntent.setClass(this, UpdateService.class); //销毁时重新启动Service
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startService(localIntent);
//        doVPNLogout();

    }

    private void checkUpdate() {
        Toast.makeText(this, "checkUpdate", Toast.LENGTH_SHORT).show();
        UpdateHelper updateHelper = new UpdateHelper.Builder(this).checkUrl("http://192.168.191.1:8080/check.jsp").isAutoInstall(true) //设置为false需在下载完手动点击安装;默认值为true，下载后自动安装。
                .build();
        updateHelper.check();
    }

}
