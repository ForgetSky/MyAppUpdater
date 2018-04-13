/**   
 * @Title: MainApplication.java 
 * @Package com.ssdj.cloudroom 
 * @Description: 星星应用类
 * @author Sunocean
 * @email sunocean2008@sina.com
 * @date 2015年3月5日 下午1:57:52 
 * @version V1.0   
 */
package com.update.myupdater;

import android.app.Application;
import android.content.Context;
import android.os.Vibrator;


public class MainApplication extends Application {
	public static Context context;
	public Vibrator mVibrator;
	/**
	 * 系统版本类型，为0表示不支持--user参数（一般为2.3以下） 为1，表示支持--user参数（一般为4.0以上）
	 * 4.0以及4.1系统可能同时支持两种命令格式，以带参数的为优先
	 */
	public static int systemVersion;
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();

//		startDaemonService();
		

	}
	
	/**
	 * @description 启动守护进程
	 */
//	private void startDaemonService() {
//		Intent intent = new Intent(this, DaemonService.class);
//		intent.setAction(DaemonService.START_DAEMON_ACTION);
//		this.startService(intent);
//	}


}
