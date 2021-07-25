package com.xuanfeng.countdownprogressview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Created by xuanfengwuxiang on 2021/7/23.
 * 椭圆形(操场形状)倒计时
 */

public class OvalProgressBar extends View {

    private Context mContext;
    //圆环参数
    private int mDefaultRingColor;//圆环颜色
    private float mDefaultRingWidth;//圆环宽度
    //进度条参数
    private int mProgressColor;//进度条颜色
    private float mProgressWidth;//进度条宽度
    private float mProgressValue;//倒计时进度数值

    //其他
    private int mCountDownTime;//倒计时时间
    private OnCountDownFinishListener mOnCountDownFinishListener;//计时结束监听器
    //画笔
    private Paint mDefaultRingPaint;
    private Paint mProgressPaint;

    // 作画参数
    private RectF mLeftArcRectF;//左边的半圆
    private RectF mRightArcRectF;//右边的半圆


    public OvalProgressBar(Context context) {
        this(context, null);
    }

    public OvalProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OvalProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttributes(attrs);
        init();
    }

    //获取自定义属性
    private void getAttributes(@Nullable AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CountDownProgressBar);
        mDefaultRingColor = typedArray.getColor(R.styleable.CountDownProgressBar_default_ring_color, mContext.getResources().getColor(R.color.default_ring_color));
        mDefaultRingWidth = typedArray.getDimension(R.styleable.CountDownProgressBar_default_ring_width, 2);

        mProgressColor = typedArray.getColor(R.styleable.CountDownProgressBar_progress_color, mContext.getResources().getColor(R.color.progress_color));
        mProgressWidth = typedArray.getDimension(R.styleable.CountDownProgressBar_progress_width, 2);

        mCountDownTime = typedArray.getInteger(R.styleable.CountDownProgressBar_count_down_time, 15);

        typedArray.recycle();
    }

    private void init() {
        initPaint();
    }

    //初始化画笔
    private void initPaint() {
        //圆环画笔
        mDefaultRingPaint = new Paint();
        mDefaultRingPaint.setAntiAlias(true);
        mDefaultRingPaint.setDither(true);
        mDefaultRingPaint.setStyle(Paint.Style.STROKE);
        mDefaultRingPaint.setStrokeWidth(mDefaultRingWidth);
        mDefaultRingPaint.setColor(mDefaultRingColor);

        //进度条画笔
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setDither(true);//防抖动
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);//线帽

    }

    @Override//Tip-------onMeasure只需要处理wrap_content这种情况。match_parent、具体宽高这2种情况交给super处理就好。
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int strokeWidth = (int) Math.max(mDefaultRingWidth, mProgressWidth);//圆圈的宽度


        if (widthMode != MeasureSpec.EXACTLY) {
            int widthSize = getPaddingLeft() + getPaddingRight() + strokeWidth * 2;//整个View的宽度
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            int heightSize = getPaddingTop() + getPaddingBottom() + strokeWidth * 2;//整个View的高度
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initPath();
    }

    /**
     * 初始化作画路径相关参数
     */
    private void initPath() {
        mLeftArcRectF = new RectF(mProgressWidth / 2, mProgressWidth / 2, getHeight() - mProgressWidth / 2, getHeight() - mProgressWidth / 2);
        mRightArcRectF = new RectF(getWidth() - getHeight(), mProgressWidth / 2, getWidth() - mProgressWidth / 2, getHeight() - mProgressWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();//canvas初始状态保存
        canvas.translate(getPaddingLeft(), getPaddingTop());

        //画默认椭圆
        canvas.drawArc(mLeftArcRectF, 90, 180, false, mDefaultRingPaint);
        canvas.drawLine(getHeight() / 2, mProgressWidth / 2, getWidth() - getHeight() / 2, mProgressWidth / 2, mDefaultRingPaint);
        canvas.drawArc(mRightArcRectF, -90, 180, false, mDefaultRingPaint);
        canvas.drawLine(getHeight() / 2, getHeight() - mProgressWidth / 2, getWidth() - getHeight() / 2, getHeight() - mProgressWidth / 2, mDefaultRingPaint);


        // 此处计算4段作画时间分配
        float leftArcLength = (3.14f * getHeight() / 2);
        int topLineLength = getWidth() - getHeight();
        float perimeter = (leftArcLength + topLineLength) * 2;
        float leftArcRate = (leftArcLength / perimeter);
        float topLineRate = (topLineLength / perimeter);

        float leftArcPercentage = (leftArcRate * 100f);
        float topArcPercentage = (topLineRate * 100f);
        float rightArcPercentage = leftArcPercentage;
        float bottomLinePercentage = topArcPercentage;

        //画进度圆弧
        if (mProgressValue <= leftArcPercentage) {
            canvas.drawArc(mLeftArcRectF, 90, 180 * (mProgressValue / leftArcPercentage), false, mProgressPaint);
        } else if (mProgressValue <= leftArcPercentage + topArcPercentage) {
            canvas.drawArc(mLeftArcRectF, 90, 180, false, mProgressPaint);
            float startX = getHeight() / 2;
            canvas.drawLine(startX, mProgressWidth / 2, startX + topLineLength * ((mProgressValue - leftArcPercentage) / topArcPercentage), mProgressWidth / 2, mProgressPaint);
        } else if (mProgressValue <= leftArcPercentage + topArcPercentage + rightArcPercentage) {
            canvas.drawArc(mLeftArcRectF, 90, 180, false, mProgressPaint);
            canvas.drawLine(getHeight() / 2, mProgressWidth / 2, getWidth() - getHeight() / 2, mProgressWidth / 2, mProgressPaint);
            canvas.drawArc(mRightArcRectF, -90, 180 * ((mProgressValue - leftArcPercentage - topArcPercentage) / rightArcPercentage), false, mProgressPaint);
        } else {
            canvas.drawArc(mLeftArcRectF, 90, 180, false, mProgressPaint);
            canvas.drawLine(getHeight() / 2, mProgressWidth / 2, getWidth() - getHeight() / 2, mProgressWidth / 2, mProgressPaint);
            canvas.drawArc(mRightArcRectF, -90, 180, false, mProgressPaint);

            float startX = getWidth() - getHeight() / 2;
            canvas.drawLine(startX, getHeight() - mProgressWidth / 2, startX - topLineLength * ((mProgressValue - leftArcPercentage - topArcPercentage - rightArcPercentage) / bottomLinePercentage), getHeight() - mProgressWidth / 2, mProgressPaint);
        }


        canvas.restore();//canvas初始状态恢复
    }

    public void startCountDown() {
        ValueAnimator valueAnimator = getValueAnimator(mCountDownTime * 1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgressValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnCountDownFinishListener != null) {
                    mOnCountDownFinishListener.countDownFinished();
                }
            }

        });
        valueAnimator.start();
    }

    private ValueAnimator getValueAnimator(long countdownTime) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 100);
        valueAnimator.setDuration(countdownTime);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(0);
        return valueAnimator;
    }

    public OvalProgressBar setOnCountDownFinishListener(OnCountDownFinishListener onCountDownFinishListener) {
        mOnCountDownFinishListener = onCountDownFinishListener;
        return this;
    }

    public void removeOnCountDownFinishListener() {
        mOnCountDownFinishListener = null;
    }

    public OvalProgressBar setDefaultRingColor(int defaultRingColor) {
        mDefaultRingColor = defaultRingColor;
        return this;
    }

    public OvalProgressBar setDefaultRingWidth(float defaultRingWidth) {
        mDefaultRingWidth = defaultRingWidth;
        return this;
    }

    public OvalProgressBar setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        return this;
    }

    public OvalProgressBar setProgressWidth(float progressWidth) {
        mProgressWidth = progressWidth;
        return this;
    }

    public OvalProgressBar setCountDownTime(int countDownTime) {
        mCountDownTime = countDownTime;
        return this;
    }

    public interface OnCountDownFinishListener {
        void countDownFinished();
    }

}
