package com.update.myupdater;

import android.app.Activity;
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

        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
    }

    private void checkUpdate() {
        Toast.makeText(MainActivity.this, "checkUpdate", Toast.LENGTH_SHORT).show();
        UpdateHelper updateHelper = new UpdateHelper.Builder(this)
                .checkUrl("http://192.168.191.1:8080/check.jsp")
                .isAutoInstall(true) //设置为false需在下载完手动点击安装;默认值为true，下载后自动安装。
                .build();
        updateHelper.check();
    }
}
