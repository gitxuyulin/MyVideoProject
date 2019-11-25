package com.example.xuyulin.myvideoproject.step6;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 作者： xuyulin on 2018/6/8.
 * 邮箱： xuyulin@yixia.com
 * 描述： opengl绘制图片
 */
public class MyOpenGLImageActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setRenderer(new MyImageRenderer(this));
        setContentView(glSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
