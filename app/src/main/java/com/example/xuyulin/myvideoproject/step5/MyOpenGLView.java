package com.example.xuyulin.myvideoproject.step5;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 作者： xuyulin on 2018/6/7.
 * 邮箱： xuyulin@yixia.com
 * 描述： 自定义的openglsurfaceview
 * https://blog.csdn.net/huachao1001/article/details/52044602
 */
public class MyOpenGLView extends GLSurfaceView {

    public MyOpenGLView(Context context) {
        super(context);
        init();
    }

    public MyOpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //关闭抗抖动，对于颜色较少的系统可以通过牺牲分辨率通过抖动增加颜色数量
                gl.glDisable(GL10.GL_DITHER);
                //设置清屏颜色
                gl.glClearColor(0, 0, 0, 0);
                //设置hint，这里为快速模式
                gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
                //设置深度检索
                gl.glEnable(GL10.GL_DEPTH_TEST);

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                //设置视口，0.0表示原点位置，再设置width, height，则表示整个屏幕
                gl.glViewport(0, 0, width, height);
                //设置投影矩阵
                gl.glMatrixMode(GL10.GL_PROJECTION);
                //矩阵单位化
                gl.glLoadIdentity();
                //设置视口的大小
                //参数一表示 在近平面上原点到左平面的距离
                //参数二表示 在近平面上原点到右平面的距离
                //参数三四分别是到上和下
                //参数五表示近平面
                //参数六表示愿平面距离，严格意义上可以任意给值，但为了屏幕正常显示自己想要的图形，应该适当给值
                //r是屏幕的宽高比，正常情况r<1即是高大于宽，把高设置为1，那么原点到屏幕的距离就是r
                int r = width / height;
                gl.glFrustumf(-r, r, -1, 1, 1, 10);

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //清除颜色和深度缓存
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                //设置模型矩阵
                gl.glMatrixMode(GL10.GL_MODELVIEW);
                gl.glLoadIdentity();
                //xyz轴上的偏移量
                gl.glTranslatef(0, 0, 0);


                float[] ver = {
                        0, 0, 0,
                        0.5f, 1, 0,
                        1, 0, 0

                };
                //顶点颜色
                int one = 65535;//支持65535色彩通道
                float[] color = {
                        0, 0, 0, one,
                        0, one, 0, 0,
                        0, 0, one, 0
                };
                //启用定点坐标数组
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                //启用颜色数组
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
                //设置画笔
                //为画笔指定顶点数据
                /**
                 * 参数1坐标个数
                 * 参数2顶点数据类型
                 * 参数3 连续顶点坐标数据的间隔
                 * 参数4顶点数缓冲
                 */
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getFloatbuffer(ver));
                //为画笔指定顶点数据颜色
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, getFloatbuffer(color));

                /**
                 * 参数1 绘制模型 点 线段 三角形
                 *2 数组缓存开始的位置
                 * 3 顶部观点个数
                 */
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, ver.length / 3);
            }
        });
        //RENDERMODE_CONTINUOUSLY 主动渲染模式，消耗性能
        //RENDERMODE_WHEN_DIRTY 被动渲染模式，需要刷新
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public FloatBuffer getFloatbuffer(float[] ver) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(ver.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = vbb.asFloatBuffer();
        buffer.put(ver);
        buffer.position(0);
        return buffer;
    }
}
