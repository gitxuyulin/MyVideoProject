package com.example.xuyulin.myvideoproject.step8;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * 作者： xuyulin on 2018/6/20.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音视频混合器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaMuxerRunnable extends Thread {

    private String TAG = this.getClass().getSimpleName();
    public static final int TRACK_VIDEO = 0;
    public static final int TRACK_AUDIO = 1;
    private static MediaMuxerRunnable mediaMuxerRunnable;
    private MediaMuxer mediaMuxer;
    private String filePath = Environment.getExternalStorageDirectory() + "/yushulinfeng.mp4";
    private boolean isAudioStart = false;
    private boolean isVideoStart = false;
    private Vector<MuxerData> muxerDatas;
    private boolean isMuxerStart = true;
    private int audioTrackIndex;
    private int videoTrackIndex;
    private MediaAudioRunnable audioRunnable;
    private MediaVideoRunnable videoRunnable;
    private Object lock = new Object();

    public static MediaMuxerRunnable startMediaMuxer() {
        if (mediaMuxerRunnable == null) {
            synchronized (MediaMuxerRunnable.class) {
                if (mediaMuxerRunnable == null) {
                    mediaMuxerRunnable = new MediaMuxerRunnable();
                    mediaMuxerRunnable.start();
                }
            }
        }
        return mediaMuxerRunnable;
    }

    public void stopMediaMuxer() {
        if (mediaMuxerRunnable != null) {
            isMuxerStart = false;
            isAudioStart = false;
            isVideoStart = false;
            try {
                audioRunnable.stopMediaAudio();
                videoRunnable.stopVideoRunnable();
                mediaMuxerRunnable.interrupt();
                mediaMuxerRunnable.join(1000);
                mediaMuxerRunnable = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    //将视频数据添加到MediaoVideoRunnable来中
    public void addVideoByte(byte[] data) {
        if (videoRunnable != null) {
            videoRunnable.addData(data);
        }
    }

    public void addTrackIndex(int index, MediaFormat mediaFormat) {
        if (mediaMuxer != null && isMuxerStart) {
            int track = 0;
            track = mediaMuxer.addTrack(mediaFormat);
            if (index == TRACK_AUDIO) {
                audioTrackIndex = track;
                isAudioStart = true;
            } else if (index == TRACK_VIDEO) {
                videoTrackIndex = track;
                isVideoStart = true;
            }
            synchronized (lock) {
                if (isMuxerStart()) {
                    lock.notify();
                    mediaMuxer.start();
                }
            }
        }
    }

    public boolean isAudioStart() {
        return isAudioStart;
    }

    public boolean isVideoStart() {
        return isVideoStart;
    }

    public boolean isMuxerStart() {
        return isAudioStart && isVideoStart;
    }

    public void addMuxerData(MuxerData muxerData) {
        muxerDatas.add(muxerData);
        synchronized (lock) {
            lock.notify();
        }
    }

    public void audioAndVideo() {
        try {
            muxerDatas = new Vector<>();
            mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);///storage/emulated/0/yushulinfeng.mp4
            audioRunnable = MediaAudioRunnable.startMediaAudio(new WeakReference<MediaMuxerRunnable>(this));
            videoRunnable = MediaVideoRunnable.startVideoRunnable(new WeakReference<MediaMuxerRunnable>(this));
            if (audioRunnable != null) {
                audioRunnable.setMuxerReady(true);
            }
            if (videoRunnable != null) {
                videoRunnable.setMuxerReady(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        audioAndVideo();
        while (isMuxerStart) {
            if (isMuxerStart()) {
                if (muxerDatas.isEmpty()) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    MuxerData muxerData = muxerDatas.remove(0);
                    int track;
                    if (muxerData.trackIndex == TRACK_AUDIO) {
                        track = audioTrackIndex;
                    } else {
                        track = videoTrackIndex;
                    }
                    //混合输出mp4
                    mediaMuxer.writeSampleData(track, muxerData.byteBuffer, muxerData.bufferInfo);
                }
            } else {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static class MuxerData {
        int trackIndex;
        ByteBuffer byteBuffer;
        MediaCodec.BufferInfo bufferInfo;

        public MuxerData(int trackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            this.byteBuffer = byteBuffer;
            this.bufferInfo = bufferInfo;
        }
    }
}
