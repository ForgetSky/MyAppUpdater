package com.zs.update;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zs.update.listener.OnUpdateListener;
import com.zs.update.pojo.UpdateInfo;
import com.zs.update.utils.HttpRequest;
import com.zs.update.utils.JSONHandler;
import com.zs.update.utils.NetWorkUtils;
import com.zs.update.utils.URLUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by <a href="http://www.shelwee.com">Shelwee</a> on 14-5-8.<br/>
 * Usage:
 * <pre>
 * UpdateManager updateManager = new UpdateManager.Builder(this)
 * 		.checkUrl(&quot;http://localhost/examples/version.jsp&quot;)
 * 		.isAutoInstall(false)
 * 		.build();
 * updateManager.check();
 * </pre>
 *
 * @author Shelwee(<a href="http://www.shelwee.com">http://www.shelwee.com</a>)
 * @version 1.0
 */
public class UpdateHelper {

    private Context mContext;
    private String checkUrl;
    private boolean isAutoInstall;
    private boolean isHintVersion;
    private OnUpdateListener updateListener;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder ntfBuilder;
    private static final int REQUEST_PERMISSION_CODE = 0x1;
    private static final int COMPLETE_DOWNLOAD_APK = 0x2;
    private static final int DOWNLOAD_NOTIFICATION_ID = 0x3;
    private static final String PATH = Environment
            .getExternalStorageDirectory().getPath();
    private static final String SUFFIX = ".apk";
    private static final String APK_PATH = "APK_PATH";
    private static final String APP_NAME = "APP_NAME";
    private SharedPreferences preferences_update;
    private Notification mNotification;
    private String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private HashMap<String, String> cache = new HashMap<String, String>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COMPLETE_DOWNLOAD_APK:
                    if (UpdateHelper.this.isAutoInstall) {
//                        installApk(Uri.parse("file://" + cache.get(APK_PATH)));
                        installSilent(cache.get(APK_PATH));
                    } else {
//                        ntfBuilder.setContentTitle(cache.get(APP_NAME))
//                                .setContentText(mContext.getText(R.string.finished_install))
//                                .setTicker(mContext.getText(R.string.download_done))
//                                .setAutoCancel(true)
//                                .setProgress(0, 0 ,true);
//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setDataAndType(
//                                Uri.parse("file://" + cache.get(APK_PATH)),
//                                "application/vnd.android.package-archive");
//                        PendingIntent pendingIntent = PendingIntent.getActivity(
//                                mContext, 0, intent, 0);
//                        ntfBuilder.setContentIntent(pendingIntent);
//                        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID,
//                                ntfBuilder.build());
                    }
                    break;
            }
        }

    };

    private UpdateHelper(Builder builder) {
        this.mContext = builder.context;
        this.checkUrl = builder.checkUrl;
        this.isAutoInstall = builder.isAutoInstall;
        this.isHintVersion = builder.isHintNewVersion;
        preferences_update = mContext.getSharedPreferences("Updater",
                Context.MODE_PRIVATE);
        notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 检查app是否有新版本，check之前先Builer所需参数
     */
    public void check() {
        check(null);
    }

    public void check(OnUpdateListener listener) {
        if (listener != null) {
            this.updateListener = listener;
        }
        if (mContext == null) {
            Log.e("NullPointerException", "The context must not be null.");
            return;
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ActivityCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions((Activity) mContext, new String[]{permission}, REQUEST_PERMISSION_CODE);
//            }else {
//                startCheck();
//            }
//        }else {
            startCheck();
//        }
    }

    private void startCheck() {
        AsyncCheck asyncCheck = new AsyncCheck();
        asyncCheck.execute(checkUrl);
    }

    /**
     * 2014-10-27新增流量提示框，当网络为数据流量方式时，下载就会弹出此对话框提示
     *
     * @param updateInfo
     */
    private void showNetDialog(final UpdateInfo updateInfo) {
        AlertDialog.Builder netBuilder = new AlertDialog.Builder(mContext);
        netBuilder.setTitle(mContext.getText(R.string.download_tips));
        netBuilder.setMessage(mContext.getText(R.string.download_tips_network));
        netBuilder.setNegativeButton(mContext.getText(R.string.download_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        netBuilder.setPositiveButton(mContext.getText(R.string.download_continue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AsyncDownLoad asyncDownLoad = new AsyncDownLoad();
                        asyncDownLoad.execute(updateInfo);
                    }
                });
        AlertDialog netDialog = netBuilder.create();
        netDialog.setCanceledOnTouchOutside(false);
        netDialog.show();
    }

    /**
     * 弹出提示更新窗口
     *
     * @param updateInfo
     */
    private void showUpdateUI(final UpdateInfo updateInfo) {
        NetWorkUtils netWorkUtils = new NetWorkUtils(mContext);
        final int type = netWorkUtils.getNetType();
        if (updateInfo.isForceUpgrade()){
//            if (type != 1) {
//                showNetDialog(updateInfo);
//            } else {
                AsyncDownLoad asyncDownLoad = new AsyncDownLoad();
                asyncDownLoad.execute(updateInfo);
//            }
        }else {
            AlertDialog.Builder upDialogBuilder = new AlertDialog.Builder(mContext);
            upDialogBuilder.setTitle(updateInfo.getUpdateTips());
            upDialogBuilder.setMessage(updateInfo.getChangeLog());
            upDialogBuilder.setNegativeButton(mContext.getText(R.string.next_time),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            upDialogBuilder.setPositiveButton(mContext.getText(R.string.download),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (type != 1) {
                                showNetDialog(updateInfo);
                            } else {
                                AsyncDownLoad asyncDownLoad = new AsyncDownLoad();
                                asyncDownLoad.execute(updateInfo);
                            }
                        }
                    });
            AlertDialog updateDialog = upDialogBuilder.create();
            updateDialog.setCanceledOnTouchOutside(false);
            updateDialog.show();
        }
    }

    /**
     * 获取当前app版本
     *
     * @return
     */
    private PackageInfo getPackageInfo() {
        PackageInfo pinfo = null;
        if (mContext != null) {
            try {
                pinfo = mContext.getPackageManager().getPackageInfo(
                        mContext.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pinfo;
    }

    /**
     * 检查更新任务
     */
    private class AsyncCheck extends AsyncTask<String, Integer, UpdateInfo> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (UpdateHelper.this.updateListener != null) {
                UpdateHelper.this.updateListener.onStartCheck();
            }
        }

        @Override
        protected UpdateInfo doInBackground(String... params) {
            UpdateInfo updateInfo = null;
            if (params.length == 0) {
                Log.e("NullPointerException",
                        " Url parameter must not be null.");
                return null;
            }
            String url = params[0];
            if (!URLUtils.isNetworkUrl(url)) {
                Log.e("IllegalArgument", "The URL is invalid.");
                return null;
            }
            try {
                updateInfo = JSONHandler.toUpdateInfo(new HttpRequest(url).get());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return updateInfo;
        }

        @Override
        protected void onPostExecute(UpdateInfo updateInfo) {
            super.onPostExecute(updateInfo);
//            SharedPreferences.Editor editor = preferences_update.edit();
            if (mContext != null && updateInfo != null) {
                int newVersionCode = Integer.parseInt(updateInfo.getVersionCode());
                String newpkgName = updateInfo.getPackageName();

                if (!TextUtils.isEmpty(newpkgName)) {
                    if (isAppInstalled(newpkgName)) {
                        if (newVersionCode > getVersionCode(newpkgName)) {
                            showUpdateUI(updateInfo);
                            Toast.makeText(mContext, newpkgName + "发现新版本，版本号：" + newVersionCode
                                    + "当前版本为：" + getVersionCode(newpkgName), Toast.LENGTH_SHORT).show();
//                            editor.putBoolean("hasNewVersion", true);
//                            editor.putString("latestVersionCode", updateInfo.getVersionCode());
//                            editor.putString("latestVersionName", newpkgName);
                        } else {
                            if (isHintVersion) {
                                Toast.makeText(mContext, newpkgName + "当前已是最新版, 版本号: " +getVersionCode(newpkgName) , Toast.LENGTH_SHORT).show();
                            }
//                            editor.putBoolean("hasNewVersion", false);
                        }
                    } else {
                        Toast.makeText(mContext, "应用未安装: " +getPackageInfo().versionCode , Toast.LENGTH_SHORT).show();
                        AsyncDownLoad asyncDownLoad = new AsyncDownLoad();
                        asyncDownLoad.execute(updateInfo);
                    }
                }


            }
//            else {

//                if (isHintVersion) {
//                    Toast.makeText(mContext, R.string.was_latest_version, Toast.LENGTH_LONG).show();
//                }
//            }
//            editor.putString("currentVersionCode", getPackageInfo().versionCode + "");
//            editor.putString("currentVersionName", getPackageInfo().versionName);
//            editor.apply();
            if (UpdateHelper.this.updateListener != null) {
                UpdateHelper.this.updateListener.onFinishCheck(updateInfo);
            }
        }
    }


    private boolean isAppInstalled(String pkgName) {
            PackageManager pckMan = mContext.getPackageManager();
            List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);
            if (packageInfo != null && !packageInfo.isEmpty()) {
                for (PackageInfo pInfo : packageInfo) {
                    if (pkgName.equals(pInfo.packageName)) {
                        return true;
                    }
                }
            }
        return false;
    }

    /**
     * 获取程序的版本号
     * @param packname
     * @return
     */
    public int getVersionCode(String packname){
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo packinfo = pm.getPackageInfo(packname, 0);
            return packinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 异步下载app任务
     */
    private class AsyncDownLoad extends AsyncTask<UpdateInfo, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
//            notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);
//            PendingIntent contentIntent = PendingIntent.getActivity(mContext,
//                    0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
//            ntfBuilder = new NotificationCompat.Builder(mContext)
//                    .setOngoing(true)
//                    .setSmallIcon(mContext.getApplicationInfo().icon)
//                    .setTicker(mContext.getResources().getText(R.string.start_download) + "...")
//                    .setContentIntent(contentIntent)
//                    .setProgress(100, 0, false);
//            mNotification = ntfBuilder.build();
//            notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, mNotification);
        }

        @Override
        protected Boolean doInBackground(UpdateInfo... params) {
            String apkUrl = params[0].getApkUrl();
            HttpRequest request = new HttpRequest(apkUrl);
            try {
                long apkTotalLength = request.getContentLength();
                String apkName = params[0].getAppName()
                        + params[0].getVersionName() + SUFFIX;
                cache.put(APP_NAME, params[0].getAppName());
                cache.put(APK_PATH,
                        PATH + File.separator + params[0].getAppName()
                                + File.separator + apkName);
                File savePath = new File(PATH + File.separator
                        + params[0].getAppName());
                if (!savePath.exists())
                    savePath.mkdirs();
                File apkFile = new File(savePath, apkName);
                if (apkFile.exists() && apkFile.length() == apkTotalLength) {
                    return true;
                }
                FileOutputStream fos = new FileOutputStream(apkFile);
                InputStream inputStream = request.get();
                if (inputStream == null) {
                    return false;
                }
                byte[] buf = new byte[2048];
                int count = 0;
                int length = 0;
                int progress = 0;
                while ((length = inputStream.read(buf)) > 0) {
                    fos.write(buf, 0, length);
                    count += length;
                    if ((progress == 0)
                            || (int) (count * 100 / apkTotalLength) > progress) {
                        progress += 1;
                        publishProgress(progress);
                    }
                    if (UpdateHelper.this.updateListener != null) {
                        UpdateHelper.this.updateListener
                                .onDownloading(progress);
                    }
                }
                inputStream.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            ntfBuilder.setContentTitle(cache.get(APP_NAME))
//                      .setContentText(mContext.getResources().getText(R.string.download_progress) + ": " + values[0] + "%")
//                      .setProgress(100, values[0], false);
//            mNotification = ntfBuilder.build();
//            notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, mNotification);
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            if (flag) {
                handler.obtainMessage(COMPLETE_DOWNLOAD_APK).sendToTarget();
                if (UpdateHelper.this.updateListener != null) {
                    UpdateHelper.this.updateListener.onFinshDownload();
                }
            } else {
                Log.e("Error", "check your network connction");
            }
        }
    }

    public static class Builder {
        private Context context;
        private String checkUrl;
        private boolean isAutoInstall = true;
        private boolean isHintNewVersion = true;

        public Builder(Context ctx) {
            this.context = ctx;
        }

        /**
         * 检查是否有新版本App的URL接口路径
         *
         * @param checkUrl
         * @return
         */
        public Builder checkUrl(String checkUrl) {
            this.checkUrl = checkUrl;
            return this;
        }

        /**
         * 是否需要自动安装, 不设置默认自动安装
         *
         * @param isAuto true下载完成后自动安装，false下载完成后需在通知栏手动点击安装
         * @return
         */
        public Builder isAutoInstall(boolean isAuto) {
            this.isAutoInstall = isAuto;
            return this;
        }

        /**
         * 当没有新版本时，是否Toast提示
         *
         * @param isHint
         * @return true提示，false不提示
         */
        public Builder isHintNewVersion(boolean isHint) {
            this.isHintNewVersion = isHint;
            return this;
        }

        /**
         * 构造UpdateManager对象
         *
         * @return
         */
        public UpdateHelper build() {
            return new UpdateHelper(this);
        }
    }

    private void installApk(Uri data) {
        if (mContext != null) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(data, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
            if (notificationManager != null) {
                notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);
            }
        } else {
            Log.e("NullPointerException", "The context must not be null.");
        }
    }

    /**
     * install slient
     *
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int installSilent(String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = { "pm", "install", "-r", filePath };
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Log.d("test-test", "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }
}