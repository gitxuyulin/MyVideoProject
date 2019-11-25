package com.example.xuyulin.myvideoproject.step6;

import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;

import static com.example.xuyulin.myvideoproject.step6.Camera2Render.RESULT_CODE_CAMERA;

/**
 * 作者： xuyulin on 2018/10/22.
 * 邮箱： xuyulin@yixia.com
 * 描述： Camera2是android5.0新增的api，Camera2与Camera差别比较大，采用的全新的模式，功能更加强大。
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Activity extends AppCompatActivity {

    private GLSurfaceView surfaceView;
    private ImageView iv;
    private Button btn;
    private Camera2Render camera2Render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_two);
        surfaceView = findViewById(R.id.surfaceView);
        iv = findViewById(R.id.iv);
        btn = findViewById(R.id.btn);
        initView();
    }

    public void initView() {
        camera2Render = new Camera2Render(this, surfaceView, iv, btn);
        surfaceView.setRenderer(camera2Render);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case RESULT_CODE_CAMERA:
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    //授权成功之后，调用系统相机进行拍照操作等
                    camera2Render.openCamera();
                } else {
                    //用户授权拒绝之后，友情提示一下就可以了
                    Toast.makeText(this, "请开启应用拍照权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera2Render.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera2Render.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera2Render.onDestory();
    }
}
