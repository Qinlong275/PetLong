package com.jju.howe.howeassistant.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MicRecordingView extends View {

    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 透明度集合
     */
    private List<Integer> alphaList;
    /**
     * 所有圆环半径集合
     */
    private List<Integer> circleList;
    /**
     * 圆环的颜色
     */
    private int circleColor = Color.RED;
    /**
     * 圆环之间的间隔大小
     */
    private int gapLength = 50;
    /**
     * 最大半径
     */
    private int maxWidth = 200;
    /**
     * 圆环的个数
     */
    private int circieNumber = 5;
    /**
     * 圆环的半径x，y
     */
    private float circleX, circleY;
    /**
     * 是否运行动画
     */
    private boolean isStarting = false;

    public MicRecordingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        circleColor = Color.RED;
        paint = new Paint();
        paint.setColor(circleColor);
        alphaList = new ArrayList<>();
        circleList = new ArrayList<>();
        alphaList.add(0, 255);
        circleList.add(0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxWidth = w > h ? h / 2 : w / 2;
        circleX = w / 2;
        circleY = h / 2;
        circieNumber = maxWidth / gapLength;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < circleList.size(); i++) {
            int circleWidth = circleList.get(i);
            int alpha = alphaList.get(i);
            paint.setAlpha(alpha);
            canvas.drawCircle(circleX, circleY, circleWidth + gapLength, paint);

            if (isStarting && alpha > 0 && circleWidth < maxWidth) {//一个圆环逐渐扩大
                alphaList.set(i, alpha - 1);
                circleList.set(i, circleWidth + 1);
            }

            /**当一个圆形的半径扩大到了gapLength的长度，就创建下一个圆*/
            if (isStarting && circleList.get(circleList.size() - 1) == gapLength) {
                alphaList.add(255);
                circleList.add(0);
            }

            if (isStarting && circleList.size() == circieNumber) {//保持圆圈在运行
                alphaList.remove(0);
                circleList.remove(0);
            }

            invalidate();
        }
    }

    /**
     * 执行动画
     */
    public void start() {
        isStarting = true;
    }

    /**
     * 停止动画
     */
    public void stop() {
        isStarting = false;
    }

    /**
     * 判断是都在不在执行
     */
    public boolean isStarting() {
        return isStarting;
    }

}
