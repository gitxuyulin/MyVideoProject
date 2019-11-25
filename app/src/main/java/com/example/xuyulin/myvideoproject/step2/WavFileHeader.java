package com.example.xuyulin.myvideoproject.step2;

/**
 * 作者： xuyulin on 2018/5/31.
 * 邮箱： xuyulin@yixia.com
 * 描述： wav头部写入文件的工具类
 */
public class WavFileHeader {

    public static final int WAV_CHUNKSIZE_OFFSET = 4;
    public static final int WAV_SUB_CHUNKSIZE2_OFFSET = 40;
    public static final int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;

    public String mChunkID = "RIFF";
    public int mChunkSize = 0;
    public String mFormat = "WAVE";

    public String mSubChunk1ID = "fmt ";
    public int mSubChunk1Size = 16;
    public short mAudioFormat = 1;
    public short mNumChannel = 1;
    public int mSampleRate = 8000;
    public int mByteRate = 0;
    public short mBlockAlign = 0;
    public short mBitsPerSample = 8;

    public String mSubChunk2ID = "data";
    public int mSubChunk2Size = 0;

    public WavFileHeader() {

    }

    public WavFileHeader(int sampleRateInHz, int bitsPerSample, int channels) {
        mSampleRate = sampleRateInHz;
        mBitsPerSample = (short) bitsPerSample;
        mNumChannel = (short) channels;
        mByteRate = mSampleRate * mNumChannel * mBitsPerSample / 8;
        mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
    }
}
