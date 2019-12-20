package com.example.xuyulin.myvideoproject.step11;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Create by 徐玉林.
 * Create on 2019-12-16.
 * Describe: =>
 */
public class DrawTexture {

    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 vPosition;\n" +
            "attribute vec2 vCoordinate;\n" +
            "uniform mat4 vMatrix;\n" +
            "varying vec2 aCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "gl_Position = vMatrix*vPosition;\n" +
            "aCoordinate=vCoordinate;\n" +
            "}";

    private static final String CAMERA_INPUT_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "vuniform samplerExternalOES vTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   gl_FragColor = texture2D(vTexture, textureCoordinate);\n" +
            "}";
    //矩阵坐标
    private final float vertexPoint[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    //纹理坐标
    private final float texturePoint[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };
    private final String mVertexShader;
    private final String mFragmentShader;
    protected FloatBuffer mGLCubeBuffer;
    protected FloatBuffer mGLTextureBuffer;
    private int mFrameWidth = -1;
    private int mFrameHeight = -1;
    private int mScreenW;
    private int mScreenH;
    protected int mGLProgId;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public DrawTexture(int width, int height, int screenW, int screenH) {
        mVertexShader = CAMERA_INPUT_VERTEX_SHADER;
        mFragmentShader = CAMERA_INPUT_FRAGMENT_SHADER;

        mGLCubeBuffer = ByteBuffer.allocateDirect(vertexPoint.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(vertexPoint).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(texturePoint.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(texturePoint).position(0);

        mFrameWidth = width;
        mFrameHeight = height;
        mScreenW = screenW;
        mScreenH = screenH;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mGLProgId = OpenGlUtils.loadProgram(mVertexShader, mFragmentShader);
        glHPosition = GLES20.glGetAttribLocation(mGLProgId, "vPosition");
        glHCoordinate = GLES20.glGetAttribLocation(mGLProgId, "vCoordinate");
        glHTexture = GLES20.glGetUniformLocation(mGLProgId, "vTexture");
        glHMatrix = GLES20.glGetUniformLocation(mGLProgId, "vMatrix");
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float sWH = mFrameWidth / (float) mFrameHeight;
        float sWidthHeight = width / (float) height;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 5);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 5);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    public void onDrawFrame(GL10 gl) {
//        GLES20.glViewport(0, 0, mScreenW, mScreenH);
//        GLES20.glUseProgram(mGLProgId);
//
//
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//
//        mGLCubeBuffer.position(0);
//        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
//        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
//        mGLTextureBuffer.position(0);
//        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
//                mGLTextureBuffer);
//
//        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
//        if (textureId != OpenGlUtils.NO_TEXTURE) {
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//            GLES20.glUniform1i(mGLUniformTexture, 0);
//        }
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
//        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

}
