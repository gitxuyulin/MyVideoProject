package com.example.xuyulin.myvideoproject.step12;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Create by 徐玉林.
 * Create on 2019-12-20.
 * Describe: =>
 */
public class FBORenderer implements GLSurfaceView.Renderer {

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModuleMatrix = new float[16];
    private final float[] mViewProjectionMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    public static int sScreenWidth;
    public static int sScreenHeight;
    private Shape_FBO mRectangle;
    float yAngle;
    float xAngle;
    private Context mContext;

    public FBORenderer(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1);
        mRectangle = new Shape_FBO(mContext);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        sScreenWidth = width;
        sScreenHeight = height;
        GLES20.glViewport(0, 0, width, height);
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, (float) width / height, 2, 5);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Matrix.setIdentityM(mModuleMatrix, 0);
        Matrix.rotateM(mModuleMatrix, 0, xAngle, 1, 0, 0);
        Matrix.rotateM(mModuleMatrix, 0, yAngle, 0, 1, 0);
        Matrix.multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewProjectionMatrix, 0, mModuleMatrix, 0);
//        GLES20.glViewport(0, 0, 1024, 1024);
        mRectangle.draw(mMVPMatrix, mModuleMatrix);
        mRectangle.draw(mMVPMatrix, mModuleMatrix);
    }

}
