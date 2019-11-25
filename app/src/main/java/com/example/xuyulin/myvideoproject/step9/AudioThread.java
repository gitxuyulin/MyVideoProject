package com.example.xuyulin.myvideoproject.step9;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/27.
 * 邮箱： xuyulin@yixia.com
 * 描述： 解码音频的线程
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AudioThread extends Thread {

    private String TAG = "xyl";
    private String sourcePath;
    private AudioTrack audioTrack;
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaDecoder;
    private int bufferSizeInBytes = 0;
    private int timeoutUs = 1000;
    private boolean isStart = false;
    private boolean isPause = false;

    public AudioThread(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void threadStart() {
        if (!isPause) {
            isStart = true;
            this.start();
        } else {
            isPause = false;
        }
    }

    public void threadPause() {
        isPause = true;
    }

    public void threadStop() {
        isStart = false;
        mediaDecoder.stop();
        mediaDecoder.release();
        mediaExtractor.release();
        audioTrack.stop();
        audioTrack.release();
        try {
            this.join();
            this.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mediaExtractor.selectTrack(i);
                int sampleRateInHz = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                int channelConfig = (channelCount == 1) ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
                int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
                int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                bufferSizeInBytes = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
                int frameSizeInByte = channelCount * 2;
                bufferSizeInBytes = (bufferSizeInBytes / frameSizeInByte) * frameSizeInByte;
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
                audioTrack.play();
                try {
                    mediaDecoder = MediaCodec.createDecoderByType(mime);
                    mediaDecoder.configure(mediaFormat, null, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        if (mediaDecoder == null) {
            return;
        }
        mediaDecoder.start();

        /**
         * 1. position:当前读或者写的位置
         * 2. mark:标记上一次mark的位置，方便reset将postion置为mark的值。
         * 3. limit:标记数据的最大有效的位置
         * 4. capacity:标记buffer的最大可存储值
         * */
        ByteBuffer[] buffers = mediaDecoder.getOutputBuffers();
        int byteSize = buffers[0].capacity();
        if (byteSize <= 0) {
            byteSize = bufferSizeInBytes;
        }
        byte[] outputByte = new byte[byteSize];

        ByteBuffer[] inputBuffers = mediaDecoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaDecoder.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        long currentTime = System.currentTimeMillis();
        boolean isInputEnd = false;

        while (isStart) {
            Log.v(TAG, "解码进行中");
            //这里是为了做暂停和继续播放使用
            if (isPause) {
                continue;
            }

            //资源装入解码器
            if (!isInputEnd) {
                isInputEnd = inputEnd(inputBuffers, mediaDecoder, mediaExtractor);
            }

            int outputBufferIndex = mediaDecoder.dequeueOutputBuffer(bufferInfo, timeoutUs);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.v(TAG, "format change");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.v(TAG, "超时");
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    outputBuffers = mediaDecoder.getOutputBuffers();
                    Log.v(TAG, "output buffers changed");
                    break;
                default:
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    //延时操作，如果缓冲区的时间大于当前播放的时间就休眠一下
                    delayedRender(bufferInfo, currentTime);
                    if (bufferInfo.size > 0) {
                        if (bufferInfo.size > byteSize) {
                            outputByte = new byte[bufferInfo.size];
                        }
                        outputBuffer.position(0);
                        outputBuffer.get(outputByte, 0, bufferInfo.size);
                        outputBuffer.clear();
                        if (audioTrack != null) {
                            audioTrack.write(outputByte, 0, bufferInfo.size);
                        }
                    }
                    mediaDecoder.releaseOutputBuffer(outputBufferIndex, false);
                    break;
            }
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.v(TAG, "buffer stream end");
                break;
            }

        }

    }

    public boolean inputEnd(ByteBuffer[] inputBuffers, MediaCodec mediaCodec, MediaExtractor extractor) {
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(timeoutUs);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = extractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                return true;
            } else {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                extractor.advance();//读取下一帧数据
            }
        }
        return false;
    }

    public void delayedRender(MediaCodec.BufferInfo mBufferInfo, long currentTime) {
        while (mBufferInfo.presentationTimeUs / 1000 > (System.currentTimeMillis() - currentTime)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
