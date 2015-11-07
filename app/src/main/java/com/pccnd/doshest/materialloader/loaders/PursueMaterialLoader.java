/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pccnd.doshest.materialloader.loaders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 水平，几个小球相继追赶
 * 
 * Created by doshest on 2015/11/7.
 */
public class PursueMaterialLoader extends View {

    /**
     * 宽度
     */
    private int mWidth;

    /**
     * 高度
     */
    private int mHeight;

    /**
     * 圆半径
     */
    private float mCircleRadius = 5;
    
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
     * 间隔
     */
    private float mDivideWidth = 10;

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
    public PursueMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public PursueMaterialLoader(Context context, AttributeSet attrs) {
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
    public PursueMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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

        /* 高度 */
        mHeight = (int)(2 * mCircleRadius);
        
        /* 圆 */
        for (int i = 0; i < mCircleCount; i++) {
            mCircle = new Circle();
            mCircle.radius = mCircleRadius;
            mCircle.x = mCircle.oldX = -mCircleRadius - (mDivideWidth + mCircleRadius * 2) * i;
            mCircle.y = (float)(mHeight / 2);
            mCircles.add(mCircle);
        } 
    
       
    }

    /**
     * 大小
     * 
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(mWidth, widthMeasureSpec, MeasureSpec.UNSPECIFIED), resolveSizeAndState(mHeight, heightMeasureSpec, MeasureSpec.UNSPECIFIED));
       
        /* 开始进入动画 */
        startEnterAnim();
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
            canvas.drawCircle(mCircle.x, mCircle.y, mCircle.radius, mPaint);
        }
    }
    

    /**
     * 开始进入动画
     */
    private void startEnterAnim() {

        // 求所有小球长度一半的偏移量
        float offset =  mCircleCount % 2 == 0 ?
                (mCircleCount / 2) * (mDivideWidth + 2 * mCircleRadius) - mDivideWidth / 2 :
                (mCircleCount / 2) * (mDivideWidth + 2 * mCircleRadius) + mCircleRadius;
        
        /* 进入动画 */
        for (int i = 0; i < mCircleCount; i++) {
            final Circle circle = mCircles.get(i);
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(circle.oldX, circle.oldX + mWidth / 2 + offset);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(800 + 200 * i);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float x = (float) animation.getAnimatedValue();
                    circle.x = x;
                    invalidate();
                }
            });
            
            if (i == mCircleCount - 1) {
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startLeaveAnim();
                            }
                        }, 400);
                    }
                });
            }
        }
    }

    /**
     * 开始退出动画
     */
    private void startLeaveAnim() {

        // 求所有小球长度一半的偏移量
        float offset =  mCircleCount % 2 == 0 ?
                (mCircleCount / 2) * (mDivideWidth + 2 * mCircleRadius) - mDivideWidth / 2 :
                (mCircleCount / 2) * (mDivideWidth + 2 * mCircleRadius) + mCircleRadius;
        
        /* 退出动画 */
        for (int i = 0; i < mCircleCount; i++) {
            final Circle circle = mCircles.get(i);
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(circle.x, circle.x + mWidth / 2 + offset);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(800 + 200 * i);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float x = (float) animation.getAnimatedValue();
                    circle.x = x;
                    invalidate();
                }
            });

            if (i == mCircleCount - 1) {
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startEnterAnim();

                    }
                });
            }
        }
    }
    

    /**
     * 圆类
     */
    private class Circle {
        public float oldX;
        public float x;
        public float y;
        public float radius;
    }
}
