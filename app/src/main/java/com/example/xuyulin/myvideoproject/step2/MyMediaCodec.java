package com.example.xuyulin.myvideoproject.step2;

/**
 * 作者： xuyulin on 2018/6/4.
 * 邮箱： xuyulin@yixia.com
 * 描述： 音频编解码的类
 */
public class MyMediaCodec implements MyAudioRecord.AudioTranscribeListener, MyAudioEncoder.AudioEncoderListener, MyAudioDecoder.AudioDecoderListener {

    private String TAG = this.getClass().getSimpleName();
    private MyAudioEncoder audioEncoder;
    private MyAudioDecoder audioDecoder;
    private boolean isEncoderStart = false;
    private boolean isDecoderStart = false;
    private Thread encoderThread;
    private Thread decoderThread;
    private MyAudioRecord audioRecord;
    private MyAudioTrack audioTrack;

    public void startMediaCodec() {
        audioRecord = new MyAudioRecord();
        audioTrack = new MyAudioTrack();
        audioEncoder = new MyAudioEncoder();
        audioDecoder = new MyAudioDecoder();
        audioRecord.setListener(this);
        audioEncoder.setListener(this);
        audioDecoder.setListener(this);
        audioRecord.startTranscribe();
        audioTrack.audioTrackPlay();
        isEncoderStart = true;
        isDecoderStart = true;
        audioEncoder.initEncoder();
        audioDecoder.initDecoder();
        encoderThread = new Thread(new EncoderThread());
        decoderThread = new Thread(new DecoderThread());
        encoderThread.start();
        decoderThread.start();

    }

    public void stopMediaCodec() {
        isEncoderStart = false;
        isDecoderStart = false;
        try {
            encoderThread.interrupt();
            encoderThread.join(1000);
            audioEncoder.closeEncoder();
            decoderThread.interrupt();
            decoderThread.join(1000);
            audioDecoder.closeDecoder();
            audioRecord.stopTranscribe();
            audioTrack.audioTrackStop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void audioByte(byte[] audioByte) {
        long presentationTimeUs = (System.nanoTime()) / 1000L;
        audioEncoder.encode(audioByte, presentationTimeUs);
    }

    @Override
    public void onEncodeInvok(byte[] bytes, long presentationTimeUs) {
        audioDecoder.decoder(bytes, presentationTimeUs);
    }

    @Override
    public void onDecoderInvok(byte[] bytes, long presentationTimeUs) {
        audioTrack.starting(bytes);
    }

    public class EncoderThread implements Runnable {

        @Override
        public void run() {
            while (isEncoderStart) {
                audioEncoder.retrieve();
            }
        }
    }

    public class DecoderThread implements Runnable {

        @Override
        public void run() {
            while (isDecoderStart) {
                audioDecoder.retrieve();
            }
        }
    }

}
