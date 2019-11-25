package com.example.xuyulin.myvideoproject.step1.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.xuyulin.myvideoproject.R;

/**
 * 作者： xuyulin on 2018/5/15.
 * 邮箱： xuyulin@yixia.com
 * 描述： Bitmap绘图的两种方式，第一种，根据图片复制出一片区域并绘图
 */

public class ImageViewActivity extends AppCompatActivity {

    private ImageView myimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        myimage = findViewById(R.id.myimage);
        WindowManager manager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;  //以要素为单位
        int height = metrics.heightPixels;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boy);
        Bitmap newBit = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Paint paint = new Paint();
        Canvas canvas = new Canvas(newBit);
        Matrix matrix = new Matrix();
        //平移操作
        //数字不要写的太大，不然可能会平移出imageview的绘制区域而导致看不见，因为imageview为warpcontent模式，区域不足够大
//        matrix.postTranslate(50, 50);
        //旋转变换
        matrix.postRotate(90);
        Log.e("xyl", width + "  " + height);
        matrix.postTranslate(width/2, 0);
        //缩放变换
//        matrix.postScale(1,2);
        canvas.drawBitmap(bitmap, matrix, paint);
        myimage.setImageBitmap(newBit);

    }
}
