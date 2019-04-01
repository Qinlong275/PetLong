package com.demo.petlong;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class AppService extends Service{
	private WindowManager windowManager;
	/**
	 * 悬浮窗界面的显示
	 */
	public static LayoutParams layoutParams;
	/**
	 * 属于桌面的应用包名称
	 */
	private List<String> homePackageNames;
	/**
	 * 线程的使用
	 */
	private Handler handler = new Handler();
	private DrawRunnable drawRunnable = new DrawRunnable();
	private ChangRunnable changRunnable = new ChangRunnable();
	/**
	 * 人物类
	 */
	private Person person;
	/**
	 * 每帧运行时间,默认150
	 */
	private int frameTime;
	/**
	 * 随机动画改变时间,默认5秒
	 */
	private int randomTime;
	/**
	 * 注册监听器，监听屏幕的Off和on状态
	 */
	private Receivers receivers = new Receivers();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		init();
		registerReceivers();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(drawRunnable);
		handler.removeCallbacks(changRunnable);
		removePerson();
		unregisterReceiver(receivers);
	}

	/**
	 * 初始化数据
	 */
	private void init() {
		//只获取一次
		homePackageNames = getHomes();
		layoutParams = new LayoutParams();
		windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		//参数设置
		if (Build.VERSION.SDK_INT >= 25) {
			layoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
		}
		layoutParams.format = PixelFormat.RGBA_8888;//图片格式，背景透明
		layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		//
		createPerson();
		//显示
		addPerson();
		handler.post(drawRunnable);
		handler.post(changRunnable);
		frameTime = 150;
		randomTime = 5000;
	}

	/**
	 * 人物的创建，根据用户的设置而显示
	 */
	public void createPerson() {
		try {
			person = new Chopper(this);
			//参数设置
			layoutParams.x = (int) person.getX();
			layoutParams.y = (int) person.getY();
			layoutParams.width = person.getBmpW();//设置大小
			layoutParams.height = person.getBmpH();

		} catch (Exception e) {
			e.printStackTrace();
			showErrorAndClose(e);
		}
	}

	/**
	 * 图像绘制，悬浮窗位置变化
	 */
	class DrawRunnable implements Runnable {
		@Override
		public void run() {
//			System.out.println("PET_LOG: DrawRunnable running");
			long start = System.currentTimeMillis();
			try {
				person.invalidate();	//进行重绘，调用person的onDraw
				layoutParams.x = (int) person.getX();
				layoutParams.y = (int) person.getY();
				windowManager.updateViewLayout(person, layoutParams);
//                //长按启动
//                if(person.getOnPerson() == 1 && System.currentTimeMillis()-person.getTouchDownTime() > 1000){
//                    ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//                    List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
//                    if(!rti.get(0).topActivity.getPackageName().equals("akai.floatView.op.luffy")){
//                        Intent intent = new Intent(AppService.this, MainSettings.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
//                    person.setOnPerson(0);
//                }
				long end = System.currentTimeMillis();
				if (end - start < frameTime) {
					handler.postDelayed(this, frameTime - (end - start));
				} else {
					handler.post(this);
				}
			} catch (Exception e) {
				e.printStackTrace();
				showErrorAndClose(e);
			}
		}
	}

	/**
	 * 动画之间变换
	 */
	class ChangRunnable implements Runnable {
		@Override
		public void run() {
			try {
				person.randomChange();
				handler.postDelayed(this, randomTime);
			} catch (Exception e) {
				e.printStackTrace();
				showErrorAndClose(e);
			}
		}
	}

	/**
	 * 尝试移除显示窗口
	 */
	private void removePerson() {
		try {
			windowManager.removeView(person);
		} catch (Exception e) {
		}
	}

	/**
	 * 尝试添加显示窗口
	 */
	private void addPerson() {
		try {
			Log.i("qinlong","show pet");
			windowManager.addView(person, layoutParams);
		} catch (Exception e) {
		}
	}

	/**
	 * 获得属于桌面的应用的应用包名称
	 *
	 * @return 返回包含所有包名的字符串列表
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		//属性
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.name);
//			System.out.println("name: " + ri.activityInfo.name);
		}
		names.add("akai.pet.one.piece.settings.MainSettings");
		names.add("akai.pet.one.piece.store.StoreActivity");
		return names;
	}

	/**
	 * 判断当前界面是否是桌面
	 */
	private boolean isHome() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		if (rti.size() == 0) {
			return false;
		}
		return homePackageNames.contains(rti.get(0).topActivity.getClassName());
	}

	/**
	 * 广播监听器的注册
	 */
	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receivers, filter);
	}

	/**
	 * 监听当前屏幕是否亮着
	 */
	public static class Receivers extends BroadcastReceiver {
		public Receivers() {
		}

		@Override
		public void onReceive(final Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				Toast.makeText(context, "开机启动", Toast.LENGTH_LONG).show();
				if (true) {
					Handler h = new Handler();
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							context.startService(new Intent(context, AppService.class));
						}
					}, 2 * 60 * 1000);
				}
			}
		}
	}

	/**
	 * 资源错误导致报错，关闭提示并关闭服务
	 */
	private void showErrorAndClose(Exception e) {
		Toast.makeText(AppService.this, "未知错误", Toast.LENGTH_LONG).show();
		stopSelf();
	}
}
