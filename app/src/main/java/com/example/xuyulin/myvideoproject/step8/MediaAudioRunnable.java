package com.example.xuyulin.myvideoproject.step8;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/20.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音频采集器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaAudioRunnable extends Thread {

    private String TAG = this.getClass().getSimpleName();
    private static MediaAudioRunnable mediaAudioRunnable;
    private static boolean isStartRecord = false;
    private AudioRecord audioRecord;
    private MediaCodec mediaEncoder;
    private int TIMEOUT_USEC = 1000;
    private String mime = "audio/mp4a-latm";
    private int sampleRate = 16000;
    private int KEY_BIT_RATE = 64000;
    private int MAX_INPUT_SIZE = 16384;
    private long prevOutputPTSUs = 0;
    public int SAMPLES_PER_FRAME = 1024;
    private WeakReference<MediaMuxerRunnable> mediaMuxerRunnable;
    private Object lock = new Object();
    private boolean isMuxerReady = false;

    public MediaAudioRunnable(WeakReference<MediaMuxerRunnable> mediaMuxerRunnable) {
        this.mediaMuxerRunnable = mediaMuxerRunnable;
        initMediaRecoder();
    }

    public static MediaAudioRunnable startMediaAudio(WeakReference<MediaMuxerRunnable> mediaMuxerRunnable) {
        if (mediaAudioRunnable == null) {
            synchronized (MediaAudioRunnable.class) {
                if (mediaAudioRunnable == null) {
                    isStartRecord = true;
                    mediaAudioRunnable = new MediaAudioRunnable(mediaMuxerRunnable);
                    mediaAudioRunnable.start();
                }
            }
        }
        return mediaAudioRunnable;
    }

    public void stopMediaAudio() {
        if (mediaAudioRunnable != null) {
            isStartRecord = false;
            audioRecord.stop();
            audioRecord = null;
            mediaEncoder.stop();
            mediaEncoder.release();
            mediaEncoder = null;
            try {
                mediaAudioRunnable.interrupt();
                mediaAudioRunnable.join(1000);
                mediaAudioRunnable = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMuxerReady(boolean isMuxerReady) {
        synchronized (lock) {
            lock.notifyAll();
            this.isMuxerReady = isMuxerReady;
        }
    }

    public void initMediaRecoder() {
        //初始化AudioRecord
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 4);
        audioRecord.startRecording();
        //初始化AudioEncoder对音频编码
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(mime, sampleRate, 1);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);
        try {
            mediaEncoder = MediaCodec.createEncoderByType(mime);
            mediaEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        while (isStartRecord) {
            if (buf != null) {
                buf.clear();
                int ret = audioRecord.read(buf, SAMPLES_PER_FRAME);
                if (ret > 0) {
                    buf.position(ret);
                    buf.flip();//写模式转换读模式
                    encode(buf, ret, getPTSUs());
                }
            }
        }
    }

    public void encode(ByteBuffer bytes, int ret, long presentationTimeUs) {
        //向编码器输入数据
        ByteBuffer[] inputBuffers = mediaEncoder.getInputBuffers();
        int inputIndex = mediaEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputIndex];
            inputBuffer.clear();
            if (bytes != null) {
                inputBuffer.put(bytes);
            }
            if (ret <= 0) {
                mediaEncoder.queueInputBuffer(inputIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mediaEncoder.queueInputBuffer(inputIndex, 0, ret, presentationTimeUs, 0);
            }
        }

        //获取解码后的数据
        MediaMuxerRunnable muxerRunnable = mediaMuxerRunnable.get();
        if (muxerRunnable == null) {
            return;
        }
        ByteBuffer[] outputBuffers = mediaEncoder.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputIndex;
        do {
            outputIndex = mediaEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mediaEncoder.getOutputBuffers();
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat mediaFormat = mediaEncoder.getOutputFormat();
                muxerRunnable.addTrackIndex(MediaMuxerRunnable.TRACK_AUDIO, mediaFormat);
            } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (outputIndex < 0) {

            } else {
                ByteBuffer outputBuffer = outputBuffers[outputIndex];
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0 && muxerRunnable.isMuxerStart()) {
                    bufferInfo.presentationTimeUs = getPTSUs();
                    muxerRunnable.addMuxerData(new MediaMuxerRunnable.MuxerData(MediaMuxerRunnable.TRACK_AUDIO, outputBuffer, bufferInfo));
                    prevOutputPTSUs = bufferInfo.presentationTimeUs;
                }
                mediaEncoder.releaseOutputBuffer(outputIndex, false);
            }
        } while (outputIndex >= 0);
    }

    private long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }

}
