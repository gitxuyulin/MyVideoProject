package com.example.xuyulin.myvideoproject.step8;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * 作者： xuyulin on 2018/7/2.
 * 邮箱： xuyulin@yixia.com
 * 描述： 自定义的suefaceview
 */
public class MySurfaceView extends SurfaceView {

    private double myRatio;

    public MySurfaceView(Context context) {
        super(context);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRatio(double ratio) {
        if (ratio > 0) {
            this.myRatio = ratio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //getMeasuredHeight()返回的是原始测量高度，与屏幕无关，即超出屏幕的也会被计算在内
        //getHeight()返回的是在屏幕上显示的高度
        //MeasureSpec.getSize获取总宽度,是包含padding值
        if (myRatio > 0) {
            int initWidth = MeasureSpec.getSize(widthMeasureSpec);
            int intitHeight = MeasureSpec.getSize(heightMeasureSpec);
            initWidth -= (getPaddingLeft() + getPaddingRight());
            intitHeight -= (getPaddingTop() + getPaddingBottom());
            double viewRatio = initWidth / intitHeight;
            double dValue = myRatio / viewRatio - 1;
            if (Math.abs(dValue) > 0.01) {
                if (dValue > 0) {
                    intitHeight = (int) (initWidth / myRatio);
                } else {
                    initWidth = (int) (intitHeight / myRatio);
                }
                initWidth += (getPaddingLeft() + getPaddingRight());
                intitHeight += (getPaddingTop() + getPaddingBottom());
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(intitHeight, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
