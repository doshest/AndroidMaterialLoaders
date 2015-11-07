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
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 几个小球有次序从一个大球飞出，然后再有次序飞入那个大球.
 * 
 * Created by doshest on 2015/11/7.
 */
public class ScatterMaterialLoader extends View {

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
     * 当前的中间圆半径
     */
    private float mCurrentMiddleCircleRadius = 40;
   
    /**
     * 当前的小圆半径
     */
    private float mCurrentSmallCircleRadius = mCurrentMiddleCircleRadius / 6;

    /**
     * 抽象大圆半径
     */
    private float mBigCircleRadius = mCurrentMiddleCircleRadius - mCurrentSmallCircleRadius;
    
    /**
     * 中间圆和抽象大圆变化半径的最大比率
     */
    private float mMaxCircleRadiusScaleRate = 0.4f;

    /**
     * 小圆个数
     */
    private static int mSmallCircleCount = 8;

    /**
     * 小圆
     */
    private Circle mSmallCircle;

    /**
     * 中间圆
     */
    private Circle mMiddleCircle = new Circle();

    /**
     * 维护小圆容器
     */
    private List<Circle> mSmallCircles = new ArrayList<Circle>();

    /**
     * 表示哪一个小圆
     */
    private int mPosition;

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
    public ScatterMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public ScatterMaterialLoader(Context context, AttributeSet attrs) {
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
    public ScatterMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mWidth = mHeight = (int)(2 * (20 + mBigCircleRadius * (1 + mMaxCircleRadiusScaleRate) + mCurrentSmallCircleRadius));
        
        /* 抽象大圆 */
        mBigCircle.x = mWidth / 2;
        mBigCircle.y = mHeight / 2;
        mBigCircle.radius = mBigCircleRadius;
        
        /* 中间圆 */
        mMiddleCircle.radius = mCurrentMiddleCircleRadius;
        mMiddleCircle.x = mBigCircle.x;
        mMiddleCircle.y = mBigCircle.y;
        
        /* 静态圆 */
        for (int i = 0; i < mSmallCircleCount; i++) {
            mSmallCircle = new Circle();
            mSmallCircle.radius = mCurrentSmallCircleRadius;
            mSmallCircle.currentBigCircleRadius = 0;
            mSmallCircle.radian = (float)Math.toRadians(-90 + 45 * i);
            mSmallCircle.x = (float)(mMiddleCircle.x + (mCurrentMiddleCircleRadius - mSmallCircle.radius) * Math.cos(mSmallCircle.radian));
            mSmallCircle.y = (float)(mMiddleCircle.y + (mCurrentMiddleCircleRadius - mSmallCircle.radius) * Math.sin(mSmallCircle.radian));
            mSmallCircles.add(mSmallCircle);
        }

        /* 延时一段时间开启动画 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startBigCircleRadiusAnim(0);
                startAngleAndMiddleCircleRadiusAnim();
            }
        }, 200);   
        
        
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
        

        /* 中间圆 */
        canvas.drawCircle(mMiddleCircle.x, mMiddleCircle.y, mCurrentMiddleCircleRadius, mPaint);
        
        /* 小圆 */
        for (int i = 0; i < mSmallCircleCount; i++) {
            mSmallCircle = mSmallCircles.get(i);
            
            /* 小圆 */
            canvas.drawCircle(mSmallCircle.x, mSmallCircle.y, mSmallCircle.radius, mPaint);
            
            /* 判断哪个圆可以作贝塞尔曲线 */
            if (doAdhere(i)) {
                //drawAdherentBody(canvas, i);
                Path path = drawAdherentBody(mMiddleCircle.x,mMiddleCircle.y,mCurrentMiddleCircleRadius,20,
                        mSmallCircle.x, mSmallCircle.y, mSmallCircle.radius,45);
                canvas.drawPath(path, mPaint);
            }
           
        }
    }

    /**
     * 画粘连体（通用方法）
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
     * 画粘连体（专用方法）
     * 
     * @param canvas
     * @param position
     */
    @Deprecated
    private void drawAdherentBody(Canvas canvas,int position) {
    
        mSmallCircle = mSmallCircles.get(position);
        
        /* 求三角函数 */
        float angle =(float) Math.toDegrees(Math.atan(Math.abs(mMiddleCircle.y - mSmallCircle.y) / Math.abs(mMiddleCircle.x - mSmallCircle.x)));
        float offset1 = 20;
        float offset2 = 45;
        
        /* 根据动态球与静态球的相对位置求四个点 */
        float differenceX = mMiddleCircle.x - mSmallCircle.x;
        float differenceY = mMiddleCircle.y - mSmallCircle.y;
        float smallX1,smallY1,smallX2,smallY2,middleX1,middleY1,middleX2,middleY2;
        
        if (differenceX > 0 && differenceY > 0) {
            smallX1 = mSmallCircle.x - mSmallCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            smallY1 = mSmallCircle.y + mSmallCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            smallX2 = mSmallCircle.x + mSmallCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            smallY2 = mSmallCircle.y + mSmallCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            middleX1 = mMiddleCircle.x - mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            middleY1 = mMiddleCircle.y - mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(angle - offset1));
            middleX2 = mMiddleCircle.x + mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            middleY2 = mMiddleCircle.y - mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
        } else if (differenceX < 0 && differenceY < 0) {
            smallX1 = mSmallCircle.x - mSmallCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            smallY1 = mSmallCircle.y - mSmallCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            smallX2 = mSmallCircle.x + mSmallCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            smallY2 = mSmallCircle.y - mSmallCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            middleX1 = mMiddleCircle.x - mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            middleY1 = mMiddleCircle.y + mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
            middleX2 = mMiddleCircle.x + mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            middleY2 = mMiddleCircle.y + mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(angle - offset1));
        } else if (differenceX < 0 && differenceY > 0) {
            smallX1 = mSmallCircle.x - mSmallCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            smallY1 = mSmallCircle.y + mSmallCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            smallX2 = mSmallCircle.x + mSmallCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            smallY2 = mSmallCircle.y + mSmallCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            middleX1 = mMiddleCircle.x - mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            middleY1 = mMiddleCircle.y - mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
            middleX2 = mMiddleCircle.x + mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            middleY2 = mMiddleCircle.y - mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(angle - offset1));
        } else {
            smallX1 = mSmallCircle.x - mSmallCircle.radius * (float) Math.cos(Math.toRadians(180 - offset2 - angle));
            smallY1 = mSmallCircle.y - mSmallCircle.radius * (float) Math.sin(Math.toRadians(180 - offset2 - angle));
            smallX2 = mSmallCircle.x + mSmallCircle.radius * (float) Math.cos(Math.toRadians(angle - offset2));
            smallY2 = mSmallCircle.y - mSmallCircle.radius * (float) Math.sin(Math.toRadians(angle - offset2));
            middleX1 = mMiddleCircle.x - mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(angle - offset1));
            middleY1 = mMiddleCircle.y + mCurrentMiddleCircleRadius* (float) Math.sin(Math.toRadians(angle - offset1));
            middleX2 = mMiddleCircle.x + mCurrentMiddleCircleRadius * (float) Math.cos(Math.toRadians(180 - offset1 - angle));
            middleY2 = mMiddleCircle.y + mCurrentMiddleCircleRadius * (float) Math.sin(Math.toRadians(180 - offset1 - angle));
        }
        
        /* 控制点 */
        float anchorX1 = ( smallX1 + middleX2 ) / 2;
        float anchorY1 = ( smallY1 + middleY2 ) / 2;

        float anchorX2 = ( smallX2 + middleX1 ) / 2;
        float anchorY2 = ( smallY2 + middleY1 ) / 2;
        
        
        /* 画贝塞尔曲线 */
        mPath.reset();
        mPath.moveTo(smallX1, smallY1);
        mPath.quadTo(anchorX1, anchorY1, middleX1, middleY1);
        mPath.lineTo(middleX2, middleY2);
        mPath.quadTo(anchorX2, anchorY2, smallX2, smallY2);
        mPath.lineTo(smallX1, smallY1);
        canvas.drawPath(mPath, mPaint);
        
    }

    /**
     * 判断粘连范围
     * 
     * @param position
     * @return
     */
    private boolean doAdhere(int position) {

        mSmallCircle = mSmallCircles.get(position);
        
        /* 半径变化 */
        float distance = (float) Math.sqrt(Math.pow(mMiddleCircle.x - mSmallCircle.x, 2) + Math.pow(mMiddleCircle.y - mSmallCircle.y, 2));
        
        /* 判断是否可以作贝塞尔曲线 */
        if (distance < mCurrentMiddleCircleRadius + mCurrentSmallCircleRadius * 3) 
            return true;
        else
            return false;
    }

    /**
     * 开始当前抽象大圆半径变化动画
     * 
     * @param pos
     */
    private void startBigCircleRadiusAnim(int pos) {
        if ( pos == mSmallCircleCount)
            return;

        mPosition = pos;
        final Circle smallCircle = mSmallCircles.get(mPosition);
        
        /* 抽象大圆的半径动画 */
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCurrentMiddleCircleRadius - smallCircle.radius, mBigCircleRadius * (1 + mMaxCircleRadiusScaleRate) + 20);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(400);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentBigCircleRadius = (float) animation.getAnimatedValue();
                smallCircle.currentBigCircleRadius = currentBigCircleRadius;
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                
                startBigCircleRadiusAnim(++mPosition);
            }

        });
        
        
    }

    /**
     * 开始小圆坐标和中间圆半径变化动画
     */
    public void startAngleAndMiddleCircleRadiusAnim() {
        
        /* 小圆坐标动画 */
        for (int i = 0; i < mSmallCircleCount; i++) {
            
            final Circle smallCircle = mSmallCircles.get(i);
            float angle = (float) Math.toDegrees(smallCircle.radian);
            
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(angle, angle + 360);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(3600);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float angle = (float) animation.getAnimatedValue();
                    smallCircle.x = (float) (mBigCircle.x + smallCircle.currentBigCircleRadius * Math.cos(Math.toRadians(angle)));
                    smallCircle.y = (float) (mBigCircle.y + smallCircle.currentBigCircleRadius * Math.sin(Math.toRadians(angle)));
                    invalidate();
                }
            });

        }

        /* 中间圆半径动画 */
        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(mCurrentMiddleCircleRadius, mCurrentMiddleCircleRadius * (1 - mMaxCircleRadiusScaleRate));
        valueAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator2.setDuration(3600);
        valueAnimator2.start();
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentMiddleCircleRadius = (float) animation.getAnimatedValue();
                mCurrentMiddleCircleRadius = currentMiddleCircleRadius;
                invalidate();
            }
        });
        valueAnimator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                
                startBigCircleRadiusAnimRecovery(mSmallCircleCount - 1);
                startAngleAndMiddleCircleRadiusAnimRecovery();
            }
        });
    }

    /**
     * 开始当前抽象大圆半径变化恢复动画
     *
     * @param pos
     */
    private void startBigCircleRadiusAnimRecovery(int pos) {
        if ( pos == -1)
            return;

        mPosition = pos;
        final Circle smallCircle = mSmallCircles.get(mPosition);
        
        /* 抽象大圆的半径动画 */
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(smallCircle.currentBigCircleRadius, mCurrentMiddleCircleRadius - smallCircle.radius);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(400);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentBigCircleRadius = (float) animation.getAnimatedValue();
                smallCircle.currentBigCircleRadius = currentBigCircleRadius;
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                
                startBigCircleRadiusAnimRecovery(--mPosition);
            }

        });
    }

    /**
     * 开始小圆坐标和中间圆半径变化恢复动画
     */
    public void startAngleAndMiddleCircleRadiusAnimRecovery() {

        /* 小圆坐标动画 */
        for (int i = mSmallCircleCount - 1 ; i >= 0; i--) {

            final Circle smallCircle = mSmallCircles.get(i);
            float angle = (float) Math.toDegrees(smallCircle.radian);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(angle, angle + 360);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(3600);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float angle = (float) animation.getAnimatedValue();
                    smallCircle.x = (float) (mBigCircle.x + smallCircle.currentBigCircleRadius * Math.cos(Math.toRadians(angle)));
                    smallCircle.y = (float) (mBigCircle.y + smallCircle.currentBigCircleRadius * Math.sin(Math.toRadians(angle)));
                    invalidate();


                }
            });

        }
        
        /* 中间圆半径动画 */
        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(mCurrentMiddleCircleRadius, mCurrentMiddleCircleRadius / (1 - mMaxCircleRadiusScaleRate));
        valueAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator2.setDuration(3600);
        valueAnimator2.start();
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentMiddleCircleRadius = (float) animation.getAnimatedValue();
                mCurrentMiddleCircleRadius = currentMiddleCircleRadius;

                invalidate();
            }
        });
        valueAnimator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startBigCircleRadiusAnim(0);
                        startAngleAndMiddleCircleRadiusAnim();
                    }
                }, 200);

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
        public float radian;
        public float currentBigCircleRadius;
    }
}
