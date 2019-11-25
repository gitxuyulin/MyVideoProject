package com.example.xuyulin.myvideoproject.step9;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/29.
 * 邮箱： xuyulin@yixia.com
 * 描述： 视频解码播放线程
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VideoThread extends Thread {

    private String TAG = "xyl";
    private String sourcePath;
    private Surface surface;
    private boolean isStart = false;
    private boolean isPause = false;
    private MediaExtractor videoExtractor;
    private MediaCodec videoDecoder;
    private VideoCallback videoCallback;
    private long timeUS = 1000;
    private long pauseTime = 0;

    public VideoThread(String sourcePath, Surface surface, VideoCallback videoCallback) {
        this.sourcePath = sourcePath;
        this.surface = surface;
        this.videoCallback = videoCallback;
    }

    public void threadStart() {
        if (!isPause) {
            isStart = true;
            this.start();
        } else {
            pauseTime -= System.currentTimeMillis();
            isPause = false;
        }
    }

    public void threadPause() {
        pauseTime = System.currentTimeMillis();
        isPause = true;

    }

    public void threadStop() {
        isStart = false;
        videoDecoder.stop();
        videoDecoder.release();
        videoExtractor.release();
        try {
            this.join();
            this.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                videoExtractor.selectTrack(i);
                int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                float time = mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000000;
                videoCallback.onVideoScreenSize(width, height, time);
                try {
                    videoDecoder = MediaCodec.createDecoderByType(mime);
                    videoDecoder.configure(mediaFormat, surface, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        if (videoDecoder == null) return;
        videoDecoder.start();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] inputBuffers = videoDecoder.getInputBuffers();
        boolean isInputEOS = false;
        long currentTime = System.currentTimeMillis();
        while (isStart) {
            //这里是为了做暂停和继续播放使用
            if (isPause) {
                continue;
            }
            //将资源传递到解码器
            if (!isInputEOS) {
                isInputEOS = mediaEOS(inputBuffers);
            }

            int outputBufferIndex = videoDecoder.dequeueOutputBuffer(bufferInfo, timeUS);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.v(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.v(TAG, "超时");
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.v(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    break;
                default:
                    //延迟渲染
                    sleepRender(bufferInfo, currentTime);
                    videoDecoder.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.v(TAG, "buffer stream end");
                break;
            }
        }
        videoCallback.onTextBack();
        threadStop();
    }

    public boolean mediaEOS(ByteBuffer[] myInputBuffers) {
        boolean isEnd = false;
        int inputBufferIndex = videoDecoder.dequeueInputBuffer(timeUS);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = myInputBuffers[inputBufferIndex];
            int sampleSize = videoExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                videoDecoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEnd = true;
                Log.v(TAG, "eos");
            } else {
                videoDecoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, videoExtractor.getSampleTime(), 0);
                videoExtractor.advance();//读取下一帧数据
            }
        }
        return isEnd;
    }

    public void sleepRender(MediaCodec.BufferInfo bufferInfo, long currentTime) {
        while (bufferInfo.presentationTimeUs / 1000 > (System.currentTimeMillis() - currentTime + pauseTime)) {
            try {
                this.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public interface VideoCallback {
        void onVideoScreenSize(int width, int height, float time);

        void onTextBack();
    }

}
