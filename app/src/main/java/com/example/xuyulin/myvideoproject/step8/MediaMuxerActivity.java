package com.example.xuyulin.myvideoproject.step8;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;

import java.io.IOException;
import java.util.List;

/**
 * 作者： xuyulin on 2018/6/20.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音视频的采集封装成mp4输出
 */
public class MediaMuxerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private String TAG = this.getClass().getSimpleName();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private MediaMuxerRunnable mediaMuxerRunnable;
    private boolean startOrStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_media_muxer);
        surfaceView = findViewById(R.id.muxer_surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        openCamera();
    }

    public void startMuxer(View view) {
        Toast.makeText(this, "录制开始", Toast.LENGTH_SHORT).show();
        startOrStop = true;
        mediaMuxerRunnable = MediaMuxerRunnable.startMediaMuxer();
    }

    public void stopMuxer(View view) {
        Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show();
        startOrStop = false;
        mediaMuxerRunnable.stopMediaMuxer();
    }

    public void openCamera() {
        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setFlashMode("off");
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        parameters.setPreviewSize(1920, 1080);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setPreviewCallback(this);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (startOrStop) {
            mediaMuxerRunnable.addVideoByte(data);
        }
    }
}
