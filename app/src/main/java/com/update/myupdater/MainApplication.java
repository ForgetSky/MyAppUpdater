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


public class MainApplication extends Application {
	public static Context context;

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
