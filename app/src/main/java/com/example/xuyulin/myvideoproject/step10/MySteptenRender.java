package com.example.xuyulin.myvideoproject.step10;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 作者： xuyulin on 2018/9/27.
 * 邮箱： xuyulin@yixia.com
 * 描述：
 */
public class MySteptenRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {
    private int myTextureId = 0;
    private GLSurfaceView mGLSurface;
    private Camera mCamera;
    private boolean isFirst;
    private SurfaceTexture surfaceTexture;
    private float[] mTextureTransformMatrix = new float[16];

    public MySteptenRender(GLSurfaceView glSurface, Camera camera) {
        mGLSurface = glSurface;
        mCamera = camera;
        isFirst = true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (MySteptenRender.class) {
            if (isFirst) {
                isFirst = false;
                new Runnable() {
                    @Override
                    public void run() {
                        int[] textures = new int[1];
                        GLES20.glGenTextures(1, textures, 0);//创建纹理
                        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);//绑定纹理
                        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                        myTextureId = textures[0];
                        surfaceTexture = new SurfaceTexture(myTextureId);
                        try {
                            mCamera.setPreviewTexture(surfaceTexture);
                            mCamera.setPreviewCallback(MySteptenRender.this);
                            mCamera.startPreview();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.run();
                surfaceTexture.updateTexImage();
                surfaceTexture.getTransformMatrix(mTextureTransformMatrix);
            }
        }

        int drawTexture = myTextureId;

        GLES20.glViewport(0, 0, 720, 1280);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mGLSurface.requestRender();
    }
}
