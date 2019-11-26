package com.example.xuyulin.myvideoproject.step2;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/4.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音频编码的类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MyAudioEncoder {

    private String TAG = this.getClass().getSimpleName();
    private String MIME_ENCODER_TYPE = "audio/mp4a-latm";
    private int CHANNEL_NUM = 1;
    private int SAMPLE_RATE = 44100;
    private int KEY_BIT_RATE = 96000;
    private int MAX_INPUT_SIZE = 16384;//16bit
    private MediaCodec mediaEncoder;
    private AudioEncoderListener listener;
    private boolean isOpen = false;

    public void initEncoder() {
        if (mediaEncoder != null) {
            mediaEncoder = null;
        }
        isOpen = true;
        try {
            //mime：即MediaCodec.createEncoderByType()的参数,用来表示媒体文件的格式 mp3为audio/mpeg；aac为audio/mp4a-latm；mp4为video/mp4v-es
            // audio前缀为音频，video前缀为视频 我们可用此区别区分媒体文件内的音频轨道和视频轨道
            //mime的各种类型定义在MediaFormat静态常量中
            mediaEncoder = MediaCodec.createEncoderByType(MIME_ENCODER_TYPE);//初始化音频AAC编码器
            MediaFormat format = MediaFormat.createAudioFormat(MIME_ENCODER_TYPE, SAMPLE_RATE, CHANNEL_NUM);//第一个参数是类型，第二个是采样率，第三个是通道
            format.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE);//比特率，如果太低会造成类似马赛克的现象
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//这里为AAC，所以mime为audio/mp4a-latm
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);//作用于inputBuffer的大小
            mediaEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaEncoder == null) {
            return;
        }
        mediaEncoder.start();
        Log.e(TAG, "MyAudioEncoder is init");
    }

    public void closeEncoder() {
        if (mediaEncoder != null) {
            isOpen = false;
            mediaEncoder.stop();
            mediaEncoder.release();
            Log.e(TAG, "MyAudioEncoder close");
        }
    }

    interface AudioEncoderListener {
        void onEncodeInvok(byte[] bytes, long presentationTimeUs);
    }

    public void setListener(AudioEncoderListener listener) {
        this.listener = listener;
    }

    //这里不能用synchronized修饰，因为这个方法很费时间，如果同步的话会造成音频录制存储特别的慢而播放不出来
    //而且这里没有必要利用同步，因为线程调用retrieve方法基本不浪费时间的，所以存储的音频都会即使被取走，不会造成溢出
    public void encode(byte[] audioByte, long presentationTimeUs) {
        if (!isOpen) {
            return;
        }
        try {
            ByteBuffer[] byteInputBuffers = mediaEncoder.getInputBuffers();
            //如果存在可用的缓冲区，此方法会返回其位置索引，否则返回-1，参数为超时时间，
            // 单位是毫秒，如果此参数是0，则立即返回，如果参数小于0，则无限等待直到有可使用的缓冲区，如果参数大于0，则等待时间为传入的毫秒值
            int dequeueInputIndex = mediaEncoder.dequeueInputBuffer(1000);
            if (dequeueInputIndex >= 0) {
                ByteBuffer byteBuffer = byteInputBuffers[dequeueInputIndex];
                byteBuffer.clear();
                byteBuffer.put(audioByte);
                mediaEncoder.queueInputBuffer(dequeueInputIndex, 0, audioByte.length, presentationTimeUs, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        Log.e(TAG, "MyAudioEncoder is encode");
    }

    public synchronized void retrieve() {
        if (!isOpen) {
            return;
        }
        ByteBuffer[] byteOutputBuffers = mediaEncoder.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int dequeueOutputIndex = mediaEncoder.dequeueOutputBuffer(bufferInfo, 1000);
        if (dequeueOutputIndex >= 0) {
            ByteBuffer byteBuffer = byteOutputBuffers[dequeueOutputIndex];
            byteBuffer.position(bufferInfo.offset);
            byteBuffer.limit(bufferInfo.offset + bufferInfo.size);
//            byte[] bytes = new byte[bufferInfo.size + 7];
//            addADTStoPacket(bytes, bufferInfo.size + 7);
//            byteBuffer.get(bytes, 7, bufferInfo.size);
            byte[] bytes = new byte[bufferInfo.size];
            byteBuffer.get(bytes, 0, bufferInfo.size);
            listener.onEncodeInvok(bytes, bufferInfo.presentationTimeUs);
            //如果需要输出成文件在这里操作文件的写操作保存成.aac文件即可
            mediaEncoder.releaseOutputBuffer(dequeueOutputIndex, false);
        }
        Log.e(TAG, "MyAudioEncoder is retrieve");
    }

    public void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 16KHz    39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int chanCfg = 1; // CPE
// fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

}
