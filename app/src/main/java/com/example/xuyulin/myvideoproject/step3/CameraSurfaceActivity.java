package com.example.xuyulin.myvideoproject.step3;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.xuyulin.myvideoproject.R;

import java.io.IOException;

/**
 * 作者： xuyulin on 2018/6/5.
 * 邮箱： xuyulin@yixia.com
 * 描述： camera录制视频的类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CameraSurfaceActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private String TAG = this.getClass().getSimpleName();
    public static final String cameraPath = Environment.getExternalStorageDirectory() + "/mengmeng.mp4";
    private SurfaceView surfaceView;
    private Camera camera;
    private int audioSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_camera);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        // 打开摄像头并将展示方向旋转90度
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        //将Camera.Paramers作为参数传入，这样即可对相机的拍照参数进行控制
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);


        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
            }
        });
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
        camera.release();
        Log.e("xyl", "视频大小" + audioSize);
    }

}
