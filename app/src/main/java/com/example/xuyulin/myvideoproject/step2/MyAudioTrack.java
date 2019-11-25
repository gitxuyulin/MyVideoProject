package com.example.xuyulin.myvideoproject.step2;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * 作者： xuyulin on 2018/6/1.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音频播放的类
 */
public class MyAudioTrack {

    private String TAG = this.getClass().getSimpleName();
    private AudioTrack audioTrack;
    private Thread thread;
    private WAVFileRead wavFileRead;

    public void audioTrackPlay() {
        int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return;
        }
        /**
         * 参数介绍
         * @param streamType 这个参数代表着当前应用使用的哪一种音频管理策略，当系统有多个进程需要播放音频时，这个管理策略会决定最终的展现效果，该参数的可选的值以常量的形式定义在 AudioManager 类中
         * @param sampleRateInHz 采样率，从AudioTrack源码的“audioParamCheck”函数可以看到，这个采样率的取值范围必须在 4000Hz～192000Hz 之间
         * @param channelConfig 通道数的配置,跟AudioRecord相同
         * @param audioFormat 数据位宽配置,跟AudioRecord相同
         * @param bufferSizeInBytes 配置的是 AudioTrack 内部的音频缓冲区的大小,跟AudioRecord原理相同
         * @param mode 可选的值以常量的形式定义在 AudioTrack 类中，一个是 MODE_STATIC，另一个是 MODE_STREAM,前者需要一次性将所有的数据都写入播放缓冲区，简单高效，通常用于播放铃声、系统提醒的音频片段; 后者则是按照一定的时间间隔不间断地写入音频数据，理论上它可用于任何音频播放的场景
         * */
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
//        wavFileRead = new WAVFileRead();
//        wavFileRead.openFile();
//        thread = new Thread(new AudioTrackThread());
//        thread.start();
    }

    public void audioTrackStop() {
        try {
            if (thread != null) {
                thread.interrupt();
                thread.join(1000);
                thread = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (audioTrack.getPlayState() == audioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
        audioTrack.release();
        if (wavFileRead != null)
            wavFileRead.closeFile();
    }

    public void starting(byte[] buffer) {
        audioTrack.write(buffer, 0, buffer.length);
        audioTrack.play();
    }

    public class AudioTrackThread implements Runnable {

        @Override
        public void run() {
            byte[] buffer = new byte[1024 * 2];
            while (wavFileRead.readWAV(buffer, 0, buffer.length) != -1) {
                audioTrack.write(buffer, 0, buffer.length);
                audioTrack.play();
            }
            audioTrackStop();
        }
    }

}
