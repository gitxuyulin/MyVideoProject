package com.example.xuyulin.myvideoproject.step2;

import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xuyulin.myvideoproject.R;

import java.io.File;

/**
 * 作者： xuyulin on 2018/5/28.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音频录制播放编解码的类
 */

public class AudioActivity extends AppCompatActivity implements MyAudioRecord.AudioTranscribeListener {

    public static final String audioPath = Environment.getExternalStorageDirectory() + "/daimeng.wav";
    private TextView audio_text;
    private MyAudioRecord audioTranscribe;
    private WAVFileWrite wavFileWrite;
    private MyMediaCodec myMediaCodec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        audio_text = findViewById(R.id.audio_text);
        if (fileIsExists(audioPath)) {
            audio_text.setText("daimeng.wav");
        } else {
            audio_text.setText("还没有音频文件");
        }
    }

    public void starttest(View view) {
        audioTranscribe = new MyAudioRecord();
        wavFileWrite = new WAVFileWrite();
        wavFileWrite.openFile(44100, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_IN_MONO);
        audioTranscribe.setListener(this);
        audioTranscribe.startTranscribe();
        Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();
    }

    public void stoptest(View view) {
        audioTranscribe.stopTranscribe();
        wavFileWrite.closeFile();
        if (fileIsExists(WAVFileWrite.filePath)) {
            audio_text.setText("daimeng.wav");
        } else {
            audio_text.setText("还没有音频文件");
        }
        Toast.makeText(this, "停止录制", Toast.LENGTH_SHORT).show();
    }

    //判断wav文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void playaudio(View view) {
        if ("daimeng.wav".equals(audio_text.getText())) {
            MyAudioTrack audioTrack = new MyAudioTrack();
            audioTrack.audioTrackPlay();
            Toast.makeText(this, "播放音频", Toast.LENGTH_SHORT).show();
        }
    }

    public void startmediacodec(View view) {
        Toast.makeText(this, "音频编码", Toast.LENGTH_SHORT).show();
        myMediaCodec = new MyMediaCodec();
        myMediaCodec.startMediaCodec();
    }

    public void stopmediacodec(View view) {
        myMediaCodec.stopMediaCodec();
    }

    @Override
    public void audioByte(byte[] audioByte) {
        wavFileWrite.writeWVA(audioByte);
    }
}
