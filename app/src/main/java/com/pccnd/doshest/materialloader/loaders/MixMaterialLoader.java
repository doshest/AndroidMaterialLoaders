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

import java.util.ArrayList;
import java.util.List;

/**
 * 两个球不断融合和分开
 * 
 * Created by doshest on 2015/11/7.
 */
public class MixMaterialLoader extends View {

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
     * 当前的动态圆半径
     */
    private float mCurrentDynamicCircleRadius = 50;

    /**
     * 抽象大圆半径
     */
    private float mCurrentBigCircleRadius = mCurrentDynamicCircleRadius * 1.5f;
    
    /**
     * 动态圆半径变化比率
     */
    private float mMaxStaticCircleRadiusScaleRate = 0.5f;
    
    /**
     * 动态圆
     */
    private Circle mDynamicCircle;

    /**
     * 维护动态圆容器
     */
    private List<Circle> mDynamicCircles = new ArrayList<Circle>();
    
    /**
     * 最大粘连长度
     */
    private float mMaxAdherentLength = 2 * mCurrentDynamicCircleRadius ;
    
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
    public MixMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public MixMaterialLoader(Context context, AttributeSet attrs) {
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
    public MixMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        
        
        /* 动态圆 */
        for (int i = 0; i < 2; i++) {
            mDynamicCircle = new Circle();
            mDynamicCircle.radius = mCurrentDynamicCircleRadius;
            mDynamicCircle.x = mBigCircle.x;
            mDynamicCircle.y = mBigCircle.y;
            mDynamicCircles.add(mDynamicCircle);
        }

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
        
       
        if (doAdhere()) {
            canvas.drawCircle(mDynamicCircles.get(0).x, mDynamicCircles.get(0).y, mCurrentDynamicCircleRadius, mPaint);
            canvas.drawCircle(mDynamicCircles.get(1).x, mDynamicCircles.get(1).y, mCurrentDynamicCircleRadius, mPaint);
            Path path = drawAdherentBody(mDynamicCircles.get(0).x, mDynamicCircles.get(0).y, mCurrentDynamicCircleRadius, 40, mDynamicCircles.get(1).x, mDynamicCircles.get(1).y, mCurrentDynamicCircleRadius, 40);
            
            canvas.drawPath(path,mPaint);
        } else {
            canvas.drawCircle(mDynamicCircles.get(0).x, mDynamicCircles.get(0).y, mDynamicCircle.radius * (1 - mMaxStaticCircleRadiusScaleRate), mPaint);
            canvas.drawCircle(mDynamicCircles.get(1).x, mDynamicCircles.get(1).y, mDynamicCircle.radius * (1 - mMaxStaticCircleRadiusScaleRate), mPaint);
        }
        
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
        /* 圆1小于或等于圆2 */
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
     * 判断粘连范围
     *
     * @return
     */
    private boolean doAdhere() {
        
        /* 半径变化 */
        float distance = (float) Math.sqrt(Math.pow(mDynamicCircles.get(1).x - mDynamicCircles.get(0).x, 2) + Math.pow(mDynamicCircles.get(1).y - mDynamicCircles.get(0).y, 2));
        float scale =  ( distance / mMaxAdherentLength ) * mMaxStaticCircleRadiusScaleRate;
        mCurrentDynamicCircleRadius = mDynamicCircles.get(0).radius * (1 - scale);
        
        
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
        
        /* 抽象大圆的半径变化 */
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,mCurrentBigCircleRadius);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(1600);
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

        /* 两个球的坐标变化 */
        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(180,540);
        valueAnimator2.setInterpolator(new LinearInterpolator());
        valueAnimator2.setDuration(3200);
        valueAnimator2.setRepeatCount(Animation.INFINITE);
        valueAnimator2.start();
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = (float) animation.getAnimatedValue();
                mDynamicCircles.get(0).x = (float) (mBigCircle.x + mCurrentBigCircleRadius * Math.cos(Math.toRadians(angle)));
                mDynamicCircles.get(0).y = (float) (mBigCircle.y + mCurrentBigCircleRadius * Math.sin(Math.toRadians(angle)));
                mDynamicCircles.get(1).x = (float) (mBigCircle.x + mCurrentBigCircleRadius * Math.cos(Math.toRadians(angle + 180)));
                mDynamicCircles.get(1).y = (float) (mBigCircle.y + mCurrentBigCircleRadius * Math.sin(Math.toRadians(angle + 180)));
                invalidate();


            }
        });
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
