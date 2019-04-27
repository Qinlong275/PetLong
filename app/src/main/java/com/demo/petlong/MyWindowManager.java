package com.demo.petlong;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class MyWindowManager {

	/**
	 * 小悬浮窗View的实例
	 */
	private FloatWindowSmallView smallWindow;

	/**
	 * 控制栏浮窗View的实例
	 */
	private OperateWindow mOperateWindow;

	/**
	 * 火箭发射台的实例
	 */
	private RocketLauncher rocketLauncher;

	/**
	 * 小悬浮窗View的参数
	 */
	private LayoutParams smallWindowParams;

	/**
	 * 底部控制栏View的参数
	 */
	private LayoutParams operatorWindowParams;

	/**
	 * 火箭发射台的参数
	 */
	private LayoutParams launcherParams;

	/**
	 * 用于控制在屏幕上添加或移除悬浮窗
	 */
	private WindowManager mWindowManager;

	/**
	 * 用于获取手机可用内存
	 */
	private ActivityManager mActivityManager;


	//以下为宠物Person部分
	/**
	 * 悬浮窗界面的显示
	 */
	public WindowManager.LayoutParams layoutParams;
	/**
	 * 线程的使用
	 */
	private Handler handler = new Handler();
	private DrawRunnable drawRunnable = new DrawRunnable();
	private ChangRunnable changRunnable = new ChangRunnable();
	private SitRunnable sitRunnable = new SitRunnable();

	/**
	 * 人物类
	 */
	private Chopper person;
	/**
	 * 每帧运行时间,默认150
	 */
	private int frameTime;
	/**
	 * 随机动画改变时间,默认5秒
	 */
	private int randomTime;

	private int fixChangeTime = 5000;

	/**
	 * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
	 */
	public void createSmallWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (smallWindow == null) {
			smallWindow = new FloatWindowSmallView(context, this);
			if (smallWindowParams == null) {
				smallWindowParams = new LayoutParams();
				if (Build.VERSION.SDK_INT >= 25) {
					smallWindowParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
				} else {
					smallWindowParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
				}
				smallWindowParams.format = PixelFormat.RGBA_8888;
				smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				smallWindowParams.width = FloatWindowSmallView.windowViewWidth;
				smallWindowParams.height = FloatWindowSmallView.windowViewHeight;
				smallWindowParams.x = screenWidth / 2;
				smallWindowParams.y = screenHeight / 2;
			}
			smallWindow.setParams(smallWindowParams);
			windowManager.addView(smallWindow, smallWindowParams);
		}
	}

	/**
	 * 将小悬浮窗从屏幕上移除。
	 */
	public void removeSmallWindow(Context context) {
		if (smallWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(smallWindow);
			smallWindow = null;
		}
	}

	/**
	 * 创建底部的操作栏（控制宠物的动画活动）
	 */
	public void createOperateWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (mOperateWindow == null) {
			mOperateWindow = new OperateWindow(context, this);
			if (operatorWindowParams == null) {
				operatorWindowParams = new LayoutParams();
				operatorWindowParams.x = screenWidth / 2 - OperateWindow.viewWidth / 2;
				operatorWindowParams.y = screenHeight - OperateWindow.viewHeight*2;
				operatorWindowParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
				operatorWindowParams.format = PixelFormat.RGBA_8888;
				operatorWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				operatorWindowParams.width = OperateWindow.viewWidth;
				operatorWindowParams.height = OperateWindow.viewHeight;
				operatorWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
			}
			windowManager.addView(mOperateWindow, operatorWindowParams);
		}
	}

	/**
	 * 将操作栏从屏幕上移除。
	 */
	public void removeOperateWindow(Context context) {
		if (mOperateWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(mOperateWindow);
			mOperateWindow = null;
		}
	}

	/**
	 * 创建一个火箭发射台，位置为屏幕底部。
	 */
	public void createLauncher(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (rocketLauncher == null) {
			rocketLauncher = new RocketLauncher(context);
			if (launcherParams == null) {
				launcherParams = new LayoutParams();
				launcherParams.x = screenWidth / 2 - RocketLauncher.width / 2;
				launcherParams.y = screenHeight - RocketLauncher.height;
				launcherParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
				launcherParams.format = PixelFormat.RGBA_8888;
				launcherParams.gravity = Gravity.LEFT | Gravity.TOP;
				launcherParams.width = RocketLauncher.width;
				launcherParams.height = RocketLauncher.height;
			}
			windowManager.addView(rocketLauncher, launcherParams);
		}
	}

	/**
	 * 将火箭发射台从屏幕上移除。
	 */
	public void removeLauncher(Context context) {
		if (rocketLauncher != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(rocketLauncher);
			rocketLauncher = null;
		}
	}

	/**
	 * 更新火箭发射台的显示状态。
	 */
	public void updateLauncher() {
		if (rocketLauncher != null) {
			rocketLauncher.updateLauncherStatus(isReadyToLaunch());
		}
	}


	/**
	 * 创建一个卡通人物
	 */
	public void createPerson(Context context) {
		layoutParams = new LayoutParams();
		WindowManager windowManager = getWindowManager(context);
		//参数设置
		if (Build.VERSION.SDK_INT >= 25) {
			layoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
		}
		layoutParams.format = PixelFormat.RGBA_8888;//图片格式，背景透明
		layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		try {
			person = new Chopper(context);
			//参数设置
			layoutParams.x = (int) person.getX();
			layoutParams.y = (int) person.getY();
			layoutParams.width = person.getBmpW();//设置大小
			layoutParams.height = person.getBmpH();

		} catch (Exception e) {
			e.printStackTrace();
		}
		//显示
		Log.i("qinlong","show pet");
		windowManager.addView(person, layoutParams);
		handler.post(drawRunnable);
		//让宠物自己随机变换动画
		handler.post(changRunnable);
		//每个动作完后都回到坐立状态
		handler.post(sitRunnable);
		frameTime = 150;
		randomTime = 12000;
	}


	/**
	 * 将卡通人物从屏幕上移除。
	 */
	public void removePerson(Context context) {
		if (person != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(person);
			person = null;
		}
	}

	/**
	 * 改变卡通人物的动画
	 */
	public void changePersonState(int state) {
		person.changeState(state);
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
				person.invalidate();    //进行重绘，调用person的onDraw
				layoutParams.x = (int) person.getX();
				layoutParams.y = (int) person.getY();
				mWindowManager.updateViewLayout(person, layoutParams);
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
			}
		}

	}

	/**
	 * 实现每个动作完成后回归到坐立状态
	 */
	class SitRunnable implements Runnable {
		@Override
		public void run() {
			try {
				person.changeState(Chopper.FLAG_SIT);
				handler.postDelayed(this, fixChangeTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
	 *
	 * @param context 可传入应用程序上下文。
	 */
	public void updateUsedPercent(Context context) {
		if (smallWindow != null) {
//			TextView percentView = (TextView) smallWindow
//					.findViewById(R.id.percent);
//			percentView.setText(getUsedPercentValue(context));
		}
	}

	/**
	 * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
	 *
	 * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
	 */
	public boolean isWindowShowing() {
		return smallWindow != null || mOperateWindow != null || person != null;
	}

	/**
	 * 判断小火箭是否准备好发射了。
	 *
	 * @return 当火箭被发到发射台上返回true，否则返回false。
	 */
	public boolean isReadyToLaunch() {
		if (smallWindowParams == null || launcherParams == null) return false;
		if ((smallWindowParams.x > launcherParams.x && smallWindowParams.x
				+ smallWindowParams.width < launcherParams.x
				+ launcherParams.width)
				&& (smallWindowParams.y + smallWindowParams.height > launcherParams.y)) {
			return true;
		}
		return false;
	}

	/**
	 * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
	 *
	 * @param context 必须为应用程序的Context.
	 * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
	 */
	private WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
	 *
	 * @param context 可传入应用程序上下文。
	 * @return ActivityManager的实例，用于获取手机可用内存。
	 */
	private ActivityManager getActivityManager(Context context) {
		if (mActivityManager == null) {
			mActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
		}
		return mActivityManager;
	}

	/**
	 * 计算已使用内存的百分比，并返回。
	 *
	 * @param context 可传入应用程序上下文。
	 * @return 已使用内存的百分比，以字符串形式返回。
	 */
	public String getUsedPercentValue(Context context) {
		String dir = "/proc/meminfo";
		try {
			FileReader fr = new FileReader(dir);
			BufferedReader br = new BufferedReader(fr, 2048);
			String memoryLine = br.readLine();
			String subMemoryLine = memoryLine.substring(memoryLine
					.indexOf("MemTotal:"));
			br.close();
			long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll(
					"\\D+", ""));
			long availableSize = getAvailableMemory(context) / 1024;
			int percent = (int) ((totalMemorySize - availableSize)
					/ (float) totalMemorySize * 100);
			return percent + "%";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "悬浮窗";
	}

	/**
	 * 获取当前可用内存，返回数据以字节为单位。
	 *
	 * @param context 可传入应用程序上下文。
	 * @return 当前可用内存。
	 */
	private long getAvailableMemory(Context context) {
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		getActivityManager(context).getMemoryInfo(mi);
		return mi.availMem;
	}

	public void removeData(Context context){
		removeOperateWindow(context);
		removeLauncher(context);
		removePerson(context);
		removeSmallWindow(context);
		handler.removeCallbacks(drawRunnable);
		handler.removeCallbacks(changRunnable);
		handler.removeCallbacks(sitRunnable);
	}
}
