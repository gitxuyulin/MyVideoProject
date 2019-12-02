package com.example.xuyulin.myvideoproject.step10;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;

/**
 * 作者： xuyulin on 2018/7/24.
 * 邮箱： xuyulin@yixia.com
 * 描述： 摄像头采集添加水印的类
 */
public class CameraCollectWaterActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();
    private String dateSource = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xiaokaxiu.mp4";
    private GLSurfaceView water_surface;
    private Button start_addwater;
    private Camera camera;
    private Camera.Parameters parameter;
    public static final int CAMERA_BACK = 0;
    public static final int CAMERA_FRONT = 1;
    private int cameraHeight = 720;
    private int cameraWidth = 1280;

    private MySteptenRender myRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_water);
        water_surface = findViewById(R.id.water_surface);
        start_addwater = findViewById(R.id.start_addwater);
        start_addwater.setOnClickListener(this);
        initCamera();
    }

    public void initCamera() {
        camera = Camera.open(CAMERA_FRONT);
        parameter = camera.getParameters();
        parameter.setPreviewFormat(ImageFormat.NV21);
        Camera.Size mCameraSize = camera.new Size(1, 1);
        mCameraSize.height = cameraHeight;
        mCameraSize.width = cameraWidth;
        if (!parameter.getSupportedPreviewSizes().contains(mCameraSize)) {
            for (int i = parameter.getSupportedPreviewSizes().size() - 1; parameter.getSupportedPreviewSizes().size() >= 0; i--) {
                if (parameter.getSupportedPreviewSizes().get(i).height > cameraHeight) {
                    cameraHeight = parameter.getSupportedPreviewSizes().get(i).height;
                    cameraWidth = parameter.getSupportedPreviewSizes().get(i).width;
                    break;
                }
            }
        }
        parameter.setPreviewSize(cameraWidth, cameraHeight);
        myRender = new MySteptenRender(water_surface, camera);
        water_surface.setRenderer(myRender);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_addwater:
                Toast.makeText(this, "视频添加水印", Toast.LENGTH_SHORT).show();
                //时间不够了，未实现
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
