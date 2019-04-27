package com.demo.petlong;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Chopper extends Person {

	private Bitmap[] bmpImage = new Bitmap[3];        //主图
	private Bitmap[] bmpTools = new Bitmap[6];        //小工具图
	/**
	 * 标识常量
	 */
	public static final int FLAG_STAND = 0,
			FLAG_WALK = 1,
			FLAG_WALK2 = 2,
			FLAG_EAT = 3,
			FLAG_SIT = 4,
			FLAG_UP = 5,
			FLAG_DOWN = 6,
			FLAG_BALL = 7,
			FLAG_HAPPY = 8,
			FLAG_SLEEP = 9,
			FLAG_SHOCK = 10,
			FLAG_STAR = 11;
	/**
	 * 上下行走的步数
	 */
	private final int UP_DOWN_STEPS = 20;

	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;
	private int mPetLevel;
	private int mLevelSmall;

	public Chopper(Context context) {
		super(context);
		mPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mPetLevel = mPreferences.getInt("petlevel", 1);
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(20);
		paint.setAntiAlias(true);        //抗边缘锯齿
		paint.setFilterBitmap(true);    //滤波过滤
		setFocusable(true);
		res = context.getResources();
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		draw(canvas, paint);
	}

	/**
	 * 初始化数据处理
	 */
	public void init() {
		//获取界面大小
		measureScreen();
		bmpW = decodeResource(res, R.drawable.chopper_ball).getWidth();
		bmpH = decodeResource(res, R.drawable.chopper_ball).getHeight();
		actionGroup.addAll(Arrays.asList(0, 1, 2, 3, 4, 7, 8, 9, 10, 11));
		//初始化的xy,显示在屏幕上的位置
		x = bmpW / 2;
		y = bmpH / 2;
		onActionChange(FLAG_DOWN);
	}

	/**
	 * 随机动画产生
	 */
	@Override
	public void randomChange() {
		//如果处于手指拖动状态则不进行变换
		if (actionFlag != FLAG_UP) {
			int count = actionGroup.size();
			int i = (int) (Math.random() * count);
			if (actionFlag != actionGroup.get(i))//don't action the same
			{
				onActionChange(actionGroup.get(i));
			}
		}
	}

	//改变宠物的动画状态
	public void changeState(int state){
		onActionChange(state);
	}

	/**
	 * 触屏事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		touchX = event.getRawX();
		touchY = event.getRawY();		//以屏幕角为坐标点
		int bmpW = (int) (this.bmpW * personSize), bmpH = (int) (this.bmpH * personSize);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onActionChange(FLAG_UP);
				touchPreX = touchX;
				titleBarH = touchY - event.getY() - y;
				break;
			case MotionEvent.ACTION_MOVE:
				//触摸点的显示作用
				x = touchX - bmpW / 2;
				y = touchY - bmpH / 10 - titleBarH;
				break;
			case MotionEvent.ACTION_UP:
				onActionChange(FLAG_DOWN);
				titleBarH = 0;
				//报时的绘制点
				if (drawTime) {
					drawTimeNow = true;
					drawTimeFlag = 10;
				}
				break;
		}
		return true;
	}

	/***************************继承end***********************************/
	/**
	 * 绘图
	 */
	private void draw(Canvas canvas, Paint paint) {
		//公用的设置
		float dis = (1 - personSize) * bmpW / 2;

		//Matrix只实现了缩放功能
		matrix = new Matrix();
		///第一个参数的正负会改变图片的左右镜面对称，主要用在触摸拖动中，以及球形滚动时，行走时。
		matrix.postScale(personSize * leftOrRight, personSize, bmpW / 2, bmpH / 2);
		matrix.postTranslate(-dis, -dis);
		System.out.println("op chopper test: personSize*leftOrRight: " + personSize * leftOrRight);
		System.out.println("mMatrix: " + matrix);
		switch (actionFlag) {
			case FLAG_DOWN:
				actionDown(canvas, paint);
				break;
			case FLAG_SIT:
				actionSit(canvas, paint);
				break;
			case FLAG_STAND:
				actionStand(canvas, paint);
				break;
			case FLAG_UP:
				actionUp(canvas, paint);
				break;
			case FLAG_WALK:
				actionWalk(canvas, paint);
				break;
			case FLAG_WALK2:
				actionWalk2(canvas, paint);
				break;
			case FLAG_EAT:
				actionEat(canvas, paint);
				break;
			case FLAG_BALL:
				actionBall(canvas, paint);
				break;
			case FLAG_SLEEP:
				actionSleep(canvas, paint);
				break;
			case FLAG_HAPPY:
				actionHappy(canvas, paint);
				break;
			case FLAG_SHOCK:
				actionShock(canvas, paint);
				break;
			case FLAG_STAR:
				actionStar(canvas, paint);
				break;
		}
		drawLevel(canvas, paint);
		if (drawTimeNow)
			actionTime(canvas, paint);
	}

	private void levelUp(int data){
		Toast.makeText(getContext(),"经验提升："+data,Toast.LENGTH_SHORT).show();
		mLevelSmall += data;
		if (mLevelSmall / 10 >0){
			Toast.makeText(getContext(),"恭喜您的宠物等级提升了一级",Toast.LENGTH_SHORT).show();
			mPetLevel ++;
			mEditor.putInt("petlevel",  mPetLevel);
			mEditor.apply();
		}
		mLevelSmall = mLevelSmall%10;

	}

	/*********************动作,action*******************************************/
	/**
	 * 动作：下降,down
	 */
	private void actionDown(Canvas canvas, Paint paint) {
		if (frameFlag <= 1) {
			canvasDraw(canvas, bmpImage[0], matrix, paint);
			int disY = (int) (screenH - bmpH / 2 - bmpH * personSize / 2);
			if (y < disY)
				y += speed * 3;		//以此来变换图片的纵坐标
		} else if (frameFlag <= 3) {
			canvasDraw(canvas, bmpImage[1], matrix, paint);
		} else {
			int disY = (int) (screenH - bmpH / 2 - bmpH * personSize / 2);
			if (y > disY)
				y = disY;
			canvasDraw(canvas, bmpImage[1], matrix, paint);
//			randomChange();
			onActionChange(FLAG_SIT);
		}
		frameFlag++;
	}

	/**
	 * 动作：坐着,sit		//此时则会静坐5秒
	 */
	private void actionSit(Canvas canvas, Paint paint) {
		canvasDraw(canvas, bmpImage[0], matrix, paint);
	}

	/**
	 * 动作：站立,stand
	 */
	private void actionStand(Canvas canvas, Paint paint) {
		canvasDraw(canvas, bmpImage[0], matrix, paint);
	}

	/**
	 * 动作：悬空,up
	 */
	private void actionUp(Canvas canvas, Paint paint) {
		//动作判断
		if (touchX - touchPreX > 0) {//向右移动
			if (frameFlag < 1) {
				frameFlag = 1;
				leftOrRight = 1;
			} else if (frameFlag == 1) {
				frameFlag = 2;
			}
		} else if (touchX - touchPreX < 0) {//向左移动
			if (frameFlag > -1) {
				frameFlag = -1;
				leftOrRight = -1;
			} else if (frameFlag == -1) {
				frameFlag = -2;
			}
		} else {//未移动(若移动过，则需要摆回来)
			if (frameFlag != 0) {
				frameFlag -= leftOrRight;
			}
		}
		//动作绘制
		canvasDraw(canvas, bmpImage[frameFlag * leftOrRight], matrix, paint);
		//记录前一个点
		touchPreX = touchX;
	}

	/**
	 * 动作：行走,walk
	 */
	private void actionWalk(Canvas canvas, Paint paint) {
		if (x <= 0 && leftOrRight == 1
				|| x + bmpW * personSize >= screenW && leftOrRight == -1)//碰壁换向
			leftOrRight *= -1;
		x -= speed * leftOrRight;//移动
		//采用了循环方法，stand -> walk1 -> stand -> walk2
		if (frameFlag % 2 == 1) {
			canvasDraw(canvas, bmpImage[0], matrix, paint);
		} else if (frameFlag / 2 % 2 == 0) {
			canvasDraw(canvas, bmpImage[1], matrix, paint);
		} else {
			canvasDraw(canvas, bmpImage[2], matrix, paint);
		}

		frameFlag++;
	}

	/**
	 * 动作：行走,walk2,具有上下行走，斜方向行走
	 */
	private void actionWalk2(Canvas canvas, Paint paint) {
		if (y <= 0 && upOrDown == 1
				|| y + bmpH * personSize >= screenH && upOrDown == -1)//碰壁换向
			upOrDown *= -1;
		y -= speed * upOrDown;//移动
		//水平行走
		actionWalk(canvas, paint);
		if (frameFlag >= UP_DOWN_STEPS){

		}
//			randomChange();
	}

	private void actionEat(Canvas canvas, Paint paint) {
		canvasDraw(canvas, bmpImage[frameFlag % 3], matrix, paint);
		frameFlag++;
	}

	private void actionBall(Canvas canvas, Paint paint) {
		if (x <= 0 && leftOrRight == 1
				|| x + bmpW * personSize >= screenW && leftOrRight == -1)//碰壁换向
			leftOrRight *= -1;
		if (y <= 0 && upOrDown == 1
				|| y + bmpH * personSize >= screenH && upOrDown == -1)//碰壁换向
			upOrDown *= -1;
		x -= speed * leftOrRight;//移动
		y -= speed * upOrDown;//移动
		matrix.postRotate((float) (-60 * frameFlag * leftOrRight), bmpW * personSize / 2, bmpH * personSize / 2);
		canvasDraw(canvas, bmpImage[0], matrix, paint);
		frameFlag++;
	}

	private void actionSleep(Canvas canvas, Paint paint) {
		switch (frameFlag / 3 % 9) {
			case 0:
				canvasDraw(canvas, bmpImage[0], matrix, paint);
				frameFlag++;
				break;
			case 1:
			case 7:
				canvasDraw(canvas, bmpImage[1], matrix, paint);
				frameFlag = 2 * 3;
				break;
			case 2:
			case 6:
				canvasDraw(canvas, bmpImage[1], matrix, paint);
				canvasDraw(canvas, bmpTools[1], matrix, paint);
				frameFlag++;
				break;
			case 3:
			case 5:
				canvasDraw(canvas, bmpImage[1], matrix, paint);
				canvasDraw(canvas, bmpTools[2], matrix, paint);
				frameFlag++;
				break;
			case 4:
				canvasDraw(canvas, bmpImage[1], matrix, paint);
				canvasDraw(canvas, bmpTools[3], matrix, paint);
				if (frameFlag % 3 == 2) {
					if (Math.random() < 0.3)//half rate will break
					{
						frameFlag = 8 * 3;//break the bubble
						break;
					}
				}
				frameFlag++;
				break;
			case 8:
				canvasDraw(canvas, bmpImage[2], matrix, paint);
				canvasDraw(canvas, bmpTools[4], matrix, paint);
				frameFlag++;
				if (frameFlag % 3 == 2) {
					frameFlag = 0;
				}
				break;
		}
	}

	private void actionHappy(Canvas canvas, Paint paint) {
		canvasDraw(canvas, bmpImage[frameFlag / 2 % 3], matrix, paint);
		canvasDraw(canvas, bmpTools[frameFlag / 2 % 3 + 3], matrix, paint);
		frameFlag++;
	}

	private void actionShock(Canvas canvas, Paint paint) {
		int frameId = 0;
		switch (frameFlag / 2 % 6) {
			case 2:
			case 4:
				frameId = 1;
				break;
			case 3:
				frameId = 2;
				break;
			default:
				break;
		}
		canvasDraw(canvas, bmpImage[frameId], matrix, paint);
		frameFlag++;
	}

	private void actionStar(Canvas canvas, Paint paint) {
		canvasDraw(canvas, bmpImage[frameFlag / 2 % 2], matrix, paint);
		frameFlag++;
		if (Math.random() < 0.1) {
			leftOrRight *= -1;
		}
	}

	/**
	 * 动画报时功能
	 */
	private void actionTime(Canvas canvas, Paint paint) {
		paint.setTextSize(40f * personSize);
		paint.setTextAlign(Align.CENTER);
		paint.setShadowLayer(2, 2, 2, Color.BLACK);
		if (drawTimeFlag-- > 0) {
			if (touchX <= screenW / 3) {//左边绘制日期
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
				canvas.drawText(dateFormat.format(new Date()), bmpH * personSize / 2, (bmpH - 2) * personSize, paint);
			} else if (touchX >= screenW * 2 / 3) {//绘制时间
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				canvas.drawText(dateFormat.format(new Date()), bmpH * personSize / 2, (bmpH - 2) * personSize, paint);
			}
		} else {
			drawTimeNow = false;
		}
		paint.setShadowLayer(0, 0, 0, 0);//移出阴影的设置
	}

	//绘制宠物的等级
	private void drawLevel(Canvas canvas, Paint paint){
		paint.setTextSize(26f * personSize);
		paint.setTextAlign(Align.CENTER);
		paint.setShadowLayer(2, 2, 2, Color.BLUE);
		canvas.drawText("Level " + mPetLevel, 120, 46, paint);
		paint.setShadowLayer(0, 0, 0, 0);//移出阴影的设置
	}

	/**
	 * 动作变换事件(flag的变化)
	 */
	private void  onActionChange(int actionFlag) {
		this.actionFlag = actionFlag;
		frameFlag = 0;//初始化,每次变换一个新动作时才进行置零
		switch (actionFlag) {
			case FLAG_DOWN:
				if (x < 0) x = 0;
				if (x > screenW - bmpW * personSize) x = screenW - bmpW * personSize;
				bmpImage[0] = decodeResource(res, R.drawable.chopper_down_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_down_1_1);
				bmpImage[2] = null;
				break;
			case FLAG_SIT:
				frameFlag = (int) (Math.random() * 2);
				bmpImage[0] = decodeResource(res, R.drawable.chopper_sit);
				bmpImage[1] = null;
				bmpImage[2] = null;
				break;
			case FLAG_STAND:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_stand_0_1);
				bmpImage[1] = null;
				bmpImage[2] = null;
				break;
			case FLAG_UP:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_stand_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_up_0_1);
				bmpImage[2] = decodeResource(res, R.drawable.chopper_up_1_1);
				break;
			case FLAG_WALK2:
				upOrDown = Math.random() < 0.5 ? 1 : -1;
				levelUp(2);
			case FLAG_WALK:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_walk_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_walk_1_1);
				bmpImage[2] = decodeResource(res, R.drawable.chopper_walk_2_1);
				levelUp(1);
				break;
			case FLAG_EAT:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_eat_1_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_eat_2_1);
				bmpImage[2] = decodeResource(res, R.drawable.chopper_eat_3_1);
				levelUp(3);
				break;
			case FLAG_BALL:
				upOrDown = Math.random() < 0.5 ? 1 : Math.random() < 0.5 ? 0 : -1;
				bmpImage[0] = decodeResource(res, R.drawable.chopper_ball);
				bmpImage[1] = null;
				bmpImage[2] = null;
				levelUp(4);
				break;
			case FLAG_SLEEP:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_sleep_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_sleep_1_1);
				bmpImage[2] = decodeResource(res, R.drawable.chopper_sleep_2_1);
				bmpTools[1] = decodeResource(res, R.drawable.chopper_sleep_3_0);
				bmpTools[2] = decodeResource(res, R.drawable.chopper_sleep_3_1);
				bmpTools[3] = decodeResource(res, R.drawable.chopper_sleep_3_2);
				bmpTools[4] = decodeResource(res, R.drawable.chopper_sleep_3_3);
				levelUp(1);
				break;
			case FLAG_HAPPY:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_happy_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_happy_1_1);
				bmpImage[2] = decodeResource(res, R.drawable.chopper_happy_2_1);
				bmpTools[3] = decodeResource(res, R.drawable.chopper_happy_0_3);
				bmpTools[4] = decodeResource(res, R.drawable.chopper_happy_1_3);
				bmpTools[5] = decodeResource(res, R.drawable.chopper_happy_2_3);
				levelUp(3);
				break;
			case FLAG_SHOCK:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_shock_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_shock_1_1);
				bmpImage[2] = decodeResource(res, R.drawable.chopper_shock_2_1);
				levelUp(2);
				break;
			case FLAG_STAR:
				bmpImage[0] = decodeResource(res, R.drawable.chopper_star_0_1);
				bmpImage[1] = decodeResource(res, R.drawable.chopper_star_1_1);
				bmpImage[2] = null;
				levelUp(4);
				break;
		}
	}
}
