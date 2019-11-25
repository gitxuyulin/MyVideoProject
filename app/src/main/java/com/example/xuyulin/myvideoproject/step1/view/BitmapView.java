package com.example.xuyulin.myvideoproject.step1.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.xuyulin.myvideoproject.R;

/**
 * 作者： xuyulin on 2018/5/17.
 * 邮箱： xuyulin@yixia.com
 * 描述：
 */

public class BitmapView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private int width;
    private int height;

    public BitmapView(Context context) {
        super(context);
    }

    public BitmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(10);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boy);
        mMatrix = new Matrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //平移视图
//        mMatrix.postTranslate(width / 2, height / 2);
//        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        //旋转视图
        //这里需要注意下，他旋转的并不只是这张图片，而是整个view，即显示在手机上的区域，如果旋转90度那么图片的右边界会与手机屏幕的左边界重合
//        mMatrix.reset();
//        mMatrix.postRotate(90);
//        mMatrix.postTranslate(width, height / 2);
//        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        //缩放视图
//        mMatrix.reset();
//        mMatrix.postScale(1,2);
//        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        //错切
//        mMatrix.postSkew(0, 1);
//        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        //对称变换x轴
        //这里使用了矩阵变换，假设一个点的起始位置为（x0，y0），要移动到（x，y）位置，我们用mx和my表示移动的距离，
        // 那么x=x0+mx，y=y0+my，那么矩阵表示为（x0，y0，1）={1，0，mx}{0，1，my}{0，0，1}（x，y，1），用矩阵表示的，
        //运算方式为x分别和第一个集合中的三个数分别相乘，然后再相加，y和第二个集合同样操作。
        float[] matrix_x = new float[]{1f, 0f, 0f, 0f, -1f, 120f, 0f, 0f, 1f};
        mMatrix.setValues(matrix_x);
        mMatrix.postTranslate(0, height);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
    }
}
