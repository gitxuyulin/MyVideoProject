package com.example.xuyulin.myvideoproject.step7;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/14.
 * 邮箱： xuyulin@yixia.com
 * 描述： 视频编码器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaCodecEncoder {

    private String outVideoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videoEncoder.h264";
    private int width = 1280;
    private int height = 720;
    private int framerate = 30;
    private String mime = "video/avc";
    private long pts = 0;
    private int generateIndex = 0;
    private MediaCodec mediaCodec;
    private BufferedOutputStream outputStream;
    private byte[] configByte;

    public void initEncoder() {
        if (mediaCodec != null) {
            mediaCodec = null;
        }
        creatFile();
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType(mime);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeEncoder() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
            closeFile();
        }
    }

    public void encoder(byte[] data, int length) throws IOException {
        byte[] yuv420sp = new byte[width * height * 3 / 2];
        NV21ToNV12(data, yuv420sp, width, height);
        data = yuv420sp;
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        //设置一个等待时间，-1代表无限等待
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            pts = computePresentationTime(generateIndex);
            generateIndex++;
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, pts, 0);
        }

        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 12000);
        if (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            byte[] outbyte = new byte[bufferInfo.size];
            outputBuffer.get(outbyte);
            if (bufferInfo.flags == 2) {
                configByte = new byte[bufferInfo.size];
                configByte = outbyte;
            } else if (bufferInfo.flags == 1) {
                byte[] keyByte = new byte[bufferInfo.size + configByte.length];
                System.arraycopy(configByte, 0, keyByte, 0, configByte.length);
                System.arraycopy(outbyte, 0, keyByte, configByte.length, outbyte.length);
                outputStream.write(keyByte, 0, keyByte.length);
            } else {
                outputStream.write(outbyte, 0, outbyte.length);
            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);

        }
    }

    public void creatFile() {
        File file = new File(outVideoPath);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / framerate;
    }

}
