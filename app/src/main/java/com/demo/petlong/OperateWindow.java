package com.demo.petlong;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class OperateWindow extends LinearLayout implements View.OnClickListener{

	/**
	 * 记录大悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录大悬浮窗的高度
	 */
	public static int viewHeight;

	private MyWindowManager mManager;

	public OperateWindow(final Context context, final MyWindowManager manager) {
		super(context);
		mManager = manager;
		LayoutInflater.from(context).inflate(R.layout.operate_window, this);
		View view = findViewById(R.id.operate_window_layout);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		initClickEvent(R.id.icon_happy);
		initClickEvent(R.id.icon_star);
		initClickEvent(R.id.icon_sleep);
		initClickEvent(R.id.icon_walk2);
		initClickEvent(R.id.icon_walk);
		initClickEvent(R.id.icon_eat);
		initClickEvent(R.id.icon_shock);
		initClickEvent(R.id.icon_ball);
	}

	private void initClickEvent(int id){
		findViewById(id).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.icon_happy:
				mManager.changePersonState(Chopper.FLAG_HAPPY);
				break;
			case R.id.icon_star:
				mManager.changePersonState(Chopper.FLAG_STAR);
				break;
			case R.id.icon_sleep:
				mManager.changePersonState(Chopper.FLAG_SLEEP);
				break;
			case R.id.icon_walk2:
				mManager.changePersonState(Chopper.FLAG_WALK2);
				break;
			case R.id.icon_walk:
				mManager.changePersonState(Chopper.FLAG_WALK);
				break;
			case R.id.icon_eat:
				mManager.changePersonState(Chopper.FLAG_EAT);
				break;
			case R.id.icon_shock:
				mManager.changePersonState(Chopper.FLAG_SHOCK);
				break;
			case R.id.icon_ball:
				mManager.changePersonState(Chopper.FLAG_BALL);
				break;


		}
	}
}
