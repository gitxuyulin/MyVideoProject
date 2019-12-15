package com.example.xuyulin.myvideoproject.step11;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.xuyulin.myvideoproject.R;

import java.util.List;

import static android.graphics.ImageFormat.NV21;

/**
 * Create by 徐玉林.
 * Create on 2019-12-15.
 * Describe: =>
 */
public class PreviewActivity extends AppCompatActivity {

    private static final int cameraBack = 0;
    private static final int cameraFront = 1;
    private GLSurfaceView previewSurface;
    private Camera mCamera;
    private int width;
    private int height;
    private int mfps = 20;
    private boolean cameraAutoFocus = false;
    private boolean cameraFlashModes = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_water);
        initViews();
        openCamera();

    }

    public void initViews() {
        previewSurface = findViewById(R.id.preview_surface);
    }

    public void openCamera() {
        mCamera = Camera.open(cameraBack);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(NV21);
        //1.获取前后摄像头分辨率
        Camera.Size mSize = mCamera.new Size(1, 1);
        mSize.width = 720;
        mSize.height = 1280;
        width = mSize.width;
        height = mSize.height;
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizes) {
            Log.e("xyl", "手机支持分辨率:宽=" + size.width + "高=" + size.height);
        }
        if (!previewSizes.contains(mSize)) {
            mSize = findBestMatchVideoSize(previewSizes, mSize);
            width = mSize.width;
            height = mSize.height;
            Log.e("xyl", "最合适的分辨率:宽=" + width + "高=" + height);
        }
        parameters.setPreviewSize(mSize.width, mSize.height);
        int previewBufferSize = width * height * 3 / 2;

        //2.设置预览fps范围
        List<int[]> previewFpsRange = parameters.getSupportedPreviewFpsRange();
        int bestFpsIndex = -1;
        for (int i = 0; i < previewFpsRange.size(); i++) {
            int[] fpsRange = previewFpsRange.get(i);
            final float fps0 = (float) fpsRange[0] / 1000;
            final float fps1 = (float) fpsRange[1] / 1000;
            Log.e("xyl", "手机fps范围：" + fps0 + "--" + fps1);
            if ((fps0 == fps1) && (fps0 == mfps)) {
                bestFpsIndex = i;
                break;
            } else if (fps0 < mfps && fps1 > mfps) {
                bestFpsIndex = i;
                break;
            }
        }

        if (bestFpsIndex >= 0) {
            int[] fpsRange = previewFpsRange.get(bestFpsIndex);
            parameters.setPreviewFpsRange(fpsRange[0], fpsRange[1]);
        }

        //3.获取前后摄像头全时对焦支持情况.
        // 对于移动短视频录制来说，使用的对焦模式应该是continuous-video。
        // 这个模式会在录制过程中自动对焦，默认对焦点为Camera坐标系的原点。
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            cameraAutoFocus = true;
        } else {
            cameraAutoFocus = false;
        }

        //4.获取闪光灯情况
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)
                && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            cameraFlashModes = true;
        }

        //5.开启HDR
//        if (parameters.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_HDR)) {
//            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
//        }
        //6.开启防抖动
//        if (parameters.isVideoStabilizationSupported()) {
//            parameters.setVideoStabilization(true);
//        }

        mCamera.setParameters(parameters);
        mCamera.addCallbackBuffer(new byte[previewBufferSize]);
        mCamera.addCallbackBuffer(new byte[previewBufferSize]);
        mCamera.addCallbackBuffer(new byte[previewBufferSize]);
    }

    public Camera.Size findBestMatchVideoSize(List<Camera.Size> previewSizes, Camera.Size mSize) {
        if (previewSizes.size() < 1) {
            Camera.Size size = mCamera.new Size(0, 0);
            return size;
        }
        int mDstWidth, mDstHeight;
        if (mSize.width > mSize.height) {
            mDstWidth = mSize.width;
            mDstHeight = mSize.height;
        } else {
            mDstWidth = mSize.height;
            mDstHeight = mSize.width;
        }
        float error = Float.MAX_VALUE;
        int bestMatchIndex = 0;
        //找到第一个满足条件的，宽或者高大出自己设置的，取改size
        for (int i = 0; i < previewSizes.size(); i++) {
            int bWidth = previewSizes.get(i).width;
            int bHeight = previewSizes.get(i).height;
            if (bWidth < mDstWidth || bHeight < mDstHeight) continue;
            final float err = (float) (bWidth - mDstWidth) / mDstWidth + (float) (bHeight - mDstHeight) / mDstHeight;
            if (err < error) {
                error = err;
                bestMatchIndex = i;
            }
        }
        if (bestMatchIndex == -1) {
            return previewSizes.get(previewSizes.size() - 1);
        } else {
            return previewSizes.get(bestMatchIndex);
        }
    }
}
