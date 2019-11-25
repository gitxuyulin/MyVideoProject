package com.example.xuyulin.myvideoproject.step4;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.xuyulin.myvideoproject.R;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 作者： xuyulin on 2018/6/5.
 * 邮箱： xuyulin@yixia.com
 * 描述： 使用 MediaExtractor 和 MediaMuxerRunnable API 解析和封装 mp4 文件的类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoMediaActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();
    private String myPath = Environment.getExternalStorageDirectory().getPath();
    private String videoPath = Environment.getExternalStorageDirectory().getPath() + "/yixia.mp4";
    private String audioPath = Environment.getExternalStorageDirectory().getPath() + "/ouput.aac";
    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;
    private final static int ALLOCATE_BUFFER = 500 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_media);
        //音视频合并
        mediaMerge();
        //音视频分离
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    startVideo();
//                    startAudio();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public boolean startVideo() throws IOException {
        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(videoPath);
        int mVideoTrackIndex = -1;
        int framerate = 0;
        //获取track的个数，一个视频氛围音频track和视频track，所以这里大小为2，对音频track不处理，直接continue,所以封装成的视频是没有声音的
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (!mime.startsWith("video/")) {
                continue;
            }
            framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            mediaExtractor.selectTrack(i);
            mediaMuxer = new MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVideoTrackIndex = mediaMuxer.addTrack(format);
            mediaMuxer.start();
        }

        if (mediaMuxer == null) {
            return false;
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        info.presentationTimeUs = 0;
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
        int sampleSize = 0;
        while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {

            info.offset = 0;
            info.size = sampleSize;
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            info.presentationTimeUs += 1000 * 1000 / framerate;
            mediaMuxer.writeSampleData(mVideoTrackIndex, buffer, info);
            mediaExtractor.advance();
        }

        mediaExtractor.release();

        mediaMuxer.stop();
        mediaMuxer.release();

        return true;
    }

    public boolean startAudio() throws IOException {
        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(videoPath + "/yixia.mp4");
        int mAudioTrackIndex = -1;
        long sampleTime = 0;
        int framerate = 0;
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (!mime.startsWith("audio/")) {
                continue;
            }
            framerate = i;
            mediaExtractor.selectTrack(i);
            mediaMuxer = new MediaMuxer(audioPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mAudioTrackIndex = mediaMuxer.addTrack(mediaFormat);
            mediaMuxer.start();
        }

        if (mediaMuxer == null) {
            return false;
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        info.presentationTimeUs = 0;
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
        sampleTime = getSampleTime(mediaExtractor, buffer, framerate);
        int sampleSize = 0;
        while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {

            info.offset = 0;
            info.size = sampleSize;
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            info.presentationTimeUs += sampleTime;
            mediaMuxer.writeSampleData(mAudioTrackIndex, buffer, info);
            mediaExtractor.advance();
        }

        mediaExtractor.release();

        mediaMuxer.stop();
        mediaMuxer.release();

        return true;
    }

    public void mediaMerge() {
        String mergeVideoPath = Environment.getExternalStorageDirectory().getPath() + "/ouput.mp4";
        String mergeAudioPath = Environment.getExternalStorageDirectory().getPath() + "/ouput.aac";
        MediaExtractor videoExtractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();
        MediaMuxer mediaMuxer = null;
        try {
            videoExtractor.setDataSource(mergeVideoPath);
            audioExtractor.setDataSource(mergeAudioPath);
            mediaMuxer = new MediaMuxer(myPath + "/yizhibo.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int videoTrack = getTrack(videoExtractor, true);
            int audioTrack = getTrack(audioExtractor, false);
            videoExtractor.selectTrack(videoTrack);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(videoTrack);
            audioExtractor.selectTrack(audioTrack);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(audioTrack);
            int writeVideoIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();
            writeSampleData(videoExtractor, mediaMuxer, writeVideoIndex, videoTrack);
            writeSampleData(audioExtractor, mediaMuxer, writeAudioIndex, audioTrack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeSampleData(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer,
                                    int writeTrackIndex, int audioTrack) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(ALLOCATE_BUFFER);

            // 读取写入帧数据
            long sampleTime = getSampleTime(mediaExtractor, byteBuffer, audioTrack);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (true) {
                //读取帧之间的数据
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }

                mediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.presentationTimeUs += sampleTime;
                //写入帧的数据
                mediaMuxer.writeSampleData(writeTrackIndex, byteBuffer, bufferInfo);
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "writeSampleData ex", e);
        }

        return false;
    }

    private int getTrack(MediaExtractor mediaExtractor, boolean isMedia) {
        if (mediaExtractor == null) {
            Log.w(TAG, "mediaExtractor mediaExtractor is null");
            return 0;
        }
        String type = isMedia ? "video/" : "audio/";
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
            // video or audio track
            if (mineType.startsWith(type)) {
                return i;
            }
        }

        return 0;
    }

    private long getSampleTime(MediaExtractor mediaExtractor, ByteBuffer byteBuffer, int videoTrack) {
        if (mediaExtractor == null) {
            Log.w(TAG, "getSampleTime mediaExtractor is null");
            return 0;
        }
        mediaExtractor.readSampleData(byteBuffer, 0);
        //skip first I frame
        if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer, 0);

        // get first and second and count sample time
        long firstVideoPTS = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer, 0);
        long SecondVideoPTS = mediaExtractor.getSampleTime();
        long sampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
        Log.d(TAG, "getSampleTime is " + sampleTime);

        // 重新切换此信道，不然上面跳过了3帧,造成前面的帧数模糊
        mediaExtractor.unselectTrack(videoTrack);
        mediaExtractor.selectTrack(videoTrack);

        return sampleTime;
    }

}
