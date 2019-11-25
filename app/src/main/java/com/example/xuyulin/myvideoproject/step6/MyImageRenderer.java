package com.example.xuyulin.myvideoproject.step6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

import com.example.xuyulin.myvideoproject.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 作者： xuyulin on 2018/6/8.
 * 邮箱： xuyulin@yixia.com
 * 描述： 绘制图片的renderer
 */
public class MyImageRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private int[] textures = new int[1];
    private FloatBuffer textureBuffer;
    //纹理坐标数据
    private float texture[] = {
            // Mapping coordinates for the vertices
            0.0f, 1.0f, // top left (V2)
            0.0f, 0.0f, // bottom left (V1)
            1.0f, 1.0f, // top right (V4)
            1.0f, 0.0f // bottom right (V3)
    };
    private FloatBuffer verticyBuffer;
    //顶点坐标数据
    private float vertices[] = {-1.0f, -1.0f, 0.0f, // V1 - bottom left
            -1.0f, 1.0f, 0.0f, // V2 - top left
            1.0f, -1.0f, 0.0f, // V3 - bottom right
            1.0f, 1.0f, 0.0f // V4 - top right
    };

    public MyImageRenderer(Context context) {
        this.context = context;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticyBuffer = byteBuffer.asFloatBuffer();
        verticyBuffer.put(vertices);
        verticyBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.timg);
        //命名纹理对象
        //参数没事：
        //第一个参数：生成纹理名字的数量
        //第二个参数：存储纹理名称数组的第一个元素指针
        //第三个参数:起始位置
        gl.glGenTextures(1, textures, 0);
        //创建和使用纹理对象
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        //设置纹理贴图的参数属性
        //target —— 目标纹理，必须为GL_TEXTURE_1D或GL_TEXTURE_2D；
        //pname —— 用来设置纹理映射过程中像素映射的问题等，取值可以为：GL_TEXTURE_MIN_FILTER、GL_TEXTURE_MAG_FILTER、GL_TEXTURE_WRAP_S、GL_TEXTURE_WRAP_T；
        //param —— 实际上就是pname的值，可以参考MSDN
        //pname：
        //GL_TEXTURE_MIN_FILTER 设置最小过滤，第三个参数决定用什么过滤；
        //GL_TEXTURE_MAG_FILTER设置最大过滤，也是第三个参数决定；
        //GL_TEXTURE_WRAP_S；纹理坐标一般用str表示，分别对应xyz，2d纹理用st表示
        //GL_TEXTURE_WRAP_T   纹理和你画的几何体可能不是完全一样大的，wrap表示环绕，可以理解成让纹理重复使用，直到全部填充完成；
        //param；与第二个参数配合使用，一般取GL_LINEAR和GL_NEAREST，过滤形式
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //制定纹理
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();


        //启用二维纹理
        gl.glEnable(GL10.GL_TEXTURE_2D);
        //着色方式，可以选择flag或者smooth
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0, 0, 0, 0.5f);
        //指定深度缓冲区的清除值
        gl.glClearDepthf(1.0f);
        //启动深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);
        //指定用于深度缓冲比较值,GL_LEQUAL：如果输入的深度值小于或等于参考值，则通过
        gl.glDepthFunc(GL10.GL_LEQUAL);
        //反走样函数，第一个参数为指定颜色纹理插值的质量，第二个参数为给出最高质量的选择
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置视口大小
        gl.glViewport(0, 0, width, height);
        //对接下来要做什么进行一下声明
        //GL_PROJECTION 投影, GL_MODELVIEW 模型视图, GL_TEXTURE 纹理.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        //矩阵单位划
        gl.glLoadIdentity();
        //fovy定义可视角的大小，fovy值小，表示从相机（人眼）出发的光线的角度小，此时同等距离下，可观察到的视野范围较小，反之则大。
        //aspect定义物体显示在画板上的x和y方向上的比例。
        //zNear定义距离相机（人眼）最近处物体截面相距的距离。
        //zFar定义可观测到的物体的最远处截面相距相机的距离。
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除颜色和深度缓存
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -4.0f);


        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        //指定多边形在窗口坐标中的方向是逆时针还是顺时针的,GL_CCW说明逆时针多边形为正面，而GL_CW说明顺时针多边形为正面。
        gl.glFrontFace(GL10.GL_CW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticyBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

}
