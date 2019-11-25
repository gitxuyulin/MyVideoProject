package com.example.xuyulin.myvideoproject.step1.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.example.xuyulin.myvideoproject.R;

/**
 * 作者： xuyulin on 2018/5/23.
 * 邮箱： xuyulin@yixia.com
 * 描述：
 */

public class MSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private CanvasThread thread;
    private int width;
    private int height;

    public MSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        width = manager.getDefaultDisplay().getWidth();
        height = manager.getDefaultDisplay().getHeight();

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        thread = new CanvasThread(holder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRun(true);
        new Thread(thread, "绘制").start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRun(false);
    }

    public class CanvasThread implements Runnable {
        private SurfaceHolder mHolder;
        private boolean isRun;
        private int num = 0;

        public CanvasThread(SurfaceHolder holder) {
            this.mHolder = holder;
        }

        public void setRun(boolean isRun) {
            this.isRun = isRun;
        }

        @Override
        public void run() {
            while (isRun) {
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas();
                    canvas.drawColor(Color.WHITE);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(3f);
                    paint.setTextSize(50);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timg);
                    Rect rectOne = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    Rect rectTwo = new Rect(0, 0, width, height);
                    //第二个参数是图片的裁剪区域，就是你要显示图片的哪一块，Rect中的四个参数分别代表左上右下，我这里是显示的全部图片
                    //第三个参数是你要将图片显示到手机的哪一区域，我这里也是显示整个手机屏幕
                    //还有一点需要注意的是，canvas的绘制先后顺序是有区别的，如果我先drawText然后drawBitmap，那么文字就会被图片遮盖住
                    canvas.drawBitmap(bitmap, rectOne, rectTwo, new Paint());
                    //第二个和第三个参数自己检验的是距离顶部和左边的距离
                    canvas.drawText("我是第" + num + "名", 200, 400, paint);
                    num++;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
