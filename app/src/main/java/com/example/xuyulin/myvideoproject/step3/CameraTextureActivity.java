package com.example.xuyulin.myvideoproject.step3;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

import com.example.xuyulin.myvideoproject.R;

import java.io.IOException;

/**
 * 作者： xuyulin on 2018/6/5.
 * 邮箱： xuyulin@yixia.com
 * 描述： camera录制视频的类
 */
public class CameraTextureActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private String TAG = this.getClass().getSimpleName();
    private TextureView texture_view;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_camera);
        texture_view = findViewById(R.id.texture_view);
        texture_view.setSurfaceTextureListener(this);
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            camera.setPreviewTexture(surface);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
