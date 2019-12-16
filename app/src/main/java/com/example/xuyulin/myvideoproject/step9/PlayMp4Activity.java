package com.example.xuyulin.myvideoproject.step9;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;
import com.example.xuyulin.myvideoproject.step8.MySurfaceView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 作者： xuyulin on 2018/6/27.
 * 邮箱： xuyulin@yixia.com
 * 描述： mp4文件播放类
 */
public class PlayMp4Activity extends AppCompatActivity implements VideoThread.VideoCallback {

    private String TAG = this.getClass().getSimpleName();
    private String sourcePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xiaokaxiu.mp4";
    private MySurfaceView surface_view;
    private Button audio_button, video_button, all_button;
    private boolean isExists;
    private AudioThread audioThread;
    private VideoThread videoThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_mp4);
        surface_view = findViewById(R.id.surface_view);
        audio_button = findViewById(R.id.audio_button);
        video_button = findViewById(R.id.video_button);
        all_button = findViewById(R.id.all_button);
        isExists = judeFileExists(new File(sourcePath));
        initThread();
    }

    public void audioPlay(View view) {
        if (!isExists) {
            Toast.makeText(this, "资源不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("音频播放".equals(audio_button.getText())) {
            Toast.makeText(this, "音频播放", Toast.LENGTH_SHORT).show();
            audio_button.setText("音频暂停");
            audioThread.threadStart();
        } else {
            Toast.makeText(this, "音频暂停", Toast.LENGTH_SHORT).show();
            audio_button.setText("音频播放");
            audioThread.threadPause();
        }
    }

    public void videoPlay(View view) {
        if (!isExists) {
            Toast.makeText(this, "资源不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("视频播放".equals(video_button.getText())) {
            Toast.makeText(this, "视频播放", Toast.LENGTH_SHORT).show();
            video_button.setText("视频暂停");
            videoThread.threadStart();
        } else {
            Toast.makeText(this, "视频暂停", Toast.LENGTH_SHORT).show();
            video_button.setText("视频播放");
            videoThread.threadPause();
        }
    }

    public void mp4Play(View view) {
        if (!isExists) {
            Toast.makeText(this, "资源不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("正常播放".equals(all_button.getText())) {
            Toast.makeText(this, "正常播放", Toast.LENGTH_SHORT).show();
            all_button.setText("正常暂停");
            audioThread.threadStart();
            videoThread.threadStart();
        } else if ("重新播放".equals(all_button.getText())) {
            Toast.makeText(this, "正常播放", Toast.LENGTH_SHORT).show();
            initThread();
            all_button.setText("正常暂停");
            audioThread.threadStart();
            videoThread.threadStart();
        } else {
            Toast.makeText(this, "正常暂停", Toast.LENGTH_SHORT).show();
            all_button.setText("正常播放");
            audioThread.threadPause();
            videoThread.threadPause();
        }
    }

    public void initThread() {
        sourcePath = initData();
        audioThread = new AudioThread(sourcePath);
        videoThread = new VideoThread(sourcePath, surface_view.getHolder().getSurface(), this);
    }

    // 判断文件是否存在
    public boolean judeFileExists(File file) {

        if (file.exists()) {
            Log.e("xyl", "file exists");
            return true;
        } else {
            Log.e("xyl", "file not exists, create it ...");
            return false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioThread.threadStop();
        videoThread.threadStop();
    }

    @Override
    public void onVideoScreenSize(final int width, final int height, float time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                surface_view.setRatio((float) width / height);
            }
        });
    }

    @Override
    public void onTextBack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                all_button.setText("重新播放");
            }
        });
    }

    //将raw里video拷贝到文件
    private String initData() {
        File dir = getFilesDir();
        File path = new File(dir, "shape.mp4");
        //R.raw.shape_of_my_heart
        final BufferedInputStream in = new BufferedInputStream(getResources().openRawResource(0));
        final BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(openFileOutput(path.getName(), Context.MODE_PRIVATE));
            byte[] buf = new byte[1024];
            int size = in.read(buf);
            while (size > 0) {
                out.write(buf, 0, size);
                size = in.read(buf);
            }
            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path.toString();
    }

}
