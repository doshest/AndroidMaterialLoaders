
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

import java.util.ArrayList;
import java.util.List;

/**
 * 几个小球在一个大球之间徘徊
 *
 *Created by doshest on 2015/11/7.
 */
public class LinearMaterialLoader extends View {

    /**
     * 宽度
     */
    private int mWidth;

    /**
     * 高度
     */
    private int mHeight;

    /**
     * 当前的静态圆半径
     */
    private float mCurrentStaticCircleRadius = 10f;

    /**
     * 静态圆变化半径的最大比率
     */
    private float mMaxStaticCircleRadiusScaleRate = 0.5f;

    /**
     * 动态圆个数
     */
    private int mDynamicCircleCount = 2;

    /**
     * 圆与圆之间的间隔距离
     */
    private float mDivideWidth = 2f * mCurrentStaticCircleRadius;

    /**
     * 最大粘连长度
     */
    private float mMaxAdherentLength = 3.5f * mCurrentStaticCircleRadius;

    /**
     * 静态圆
     */
    private Circle mStaticCircle = new Circle();

    /**
     * 动态圆
     */
    private Circle mDynamicCircle;

    /**
     * 维护静态圆容器
     */
    private List<Circle> mDynamicCircles = new ArrayList<Circle>();

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
    public LinearMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     * 
     * @param context
     * @param attrs
     */
    public LinearMaterialLoader(Context context, AttributeSet attrs) {
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
    public LinearMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mWidth = (int)((mDynamicCircleCount * 2) * (mCurrentStaticCircleRadius * 2 + mDivideWidth) + mCurrentStaticCircleRadius * 2);
        mHeight = (int)(2 * mCurrentStaticCircleRadius * (1 + mMaxStaticCircleRadiusScaleRate));

        /* 静态圆 */
        mStaticCircle.radius = mCurrentStaticCircleRadius;
        mStaticCircle.x = mWidth / 2;
        mStaticCircle.y = mHeight / 2;
        
        /* 动态圆 */
        for (int i = 0; i < mDynamicCircleCount; i++) {
            mDynamicCircle = new Circle();
            mDynamicCircle.radius = mCurrentStaticCircleRadius / 3 * 2;
            mDynamicCircle.x = mStaticCircle.x - (mStaticCircle.radius * 2 + mDivideWidth) * (i + 1);
            mDynamicCircle.y = mHeight / 2;
            mDynamicCircles.add(mDynamicCircle);
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
        canvas.drawCircle(mStaticCircle.x, mStaticCircle.y, mCurrentStaticCircleRadius, mPaint);
        
        /* 动态圆 */
        for (int i = 0; i < mDynamicCircleCount; i++) {
            mDynamicCircle = mDynamicCircles.get(i);
            
            canvas.drawCircle(mDynamicCircle.x, mDynamicCircle.y, mDynamicCircle.radius, mPaint);
            
            /* 判断哪个圆可以作贝塞尔曲线 */
            if (doAdhere()) {
                Path path = drawAdherentBody(mStaticCircle.x, mStaticCircle.y, mCurrentStaticCircleRadius,45,
                        mDynamicCircle.x, mDynamicCircle.y, mDynamicCircle.radius,45);
                canvas.drawPath(path,mPaint);
      
            } 
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
        
        /* 距离变化 */
        float distance = (float) Math.sqrt(Math.pow(mDynamicCircle.x - mStaticCircle.x, 2) + Math.pow(mDynamicCircle.y - mStaticCircle.y, 2));
       
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
        
        /* 动态圆的运动动画 */
        for (int i = 0; i < mDynamicCircleCount; i++) {
            final Circle dynamicCircle = mDynamicCircles.get(i);
            ValueAnimator xValueAnimator = ValueAnimator.ofFloat(dynamicCircle.x, dynamicCircle.x + (1 + mDynamicCircleCount) * (mDivideWidth + 2 * mStaticCircle.radius));
            xValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            xValueAnimator.setDuration(1600);
            xValueAnimator.setRepeatCount(Animation.INFINITE);
            xValueAnimator.setRepeatMode(Animation.REVERSE);
            xValueAnimator.start();
            xValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    dynamicCircle.x = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }

        /* 静态圆的半径动画 */
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mStaticCircle.radius, 1 * mStaticCircle.radius * (1 + mMaxStaticCircleRadiusScaleRate));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(800);
        valueAnimator.setRepeatCount(Animation.INFINITE);
        valueAnimator.setRepeatMode(Animation.REVERSE);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                 mCurrentStaticCircleRadius = (float) animation.getAnimatedValue();
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
