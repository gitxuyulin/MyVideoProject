package com.example.xuyulin.myvideoproject.step12;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Create by 徐玉林.
 * Create on 2019-12-20.
 * Describe: =>
 */
public class FBOActivity extends AppCompatActivity {
    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new MySurfaceView(this);
        setContentView(mGLView);

    }
}
