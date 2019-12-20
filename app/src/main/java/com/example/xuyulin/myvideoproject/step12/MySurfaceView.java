package com.example.xuyulin.myvideoproject.step12;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Create by 徐玉林.
 * Create on 2019-12-20.
 * Describe: =>
 */
public class MySurfaceView extends GLSurfaceView {

    //    private MyRenderer mRenderer;
    private FBORenderer fboRenderer;

    public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2);
//      绘制普通三角形的渲染器
//      mRenderer=new MyRenderer(context);
//      this.setRenderer(mRenderer);
        fboRenderer = new FBORenderer(context);
        this.setRenderer(fboRenderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

}
