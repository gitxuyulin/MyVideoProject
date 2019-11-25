package com.example.xuyulin.myvideoproject.step2;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * 作者： xuyulin on 2018/5/31.
 * 邮箱： xuyulin@yixia.com
 * 描述： 录制音频器
 */
public class MyAudioRecord {

    private String TAG = this.getClass().getSimpleName();
    private boolean startOrStop = false;
    private AudioRecord audioRecord;
    private AudioThread audioThread;
    private AudioTranscribeListener listener;

    public void startTranscribe() {
        startOrStop = true;
        /**
         * AudioRecord
         * @param audioSource 该参数指的是音频采集的输入源，可选的值以常量的形式定义在 MediaRecorder.AudioSource 类中
         * @param sampleRateinHz 采样率，注意，目前44100Hz是唯一可以保证兼容所有Android手机的采样率
         * @param channelConfig 通道数的配置，可选的值以常量的形式定义在 AudioFormat 类中
         * @param audioFormat 这个参数是用来配置“数据位宽”的，可选的值也是以常量的形式定义在 AudioFormat 类中，ENCODING_PCM_16BIT兼容所有安卓手机
         * @param bufferSizeInBytes 它配置的是 AudioRecord 内部的音频缓冲区的大小,音频缓冲区的大小则必须是一帧大小的2～N倍
         */
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes * 4);
        audioRecord.startRecording();
        audioThread = new AudioThread();
        audioThread.start();
    }

    public void stopTranscribe() {
        startOrStop = false;
        try {
            audioThread.interrupt();
            audioThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
        audioRecord.release();
    }

    public interface AudioTranscribeListener {
        void audioByte(byte[] audioByte);
    }

    public void setListener(AudioTranscribeListener listener) {
        this.listener = listener;
    }

    public class AudioThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (startOrStop) {
                if (audioRecord == null) {
                    Log.e(TAG, "audioRecord is null");
                    return;
                }
                /**
                 * @param audioData 目标字节数组，即读取内容写入的数组
                 * @param offsetInBytes 在数组b在其中写入数据的起始位置的偏移
                 * @param sizeInBytes 要读取的字节数
                 * */
                byte[] buffer = new byte[1024 * 2];
                int ret = audioRecord.read(buffer, 0, buffer.length);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "ERROR_BAD_VALUE");
                } else {
                    //将采集的音频输出为wav文件
                    listener.audioByte(buffer);
                }
            }
        }
    }
}
