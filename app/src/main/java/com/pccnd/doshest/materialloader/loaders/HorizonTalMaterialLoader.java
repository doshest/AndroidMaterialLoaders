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
 * Created by doshest on 2015/11/7.
 */
public class HorizonTalMaterialLoader extends View {

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
    private float mMaxStaticCircleRadiusScaleRate = 0.4f;

    /**
     * 静态圆个数
     */
    private int mStaticCircleCount = 5;

    /**
     * 圆与圆之间的间隔距离
     */
    private float mDivideWidth = 3 * mCurrentStaticCircleRadius;

    /**
     * 最大粘连长度
     */
    private float mMaxAdherentLength = 3.5f * mCurrentStaticCircleRadius;

    /**
     * 全局静态圆变量
     */
    private Circle mStaticCircle;

    /**
     * 动态圆
     */
    private Circle mDynamicCircle = new Circle();
    /**
     * 维护静态圆容器
     */
    private List<Circle> mStaticCircles = new ArrayList<Circle>();

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
    public HorizonTalMaterialLoader(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     *
     * @param context
     * @param attrs
     */
    public HorizonTalMaterialLoader(Context context, AttributeSet attrs) {
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
    public HorizonTalMaterialLoader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mWidth = (int)((mStaticCircleCount + 1) * (mCurrentStaticCircleRadius * 2 + mDivideWidth));
        mHeight = (int)(2 * mCurrentStaticCircleRadius * (1 + mMaxStaticCircleRadiusScaleRate));

        /* 动态圆 */
        mDynamicCircle.radius = mCurrentStaticCircleRadius * 3 / 4;;
        mDynamicCircle.x = mDynamicCircle.radius;
        mDynamicCircle.y = mHeight / 2;

        /* 静态圆 */
        for (int i = 0; i < mStaticCircleCount; i++) {
            mStaticCircle = new Circle();
            mStaticCircle.radius = mCurrentStaticCircleRadius;
            mStaticCircle.x = (mStaticCircle.radius * 2 + mDivideWidth) * (i + 1);
            mStaticCircle.y = mHeight / 2;
            mStaticCircles.add(mStaticCircle);
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

        /* 动态圆 */
        canvas.drawCircle(mDynamicCircle.x, mDynamicCircle.y, mDynamicCircle.radius, mPaint);

        /* 静态圆 */
        for (int i = 0; i < mStaticCircleCount; i++) {
            mStaticCircle = mStaticCircles.get(i);

            /* 判断哪个圆可以作贝塞尔曲线 */
            if (doAdhere(i)) {
                canvas.drawCircle(mStaticCircle.x, mStaticCircle.y, mCurrentStaticCircleRadius, mPaint);
                // drawAdherentBody(canvas, i);
                Path path = drawAdherentBody(mStaticCircle.x, mStaticCircle.y, mCurrentStaticCircleRadius,45,
                        mDynamicCircle.x, mDynamicCircle.y, mDynamicCircle.radius,45);
                canvas.drawPath(path, mPaint);

            } else {
                canvas.drawCircle(mStaticCircle.x, mStaticCircle.y, mStaticCircle.radius, mPaint);
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

        mStaticCircle = mStaticCircles.get(position);

        /* 根据动态球与静态球的相对位置计算因子 */
        float difference = mDynamicCircle.x - mStaticCircle.x;
        float factor;
        if (difference > 0)
            factor = 1;
        else
            factor = -1;

         /* 求三角函数 */
        float sin = (float) Math.sin(Math.toRadians(45));
        float cos = (float) Math.cos(Math.toRadians(45));

        /* 四个点 */
        float staticX1 = mStaticCircle.x + factor * mCurrentStaticCircleRadius * cos;
        float staticY1 = mStaticCircle.y + factor * mCurrentStaticCircleRadius * sin;

        float staticX2 = mStaticCircle.x + factor * mCurrentStaticCircleRadius * cos;
        float staticY2 = mStaticCircle.y - factor * mCurrentStaticCircleRadius * sin;

        float dynamicX1 = mDynamicCircle.x - factor * mDynamicCircle.radius * cos;
        float dynamicY1 = mDynamicCircle.y + factor * mDynamicCircle.radius * sin;

        float dynamicX2 = mDynamicCircle.x - factor * mDynamicCircle.radius * cos;
        float dynamicY2 = mDynamicCircle.y - factor * mDynamicCircle.radius * sin;

        /* 控制点 */
        float anchorX1 = ( staticX1 + dynamicX2 ) / 2;
        float anchorY1 = ( staticY1 + dynamicY2 ) / 2 - factor * 3;

        float anchorX2 = ( staticX2 + dynamicX1 ) / 2;
        float anchorY2 = ( staticY2 + dynamicY1 ) / 2 + factor * 3;


        /* 画贝塞尔曲线 */
        mPath.reset();
        mPath.moveTo(staticX1, staticY1);
        mPath.quadTo(anchorX1, anchorY1, dynamicX1, dynamicY1);
        mPath.lineTo(dynamicX2, dynamicY2);
        mPath.quadTo(anchorX2, anchorY2, staticX2, staticY2);
        mPath.lineTo(staticX1, staticY1);
        canvas.drawPath(mPath, mPaint);
    }



    /**
     * 判断粘连范围，动态改变静态圆大小
     *
     * @param position
     * @return
     */
    private boolean doAdhere(int position) {

        mStaticCircle = mStaticCircles.get(position);

        /* 半径变化 */
        float distance = (float) Math.sqrt(Math.pow(mDynamicCircle.x - mStaticCircle.x, 2) + Math.pow(mDynamicCircle.y - mStaticCircle.y, 2));
        float scale = mMaxStaticCircleRadiusScaleRate -  mMaxStaticCircleRadiusScaleRate * (distance / mMaxAdherentLength);
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

        /* 动态圆的x坐标动画 */
        ValueAnimator xValueAnimator = ValueAnimator.ofFloat(mDynamicCircle.x, mWidth - mDynamicCircle.radius);
        xValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        xValueAnimator.setDuration(2500);
        xValueAnimator.setRepeatCount(Animation.INFINITE);
        xValueAnimator.setRepeatMode(Animation.REVERSE);
        xValueAnimator.start();
        xValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDynamicCircle.x = (float) animation.getAnimatedValue();
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
