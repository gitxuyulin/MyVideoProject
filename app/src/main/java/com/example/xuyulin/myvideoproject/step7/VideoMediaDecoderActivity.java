package com.example.xuyulin.myvideoproject.step7;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;

/**
 * 作者： xuyulin on 2018/6/14.
 * 邮箱： xuyulin@yixia.com
 * 描述： 视频解码类
 */
public class VideoMediaDecoderActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaCodecDecoder codecDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_media_decoder);
        surfaceView = findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        codecDecoder = new MediaCodecDecoder();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Toast.makeText(this, "开始播放", Toast.LENGTH_SHORT).show();
        codecDecoder.initCodecDecoder(VideoMediaDecoderActivity.this, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        codecDecoder.closeCodecDecoder();
    }
}
