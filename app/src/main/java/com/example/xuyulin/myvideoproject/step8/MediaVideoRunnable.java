package com.example.xuyulin.myvideoproject.step8;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * 作者： xuyulin on 2018/6/20.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音频采集器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaVideoRunnable extends Thread {

    private String TAG = this.getClass().getSimpleName();
    private String mime = "video/avc";
    private int width = 1920;
    private int height = 1080;
    private int FRAME_RATE = 25;
    private int COMPRESS_RATIO = 256;
    private int IFRAME_INTERVAL = 10;
    private int BIT_RATE = width * height * 3 * 8 * FRAME_RATE / COMPRESS_RATIO;
    private int TIMEOUT_USEC = 10000;
    private int COLOR_FORMAT;
    private MediaCodecInfo codecInfo;
    private static MediaVideoRunnable mediaVideoRunnable;
    private static boolean isEncoderExit = false;
    private static boolean isEncoderStart = false;
    private WeakReference<MediaMuxerRunnable> mediaMuxerRunnable;
    private MediaCodec mediaCodecEncoder;
    private Vector<byte[]> videoData;
    private Object lock = new Object();
    private byte[] mFrameByte;
    private MediaCodec.BufferInfo bufferInfo;
    private boolean isMuxerReady = false;
    private MediaFormat mediaFormat;

    public MediaVideoRunnable(WeakReference<MediaMuxerRunnable> mediaMuxerRunnable) {
        this.mediaMuxerRunnable = mediaMuxerRunnable;
        initMediaCodec();
    }

    public static MediaVideoRunnable startVideoRunnable(WeakReference<MediaMuxerRunnable> mediaMuxerRunnable) {
        if (mediaVideoRunnable == null) {
            synchronized (MediaVideoRunnable.class) {
                if (mediaVideoRunnable == null) {
                    isEncoderExit = true;
                    mediaVideoRunnable = new MediaVideoRunnable(mediaMuxerRunnable);
                    mediaVideoRunnable.start();
                }
            }
        }
        return mediaVideoRunnable;
    }

    public void stopVideoRunnable() {
        if (mediaVideoRunnable != null) {
            try {
                mediaVideoRunnable.interrupt();
                mediaVideoRunnable.join(1000);
                mediaVideoRunnable = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isEncoderExit = false;
        }
    }

    public void stopVideo() {
        videoData.clear();
        mediaCodecEncoder.stop();
        mediaCodecEncoder.release();
        isEncoderStart = false;
    }

    public void setMuxerReady(boolean isMuxerReady) {
        synchronized (lock) {
            lock.notifyAll();
            this.isMuxerReady = isMuxerReady;
        }
    }

    public void initMediaCodec() {
        isEncoderStart = true;
        mFrameByte = new byte[this.width * this.height * 3 / 2];
        bufferInfo = new MediaCodec.BufferInfo();
        videoData = new Vector<>();
        codecInfo = selectCodec(mime);
        if (codecInfo == null) {
            return;
        }
        COLOR_FORMAT = selectColorFormat(codecInfo, mime);
        mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, COLOR_FORMAT);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        startMediaCodec();
    }

    public void startMediaCodec() {
        try {
            mediaCodecEncoder = MediaCodec.createByCodecName(codecInfo.getName());
            mediaCodecEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodecEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private int selectColorFormat(MediaCodecInfo codecInfo,
                                  String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo
                .getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        return 0; // not reached
    }

    private boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    public void addData(byte[] data) {
        if (videoData != null && isMuxerReady) {
            videoData.add(data);
        }
    }

    @Override
    public void run() {
        while (isEncoderExit) {
            if (!isEncoderStart) {
                stopVideo();
                if (isMuxerReady) {
                    startMediaCodec();
                }
            } else if (!videoData.isEmpty()) {
                byte[] videoByte = videoData.remove(0);
                encoder(videoByte);
            }
        }
    }

    public void encoder(byte[] input) {
        //向编码器输入数据
        NV21toI420SemiPlanar(input, mFrameByte, this.width, this.height);

        ByteBuffer[] inputBuffers = mediaCodecEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodecEncoder.getOutputBuffers();
        int inputIndex = mediaCodecEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputIndex];
            inputBuffer.clear();
            inputBuffer.put(mFrameByte);
            mediaCodecEncoder.queueInputBuffer(inputIndex, 0, mFrameByte.length, System.nanoTime() / 1000, 0);
        }

        //获取编码后的数据
        int outputIndex = mediaCodecEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        do {
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mediaCodecEncoder.getOutputBuffers();
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat mediaFormat = mediaCodecEncoder.getOutputFormat();
                MediaMuxerRunnable muxerRunnable = mediaMuxerRunnable.get();
                if (muxerRunnable != null) {
                    muxerRunnable.addTrackIndex(MediaMuxerRunnable.TRACK_VIDEO, mediaFormat);
                }
            } else if (outputIndex < 0) {
            } else {
                ByteBuffer outputBuffer = outputBuffers[outputIndex];
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    MediaMuxerRunnable muxerRunnable = mediaMuxerRunnable.get();
                    if (muxerRunnable != null && !muxerRunnable.isVideoStart()) {
                        MediaFormat newMediaFormat = mediaCodecEncoder.getOutputFormat();
                        muxerRunnable.addTrackIndex(MediaMuxerRunnable.TRACK_VIDEO, newMediaFormat);
                    }
                    outputBuffer.position(bufferInfo.offset);//指定了下一个将要被写入或者读取的元素索引
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);//指定还有多少数据需要取出
                    if (muxerRunnable != null && muxerRunnable.isMuxerStart()) {
                        muxerRunnable.addMuxerData(new MediaMuxerRunnable.MuxerData(MediaMuxerRunnable.TRACK_VIDEO, outputBuffer, bufferInfo));
                    }
                }
                mediaCodecEncoder.releaseOutputBuffer(outputIndex, false);
            }
            outputIndex = mediaCodecEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        } while (outputIndex >= 0);
    }

    private static void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes,
                                             int width, int height) {
        System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
        for (int i = width * height; i < nv21bytes.length; i += 2) {
            i420bytes[i] = nv21bytes[i + 1];
            i420bytes[i + 1] = nv21bytes[i];
        }
    }

}
