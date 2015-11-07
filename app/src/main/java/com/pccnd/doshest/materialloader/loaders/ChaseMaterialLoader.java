package com.pccnd.doshest.materialloader.loaders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 圆形，几个小球相继追赶
 * 
 * Created by doshest on 2015/11/7.
 */
public class ChaseMaterialLoader extends View {

    /**
     * 宽度
     */
    private int mWidth;

    /**
     * 高度
     */
    private int mHeight;

    /**
     * 抽象大圆
     */
    private Circle mBigCircle = new Circle();

    /**
     * 抽象大圆半径
     */
    private int mBigCircleRadius = 40;

    /**
     * 当前圆半径
     */
    private float mCircleRadius = mBigCircleRadius / 8;
    
    /**
     * 圆个数
     */
    private static int mCircleCount = 5;
    
    /**
     * 圆
     */
    private Circle mCircle;
    
    /**
     * 维护圆容器
     */
    private List<Circle> mCircles = new ArrayList<Circle>();

    /**
     * 间隔角度
     */
    private float mDivideAngle = 25;

    /**
     * 画笔
     */
    private Paint mPaint = new Paint();
    
    /**
     * 默认颜色
     */
    private int mColor = 0xFFFFFFFF;

    /**
     * 构造函数
     * 
     * @param context
     */
    public ChaseMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public ChaseMaterialLoader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public ChaseMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        
        /* 画笔 */
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        /* 宽度、高度 */
        mWidth = mHeight = (int)(2 * (mBigCircleRadius + mCircleRadius));
        
        /* 大圆 */
        mBigCircle.x = mWidth / 2;
        mBigCircle.y = mHeight / 2;
        mBigCircle.radius = mBigCircleRadius;
        
        
        /* 圆 */
        for (int i = 0; i < mCircleCount; i++) {
            mCircle = new Circle();
            mCircle.radius = mCircleRadius;
            mCircle.angle = -90 - mDivideAngle * i;
            mCircle.x = (float)(mBigCircle.x + mBigCircleRadius * Math.cos(Math.toRadians(mCircle.angle)));
            mCircle.y = (float)(mBigCircle.y + mBigCircleRadius * Math.sin(Math.toRadians(mCircle.angle)));
            mCircles.add(mCircle);
        }
        
        /* 开始动画 */
        startAnim();
    }

    /**
     * 大小
     * 
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSizeAndState(mWidth, widthMeasureSpec, MeasureSpec.UNSPECIFIED), resolveSizeAndState(mHeight, heightMeasureSpec, MeasureSpec.UNSPECIFIED));
    }

    /**
     * 绘制
     * 
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
       
        /* 静态圆 */
        for (int i = 0; i < mCircleCount; i++) {
            mCircle = mCircles.get(i);
            canvas.drawCircle(mCircle.x,mCircle.y,mCircle.radius,mPaint);
        }
    }
    

    /**
     * 开始动画
     */
    private void startAnim() {
        
        /* 追赶动画 */
        for (int i = 0; i < mCircleCount; i++) {
            
            final Circle circle = mCircles.get(i);
            
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(circle.angle, circle.angle + 360);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(1000 + 200 * i);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float angle = (float) animation.getAnimatedValue();
                    circle.x = (float) (mBigCircle.x + mBigCircleRadius * Math.cos(Math.toRadians(angle)));
                    circle.y = (float) (mBigCircle.y + mBigCircleRadius * Math.sin(Math.toRadians(angle)));
                    invalidate();
                }
            });
            if (i == mCircleCount - 1) {
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        startAnim();
                    }
                });
            }

        }
    }

    /**
     * 圆类
     */
    private class Circle {
        public float x;
        public float y;
        public float radius;
        public float angle;
      
    }
}
