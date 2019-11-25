package com.example.xuyulin.myvideoproject.step7;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;

import java.io.IOException;

/**
 * 作者： xuyulin on 2018/6/14.
 * 邮箱： xuyulin@yixia.com
 * 描述： MediaCodec实现视频的H264编解码
 */
public class VideoMediaEncoderActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private SurfaceView surface_view;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean startPreview = false;
    private MediaCodecEncoder codecEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_media_encoder);
        surface_view = findViewById(R.id.surface_view);
        surfaceHolder = surface_view.getHolder();
        surfaceHolder.addCallback(this);
        codecEncoder = new MediaCodecEncoder();
        openCamera();
    }

    public void openCamera() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(screenHeight, screenWidth);
        camera.setParameters(parameters);
        camera.setPreviewCallback(this);
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
        if (startPreview) {
            try {
                codecEncoder.encoder(data, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(View view) {
        startPreview = true;
        codecEncoder.initEncoder();
        Toast.makeText(this, "录制开始", Toast.LENGTH_SHORT).show();
    }

    public void stop(View view) {
        startPreview = false;
        codecEncoder.closeEncoder();
        Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show();
    }

    public void play(View view) {
        Toast.makeText(this, "开始播放", Toast.LENGTH_SHORT).show();
        startActivity((new Intent()).setClass(this, VideoMediaDecoderActivity.class));
    }

}
