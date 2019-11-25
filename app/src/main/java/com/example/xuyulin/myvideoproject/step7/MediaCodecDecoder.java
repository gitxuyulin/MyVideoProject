package com.example.xuyulin.myvideoproject.step7;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/14.
 * 邮箱： xuyulin@yixia.com
 * 描述： 视频解码器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaCodecDecoder {

    private String videoDecoderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videoEncoder.h264";
    private BufferedInputStream inputStream;
    private Context mContext;
    private String mime = "video/avc";
    private int width = 1280;
    private int height = 720;
    private int framerate = 30;
    private MediaFormat mediaFormat;
    private MediaCodec mediaDecoder;
    private Thread decoderThread;
    private boolean mStopFlag = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(mContext, "播放结束!", Toast.LENGTH_LONG).show();
        }
    };

    public void initCodecDecoder(Context context, SurfaceHolder holder) {
        this.mContext = context;
        if (mediaDecoder != null) {
            mediaDecoder = null;
        }
        initFile();
        mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        try {
            mediaDecoder = MediaCodec.createDecoderByType(mime);
            mediaDecoder.configure(mediaFormat, holder.getSurface(), null, 0);
            mediaDecoder.start();
            decoder();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeCodecDecoder() {
        if (mediaDecoder != null) {
            mediaDecoder.stop();
            mediaDecoder.release();
            mediaDecoder = null;
            try {
                decoderThread.interrupt();
                decoderThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void decoder() {
        decoderThread = new Thread(new DecoderThread());
        decoderThread.start();

    }

    public class DecoderThread implements Runnable {
        @Override
        public void run() {
            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
            byte[] marker0 = new byte[]{0, 0, 0, 1};
            long timeoutUs = 10000;
            long startMs = System.currentTimeMillis();
            byte[] streamBuffer = null;
            ByteBuffer[] inputByteBuffers = mediaDecoder.getInputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            try {
                streamBuffer = getByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int byte_cnt = 0;
            while (!mStopFlag) {
                byte_cnt = streamBuffer.length;
                if (byte_cnt == 0) {
                    streamBuffer = dummyFrame;
                }

                int startIndex = 0;
                int remaining = byte_cnt;
                while (true) {
                    if (remaining == 0 || startIndex >= remaining) {
                        break;
                    }
                    int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);
                    if (nextFrameStart == -1) {
                        nextFrameStart = remaining;
                    } else {
                    }

                    int inputBufferIndex = mediaDecoder.dequeueInputBuffer(timeoutUs);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = inputByteBuffers[inputBufferIndex];
                        inputBuffer.clear();
                        inputBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                        mediaDecoder.queueInputBuffer(inputBufferIndex, 0, nextFrameStart - startIndex, 0, 0);
                        startIndex = nextFrameStart;
                    } else {
                        continue;
                    }


                    int outputBufferIndex = mediaDecoder.dequeueOutputBuffer(bufferInfo, timeoutUs);
                    if (outputBufferIndex >= 0) {
                        while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        boolean doRender = (bufferInfo.size != 0);
                        mediaDecoder.releaseOutputBuffer(outputBufferIndex, doRender);

                    }

                }
                mStopFlag = true;
                mHandler.sendEmptyMessage(0);
            }

        }
    }

    public void initFile() {
        File f = new File(videoDecoderPath);
        if (f == null | !f.exists() | f.length() == 0) {
            Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            inputStream = new BufferedInputStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public byte[] getByte() throws IOException {
        int size = 1024;
        int len;
        byte[] bytes;
        if ((InputStream) inputStream instanceof ByteArrayInputStream) {
            size = inputStream.available();
            bytes = new byte[size];
            len = inputStream.read(bytes, 0, size);
        } else {
            bytes = new byte[size];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(bytes, 0, size)) != -1) {
                bos.write(bytes, 0, len);
            }
            bytes = bos.toByteArray();
        }
        return bytes;
    }

    private int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);

        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }
        return -1;  // Not found
    }

    private int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        return lsp;
    }

}
