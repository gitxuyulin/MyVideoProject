package com.example.xuyulin.myvideoproject.step11;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Create by 徐玉林.
 * Create on 2019-12-15.
 * Describe: =>
 */
public class PreviewRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {

    private int mOutputTextureId = 0;
    private SurfaceTexture surfaceTexture;
    private GLSurfaceView mView;
    private float[] mTextureTransformMatrix = new float[16];

    public PreviewRender(GLSurfaceView previewSurface) {
        mView = previewSurface;
    }

    public void setUpCamera(Camera camera) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mOutputTextureId = textures[0];

        try {
            surfaceTexture = new SurfaceTexture(mOutputTextureId);
            surfaceTexture.setOnFrameAvailableListener(this);
            camera.setPreviewTexture(surfaceTexture);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e("xyl", "onFrameAvailable");
        mView.requestRender();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e("xyl", "onPreviewFrame");
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mTextureTransformMatrix);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e("xyl", "onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e("xyl", "onSurfaceChanged");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e("xyl", "onDrawFrame");
    }

}
