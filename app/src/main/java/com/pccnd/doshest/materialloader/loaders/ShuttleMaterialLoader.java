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

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

/**
 * 一个小球从一个大球中不断飞出飞入
 * 
 * Created by doshest on 2015/11/7.
 */
public class ShuttleMaterialLoader extends View {

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
     * 当前的静态圆半径
     */
    private float mCurrentStaticCircleRadius = 20;

    /**
     * 抽象大圆半径
     */
    private float mCurrentBigCircleRadius = mCurrentStaticCircleRadius * 3f;
    
    /**
     * 静态圆半径变化比率
     */
    private float mMaxStaticCircleRadiusScaleRate = 0.4f;

    /**
     * 动态圆半径
     */
    private float mCurrentDynamicCircleRadius = mCurrentStaticCircleRadius / 2;
    
    /**
     * 动态圆
     */
    private Circle mDynamicCircle = new Circle();

    /**
     * 静态圆
     */
    private Circle mStaticCircle = new Circle();

    /**
     * 最大粘连长度
     */
    private float mMaxAdherentLength = 2 * mCurrentStaticCircleRadius ;
    
    /**
     * 画笔
     */
    private Paint mPaint = new Paint();

    /**
     * 路径
     */
    private Path mPath = new Path();

    /**
     * 默认颜色
     */
    private int mColor = 0xFFFFFFFF;

    

    /**
     * 构造函数
     * 
     * @param context
     */
    public ShuttleMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public ShuttleMaterialLoader(Context context, AttributeSet attrs) {
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
    public ShuttleMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mWidth = mHeight = (int)(2 * (mCurrentBigCircleRadius + mCurrentDynamicCircleRadius));
        
        /* 大圆 */
        mBigCircle.x = mWidth / 2;
        mBigCircle.y = mHeight / 2;
        mBigCircle.radius = mCurrentBigCircleRadius;
        
        /* 静态圆 */
        mStaticCircle.radius = mCurrentStaticCircleRadius;
        mStaticCircle.x = mBigCircle.x;
        mStaticCircle.y = mBigCircle.y;
        
        /* 动态圆 */
        mDynamicCircle.radius = mCurrentDynamicCircleRadius;
        mDynamicCircle.x = mStaticCircle.x ;
        mDynamicCircle.y = mStaticCircle.y;

        startAnim();

       

    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSizeAndState(mWidth, widthMeasureSpec, MeasureSpec.UNSPECIFIED), resolveSizeAndState(mHeight, heightMeasureSpec, MeasureSpec.UNSPECIFIED));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        
        /* 动态圆 */
        canvas.drawCircle(mDynamicCircle.x, mDynamicCircle.y, mDynamicCircle.radius, mPaint);
        
         /* 静态圆 */
        if (doAdhere()) {
            canvas.drawCircle(mStaticCircle.x, mStaticCircle.y, mCurrentStaticCircleRadius, mPaint);
            //drawAdherentBody((canvas);
            Path path = drawAdherentBody(mStaticCircle.x,mStaticCircle.y,mCurrentStaticCircleRadius,30,
                    mDynamicCircle.x,mDynamicCircle.y,mDynamicCircle.radius,45);
            canvas.drawPath(path,mPaint);
        }
        else
            canvas.drawCircle(mStaticCircle.x, mStaticCircle.y, mStaticCircle.radius, mPaint);
        
    }

    /**
     * 画粘连体
     *
     * @param cx1     圆心x1
     * @param cy1     圆心y1
     * @param r1      圆半径r1
     * @param offset1 贝塞尔曲线偏移角度offset1
     * @param cx2     圆心x2
     * @param cy2     圆心y2
     * @param r2      圆半径r2
     * @param offset2 贝塞尔曲线偏移角度offset2
     * @return
     */
    private Path drawAdherentBody(float cx1,float cy1,float r1,float offset1,float cx2,float cy2,float r2,float offset2) {
        
        /* 求三角函数 */
        float degrees =(float) Math.toDegrees(Math.atan(Math.abs(cy2 - cy1) / Math.abs(cx2 - cx1)));
        
        /* 根据圆1与圆2的相对位置求四个点 */
        float differenceX = cx1 - cx2;
        float differenceY = cy1 - cy2;

        /* 两条贝塞尔曲线的四个端点 */
        float x1,y1,x2,y2,x3,y3,x4,y4;
        
        /* 圆1在圆2的下边 */
        if (differenceX == 0 && differenceY > 0) {
            x2 = cx2 - r2 * (float) Math.sin(Math.toRadians(offset2));
            y2 = cy2 + r2 * (float) Math.cos(Math.toRadians(offset2));
            x4 = cx2 + r2 * (float) Math.sin(Math.toRadians(offset2));
            y4 = cy2 + r2 * (float) Math.cos(Math.toRadians(offset2));
            x1 = cx1 - r1 * (float) Math.sin(Math.toRadians(offset1));
            y1 = cy1 - r1 * (float) Math.cos(Math.toRadians(offset1));
            x3 = cx1 + r1 * (float) Math.sin(Math.toRadians(offset1));
            y3 = cy1 - r1 * (float) Math.cos(Math.toRadians(offset1));

        }
        /* 圆1在圆2的上边 */
        else if (differenceX == 0 && differenceY < 0) {
            x2 = cx2 - r2 * (float) Math.sin(Math.toRadians(offset2));
            y2 = cy2 - r2 * (float) Math.cos(Math.toRadians(offset2));
            x4 = cx2 + r2 * (float) Math.sin(Math.toRadians(offset2));
            y4 = cy2 - r2 * (float) Math.cos(Math.toRadians(offset2));
            x1 = cx1 - r1 * (float) Math.sin(Math.toRadians(offset1));
            y1 = cy1 + r1 * (float) Math.cos(Math.toRadians(offset1));
            x3 = cx1 + r1 * (float) Math.sin(Math.toRadians(offset1));
            y3 = cy1 + r1 * (float) Math.cos(Math.toRadians(offset1));

        }
        /* 圆1在圆2的右边 */
        else if (differenceX > 0 && differenceY == 0) {
            x2 = cx2 + r2 * (float) Math.cos(Math.toRadians(offset2));
            y2 = cy2 + r2 * (float) Math.sin(Math.toRadians(offset2));
            x4 = cx2 + r2 * (float) Math.cos(Math.toRadians(offset2));
            y4 = cy2 - r2 * (float) Math.sin(Math.toRadians(offset2));
            x1 = cx1 - r1 * (float) Math.cos(Math.toRadians(offset1));
            y1 = cy1 + r1 * (float) Math.sin(Math.toRadians(offset1));
            x3 = cx1 - r1 * (float) Math.cos(Math.toRadians(offset1));
            y3 = cy1 - r1 * (float) Math.sin(Math.toRadians(offset1));
        } 
        /* 圆1在圆2的左边 */
        else if (differenceX < 0 && differenceY == 0 ) {
            x2 = cx2 - r2 * (float) Math.cos(Math.toRadians(offset2));
            y2 = cy2 + r2 * (float) Math.sin(Math.toRadians(offset2));
            x4 = cx2 - r2 * (float) Math.cos(Math.toRadians(offset2));
            y4 = cy2 - r2 * (float) Math.sin(Math.toRadians(offset2));
            x1 = cx1 + r1 * (float) Math.cos(Math.toRadians(offset1));
            y1 = cy1 + r1 * (float) Math.sin(Math.toRadians(offset1));
            x3 = cx1 + r1 * (float) Math.cos(Math.toRadians(offset1));
            y3 = cy1 - r1 * (float) Math.sin(Math.toRadians(offset1));
        }
        /* 圆1在圆2的右下角 */
        else if (differenceX > 0 && differenceY > 0) {
            x2 = cx2 - r2 * (float) Math.cos(Math.toRadians(180 - offset2 - degrees));
            y2 = cy2 + r2 * (float) Math.sin(Math.toRadians(180 - offset2 - degrees));
            x4 = cx2 + r2 * (float) Math.cos(Math.toRadians(degrees - offset2));
            y4 = cy2 + r2 * (float) Math.sin(Math.toRadians(degrees - offset2));
            x1 = cx1 - r1 * (float) Math.cos(Math.toRadians(degrees - offset1));
            y1 = cy1 - r1 * (float) Math.sin(Math.toRadians(degrees - offset1));
            x3 = cx1 + r1 * (float) Math.cos(Math.toRadians(180 - offset1 - degrees));
            y3 = cy1 - r1 * (float) Math.sin(Math.toRadians(180 - offset1 - degrees));
        }
        /* 圆1在圆2的左上角 */
        else if (differenceX < 0 && differenceY < 0) {
            x2 = cx2 - r2 * (float) Math.cos(Math.toRadians(degrees - offset2));
            y2 = cy2 - r2 * (float) Math.sin(Math.toRadians(degrees - offset2));
            x4 = cx2 + r2 * (float) Math.cos(Math.toRadians(180 - offset2 - degrees));
            y4 = cy2 - r2 * (float) Math.sin(Math.toRadians(180 - offset2 - degrees));
            x1 = cx1 - r1 * (float) Math.cos(Math.toRadians(180 - offset1 - degrees));
            y1 = cy1 + r1 * (float) Math.sin(Math.toRadians(180 - offset1 - degrees));
            x3 = cx1 + r1 * (float) Math.cos(Math.toRadians(degrees - offset1));
            y3 = cy1 + r1 * (float) Math.sin(Math.toRadians(degrees - offset1));
        }
        /* 圆1在圆2的左下角 */
        else if (differenceX < 0 && differenceY > 0) {
            x2 = cx2 - r2 * (float) Math.cos(Math.toRadians(degrees - offset2));
            y2 = cy2 + r2 * (float) Math.sin(Math.toRadians(degrees - offset2));
            x4 = cx2 + r2 * (float) Math.cos(Math.toRadians(180 - offset2 - degrees));
            y4 = cy2 + r2 * (float) Math.sin(Math.toRadians(180 - offset2 - degrees));
            x1 = cx1 - r1 * (float) Math.cos(Math.toRadians(180 - offset1 - degrees));
            y1 = cy1 - r1 * (float) Math.sin(Math.toRadians(180 - offset1 - degrees));
            x3 = cx1 + r1 * (float) Math.cos(Math.toRadians(degrees - offset1));
            y3 = cy1 - r1 * (float) Math.sin(Math.toRadians(degrees - offset1));
        }
        /* 圆1在圆2的右上角 */
        else {
            x2 = cx2 - r2 * (float) Math.cos(Math.toRadians(180 - offset2 - degrees));
            y2 = cy2 - r2 * (float) Math.sin(Math.toRadians(180 - offset2 - degrees));
            x4 = cx2 + r2 * (float) Math.cos(Math.toRadians(degrees - offset2));
            y4 = cy2 - r2 * (float) Math.sin(Math.toRadians(degrees - offset2));
            x1 = cx1 - r1 * (float) Math.cos(Math.toRadians(degrees - offset1));
            y1 = cy1 + r1* (float) Math.sin(Math.toRadians(degrees - offset1));
            x3 = cx1 + r1 * (float) Math.cos(Math.toRadians(180 - offset1 - degrees));
            y3 = cy1 + r1 * (float) Math.sin(Math.toRadians(180 - offset1 - degrees));
        }
        
        /* 贝塞尔曲线的控制点 */
        float anchorX1,anchorY1,anchorX2,anchorY2;
        
        /* 圆1大于圆2 */
        if (r1 > r2) {
            anchorX1 = (x2 + x3) / 2;
            anchorY1 = (y2 + y3) / 2;
            anchorX2 = (x1 + x4) / 2;
            anchorY2 = (y1 + y4) / 2;
        }
        /* 圆2小于或等于圆1 */
        else {
            anchorX1 = (x1 + x4) / 2;
            anchorY1 = (y1 + y4) / 2;
            anchorX2 = (x2 + x3) / 2;
            anchorY2 = (y2 + y3) / 2;
        }
        
        /* 画粘连体 */
        Path path = new Path();
        path.reset();
        path.moveTo(x1, y1);
        path.quadTo(anchorX1, anchorY1, x2, y2);
        path.lineTo(x4, y4);
        path.quadTo(anchorX2, anchorY2, x3, y3);
        path.lineTo(x1, y1);

        return path;
    }
    
    /**
     * 画粘连体
     * 
     * @param canvas
     */
    @Deprecated
    private void drawAdherentBody(Canvas canvas) {
        
        /* 求三角函数 */
        float angle =(float) Math.toDegrees(Math.atan(Math.abs(mStaticCircle.y - mDynamicCircle.y) / Math.abs(mStaticCircle.x - mDynamicCircle.x)));
        float offset1 = 30;
        float offset2 = 45;
        
        /* 根据动态球与静态球的相对位置求四个点 */
        float differenceX = mStaticCircle.x - mDynamicCircle.x;
        float differenceY = mStaticCircle.y - mDynamicCircle.y;
        float dynamicX1,dynamicY1,dynamicX2,dynamicY2,staticX1,staticY1,staticX2,staticY2;
        
        if (differenceX > 0 && differenceY > 0) {
            dynamicX1 = mDynamicCircle.x - mDynamicCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            dynamicY1 = mDynamicCircle.y + mDynamicCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            dynamicX2 = mDynamicCircle.x + mDynamicCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            dynamicY2 = mDynamicCircle.y + mDynamicCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            staticX1 = mStaticCircle.x - mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            staticY1 = mStaticCircle.y - mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(angle - offset1));
            staticX2 = mStaticCircle.x + mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            staticY2 = mStaticCircle.y - mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
        } else if (differenceX < 0 && differenceY < 0) {
            dynamicX1 = mDynamicCircle.x - mDynamicCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            dynamicY1 = mDynamicCircle.y - mDynamicCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            dynamicX2 = mDynamicCircle.x + mDynamicCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            dynamicY2 = mDynamicCircle.y - mDynamicCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            staticX1 = mStaticCircle.x - mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            staticY1 = mStaticCircle.y + mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
            staticX2 = mStaticCircle.x + mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            staticY2 = mStaticCircle.y + mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(angle - offset1));
        } else if (differenceX < 0 && differenceY > 0) {
            dynamicX1 = mDynamicCircle.x - mDynamicCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            dynamicY1 = mDynamicCircle.y + mDynamicCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            dynamicX2 = mDynamicCircle.x + mDynamicCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            dynamicY2 = mDynamicCircle.y + mDynamicCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            staticX1 = mStaticCircle.x - mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            staticY1 = mStaticCircle.y - mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
            staticX2 = mStaticCircle.x + mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            staticY2 = mStaticCircle.y - mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(angle - offset1));
        } else {
            dynamicX1 = mDynamicCircle.x - mDynamicCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            dynamicY1 = mDynamicCircle.y - mDynamicCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            dynamicX2 = mDynamicCircle.x + mDynamicCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            dynamicY2 = mDynamicCircle.y - mDynamicCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            staticX1 = mStaticCircle.x - mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            staticY1 = mStaticCircle.y + mCurrentStaticCircleRadius* (float) Math.sin(Math.toRadians(angle - offset1));
            staticX2 = mStaticCircle.x + mCurrentStaticCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            staticY2 = mStaticCircle.y + mCurrentStaticCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
        }
        
        /* 控制点 */
        float anchorX1 = ( dynamicX1 + staticX2 ) / 2;
        float anchorY1 = ( dynamicY1 + staticY2 ) / 2;

        float anchorX2 = ( dynamicX2 + staticX1 ) / 2;
        float anchorY2 = ( dynamicY2 + staticY1 ) / 2;
        
        
        /* 画贝塞尔曲线 */
        mPath.reset();
        mPath.moveTo(dynamicX1, dynamicY1);
        mPath.quadTo(anchorX1, anchorY1, staticX1, staticY1);
        mPath.lineTo(staticX2, staticY2);
        mPath.quadTo(anchorX2, anchorY2, dynamicX2, dynamicY2);
        mPath.lineTo(dynamicX1, dynamicY1);
        canvas.drawPath(mPath, mPaint);
        
    }
    

    /**
     * 判断粘连范围
     *
     * @return
     */
    private boolean doAdhere() {
        
        /* 半径变化 */
        float distance = (float) Math.sqrt(Math.pow(mStaticCircle.x - mDynamicCircle.x, 2) + Math.pow(mStaticCircle.y - mDynamicCircle.y, 2));
        float scale = mMaxStaticCircleRadiusScaleRate - ( distance / mMaxAdherentLength ) * mMaxStaticCircleRadiusScaleRate;
        mCurrentStaticCircleRadius = mStaticCircle.radius * (1 + scale);
        
        
        /* 判断是否可以作贝塞尔曲线 */
        if (distance < mMaxAdherentLength) 
            return true;
        else
            return false;
    }

    /**
     * 开始动画
     */
    private void startAnim() {
        
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,mCurrentBigCircleRadius);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(800);
        valueAnimator.setRepeatCount(Animation.INFINITE);
        valueAnimator.setRepeatMode(Animation.REVERSE);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentBigCircleRadius = (float) animation.getAnimatedValue();
                mCurrentBigCircleRadius = currentBigCircleRadius;
                invalidate();
            }
        });
        
        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(180,1620);
        valueAnimator2.setInterpolator(new LinearInterpolator());
        valueAnimator2.setDuration(9600);
        valueAnimator2.setRepeatCount(Animation.INFINITE);
        valueAnimator2.start();
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = (float) animation.getAnimatedValue();
                mDynamicCircle.x = (float) (mBigCircle.x + mCurrentBigCircleRadius * Math.cos(Math.toRadians(angle)));
                mDynamicCircle.y = (float) (mBigCircle.y + mCurrentBigCircleRadius * Math.sin(Math.toRadians(angle)));
                invalidate();


            }
        });
    }

    /**
     * 设置颜色
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        mPaint.setColor(mColor);
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
