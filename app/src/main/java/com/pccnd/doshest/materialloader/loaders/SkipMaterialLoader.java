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
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 几个小球相继跳跃
 * 
 * Created by doshest on 2015/11/7.
 */
public class SkipMaterialLoader extends View {

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
    private float mRadius = 6;

    /**
     * 圆个数
     */
    private int mCircleCount = 4;

    /**
     * 圆与圆之间的间隔距离
     */
    private float mDivideWidth = 5;
    
    /**
     * 表示哪一个小圆
     */
    private int mPosition;

    /**
     * 静态圆
     */
    private Circle mCircle;

    /**
     * 跳跃的高度
     */
    private float mSkipHeight = 10;

    /**
     * 维护静态圆容器
     */
    private List<Circle> mCircles = new ArrayList<Circle>();

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
    public SkipMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public SkipMaterialLoader(Context context, AttributeSet attrs) {
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
    public SkipMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mWidth = (int)((mCircleCount - 1) * (mDivideWidth + 2 * mRadius) + 2 * mRadius);
        mHeight = (int) (mSkipHeight + mRadius * 2);
        
        
        /* 圆 */
        for (int i = 0; i < mCircleCount; i++) {
            mCircle = new Circle();
            mCircle.radius = mRadius;
            mCircle.x = mRadius + (mDivideWidth + 2 * mRadius) * i;
            mCircle.y = mHeight - (mHeight - mSkipHeight) / 2;
            mCircles.add(mCircle);
        }
        
        /* 开始上升动画 */
        startUpAnim(0);
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

        for (int i = 0; i < mCircleCount; i++) {
            mCircle = mCircles.get(i);
            canvas.drawCircle(mCircle.x, mCircle.y,mCircle.radius,mPaint);
        }
    }

    /**
     * 开启上升动画
     * 
     * @param i
     */
    private void startUpAnim(int i) {
        if (i == mCircleCount) {
            return;
        }
        
        final Circle circle = mCircles.get(i);
        mPosition = i;
      
        /* 上升动画 */
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(circle.y, circle.y - mSkipHeight);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float y = (float) animation.getAnimatedValue();
                circle.y = y;
                invalidate();
            }
        });
        
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startDownAnim(mPosition);
                startUpAnim(++mPosition);
            }
        });
    }

    /**
     * 开启下降动画
     * 
     * @param i
     */
    private void startDownAnim(int i) {
        final Circle circle = mCircles.get(i);
       
        /* 下降动画 */
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(circle.y, circle.y + mSkipHeight);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float y = (float) animation.getAnimatedValue();
                circle.y = y;
                invalidate();
            }
        });
        if (i == mCircleCount - 1) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startUpAnim(0);
                }
            });
        }

    }
    

    /**
     * 圆类
     */
    private class Circle {
        public float x;
        public float y;
        public float radius;
    }
}
