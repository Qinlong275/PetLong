package com.demo.petlong;

import java.lang.reflect.Field;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.petlong.note.NoteActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class FloatWindowSmallView extends LinearLayout implements View.OnClickListener{

	/**
	 * 记录小悬浮窗的宽度
	 */
	public static int windowViewWidth;

	/**
	 * 记录小悬浮窗的高度
	 */
	public static int windowViewHeight;

	/**
	 * 记录系统状态栏的高度
	 */
	private static int statusBarHeight;

	/**
	 * 用于更新小悬浮窗的位置
	 */
	private WindowManager windowManager;

	/**
	 * 小悬浮窗的布局
	 */
	private RelativeLayout smallWindowLayout;

	/**
	 * 小火箭控件
	 */
	private ImageView rocketImg;

	/**
	 * 小悬浮窗的参数
	 */
	private WindowManager.LayoutParams mParams;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录手指按下时在小悬浮窗的View上的横坐标的值
	 */
	private float xInView;

	/**
	 * 记录手指按下时在小悬浮窗的View上的纵坐标的值
	 */
	private float yInView;

	/**
	 * 记录小火箭的宽度
	 */
	private int rocketWidth;

	/**
	 * 记录小火箭的高度
	 */
	private int rocketHeight;

	/**
	 * 记录当前手指是否按下
	 */
	private boolean isPressed;

	private CircleImageView addButton;
	private CircleImageView takePhoto;
	private CircleImageView openAlbum;

	private boolean start = true;
	private MyWindowManager mManager;

	public FloatWindowSmallView(Context context, MyWindowManager manager) {
		super(context);
		mManager = manager;
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
		smallWindowLayout = (RelativeLayout) findViewById(R.id.small_window_layout);
		windowViewWidth = smallWindowLayout.getLayoutParams().width;
		windowViewHeight = smallWindowLayout.getLayoutParams().height;
		rocketImg = (ImageView) findViewById(R.id.rocket_img);
		rocketWidth = rocketImg.getLayoutParams().width;
		rocketHeight = rocketImg.getLayoutParams().height;
		addButton = (CircleImageView) findViewById(R.id.icon_add);
		takePhoto = (CircleImageView) findViewById(R.id.icon_take_photo);
		openAlbum = (CircleImageView) findViewById(R.id.icon_album);
//		addButton.setOnClickListener(this);
		takePhoto.setOnClickListener(this);
		openAlbum.setOnClickListener(this);


	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isPressed = true;
			// 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
			xInView = event.getX();
			yInView = event.getY();
			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - getStatusBarHeight();
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			break;
		case MotionEvent.ACTION_MOVE:
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			// 手指移动的时候更新小悬浮窗的状态和位置
			updateViewPosition();
			updateViewStatus();
			break;
		case MotionEvent.ACTION_UP:
			isPressed = false;
			if (mManager.isReadyToLaunch()) {
				launchRocket();
			} else {
				updateViewStatus();
				// 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
				if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
					if (start) {
						takePhoto.setVisibility(VISIBLE);
						openAlbum.setVisibility(VISIBLE);
						open();
					} else {
						close();
						postDelayed(new Runnable() {
							@Override
							public void run() {
								takePhoto.setVisibility(INVISIBLE);
								openAlbum.setVisibility(INVISIBLE);
							}
						}, 500);

					}
				}
			}
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
	 * 
	 * @param params
	 *            小悬浮窗的参数
	 */
	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	/**
	 * 用于发射小火箭。
	 */
	private void launchRocket() {
		mManager.removeLauncher(getContext());
		new LaunchTask().execute();
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		mParams.x = (int) (xInScreen - xInView) + 300;
		mParams.y = (int) (yInScreen - yInView);
		windowManager.updateViewLayout(this, mParams);
		mManager.updateLauncher();
	}

	/**
	 * 更新View的显示状态，判断是显示悬浮窗还是小火箭。
	 */
	private void updateViewStatus() {
		if (isPressed && rocketImg.getVisibility() != View.VISIBLE) {
			mParams.width = rocketWidth;
			mParams.height = rocketHeight;
			windowManager.updateViewLayout(this, mParams);
			smallWindowLayout.setVisibility(View.GONE);
			rocketImg.setVisibility(View.VISIBLE);
			mManager.createLauncher(getContext());
		} else if (!isPressed) {
			mParams.width = windowViewWidth;
			mParams.height = windowViewHeight;
			windowManager.updateViewLayout(this, mParams);
			smallWindowLayout.setVisibility(View.VISIBLE);
			rocketImg.setVisibility(View.GONE);
			mManager.removeLauncher(getContext());
		}
	}

	//展开工具栏
	private void open() {
		start = false;
		ObjectAnimator translationLeft = new ObjectAnimator().ofFloat(takePhoto, "translationX", 0, -220f);
		translationLeft.setDuration(500);
		translationLeft.start();
		ObjectAnimator translationRight = new ObjectAnimator().ofFloat(openAlbum, "translationX", 0, 220f);
		translationRight.setDuration(500);
		translationRight.start();
		ObjectAnimator re = ObjectAnimator.ofFloat(addButton, "rotation", 0f, 90f);
		AnimatorSet animatorSetsuofang = new AnimatorSet();//组合动画
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(addButton, "scaleX", 1, 0.8f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(addButton, "scaleY", 1, 0.8f);
		animatorSetsuofang.setDuration(500);
		animatorSetsuofang.play(scaleX).with(scaleY).with(re);
		animatorSetsuofang.start();
	}

	//合上工具栏
	private void close() {
		start = true;
		ObjectAnimator translationLeft = new ObjectAnimator().ofFloat(takePhoto, "translationX", -220, 0f);
		translationLeft.setDuration(500);
		translationLeft.start();
		ObjectAnimator translationRight = new ObjectAnimator().ofFloat(openAlbum, "translationX", 220, 0f);
		translationRight.setDuration(500);
		translationRight.start();
		ObjectAnimator re = ObjectAnimator.ofFloat(addButton, "rotation", 90f, 0f);
		AnimatorSet animatorSetsuofang = new AnimatorSet();//组合动画
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(addButton, "scaleX", 0.8f, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(addButton, "scaleY", 0.8f, 1f);
		animatorSetsuofang.setDuration(500);
		animatorSetsuofang.play(scaleX).with(scaleY).with(re);
		animatorSetsuofang.start();
	}


	/**
	 * 打开大悬浮窗，同时关闭小悬浮窗。
	 */
	private void openBigWindow() {
		mManager.createBigWindow(getContext());
		mManager.removeSmallWindow(getContext());
	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.icon_album:
				mManager.changePersonState(Chopper.FLAG_SHOCK);
				Intent intent = new Intent(getContext(), NoteActivity.class);
				getContext().startActivity(intent);
				break;
			case R.id.icon_take_photo:
				mManager.changePersonState(Chopper.FLAG_SLEEP);
				Intent clockIntent = new Intent(getContext(), ClockActivity.class);
				getContext().startActivity(clockIntent);
				break;
		}
	}

	/**
	 * 开始执行发射小火箭的任务。
	 * 
	 * @author guolin
	 */
	class LaunchTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// 在这里对小火箭的位置进行改变，从而产生火箭升空的效果
			while (mParams.y > 0) {
				mParams.y = mParams.y - 30;
				publishProgress();
				try {
					Thread.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			windowManager.updateViewLayout(FloatWindowSmallView.this, mParams);
		}

		@Override
		protected void onPostExecute(Void result) {
			// 火箭升空结束后，回归到悬浮窗状态
			updateViewStatus();
			mParams.x = (int) (xDownInScreen - xInView);
			mParams.y = (int) (yDownInScreen - yInView);
			windowManager.updateViewLayout(FloatWindowSmallView.this, mParams);
		}

	}

}
