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
 * 描述： 音频解码器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MyAudioDecoder {

    private String TAG = this.getClass().getSimpleName();
    private MediaCodec mediaDecoder;
    private String MIME_DECODER_TYPE = "audio/mp4a-latm";
    private int CHANNEL_NUM = 1;
    private int SAMPLE_RATE = 44100;
    private int KEY_BIT_RATE = 96000;
    private int MAX_INPUT_SIZE = 16384;
    private AudioDecoderListener listener;
    private boolean mIsFirstFrame = true;

    public void initDecoder() {
        if (mediaDecoder != null) {
            mediaDecoder = null;
        }
        try {
            mediaDecoder = MediaCodec.createDecoderByType(MIME_DECODER_TYPE);
            MediaFormat format = MediaFormat.createAudioFormat(MIME_DECODER_TYPE, SAMPLE_RATE, CHANNEL_NUM);
            format.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE);//比特率，如果太低会造成类似马赛克的现象
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//这里为啊啊擦，所以mime为audio/mp4a-latm
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);//作用于inputBuffer的大小
            mediaDecoder.configure(format, null, null, 0);
            mediaDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "MyAudioDecoder is init");
    }

    public void closeDecoder() {
        if (mediaDecoder != null) {
            mediaDecoder.stop();
            mediaDecoder.release();
            Log.e(TAG, "MyAudioDecoder is close");
        }
    }

    interface AudioDecoderListener {
        void onDecoderInvok(byte[] bytes, long presentationTimeUs);
    }

    public void setListener(AudioDecoderListener listener) {
        this.listener = listener;
    }

    public void decoder(byte[] input, long presentationTimeUs) {
        try {
            ByteBuffer[] byteInputBuffers = mediaDecoder.getInputBuffers();
            int dequeueInputIndex = mediaDecoder.dequeueInputBuffer(1000);
            if (dequeueInputIndex >= 0) {
                ByteBuffer byteBuffer = byteInputBuffers[dequeueInputIndex];
                byteBuffer.clear();
                byteBuffer.put(input);
                if (mIsFirstFrame) {
                    mediaDecoder.queueInputBuffer(dequeueInputIndex, 0, input.length, presentationTimeUs, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                    mIsFirstFrame = false;
                } else {
                    mediaDecoder.queueInputBuffer(dequeueInputIndex, 0, input.length, presentationTimeUs, 0);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        Log.e(TAG, "MyAudioDecoder is decoder");
    }

    public void retrieve() {
        try {
            ByteBuffer[] byteBuffers = mediaDecoder.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int dequeueOutBufferIndex = mediaDecoder.dequeueOutputBuffer(bufferInfo, 1000);
            if (dequeueOutBufferIndex >= 0) {
                ByteBuffer byteBuffer = byteBuffers[dequeueOutBufferIndex];
                byteBuffer.position(bufferInfo.offset);
                byteBuffer.limit(bufferInfo.offset + bufferInfo.size);
                byte[] bytes = new byte[bufferInfo.size];
                byteBuffer.get(bytes);
                listener.onDecoderInvok(bytes, bufferInfo.presentationTimeUs);
                mediaDecoder.releaseOutputBuffer(dequeueOutBufferIndex, false);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        Log.e(TAG, "MyAudioDecoder is retrieve");
    }

}
