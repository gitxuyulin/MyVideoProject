package com.example.xuyulin.myvideoproject.step2;

import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 作者： xuyulin on 2018/6/1.
 * 邮箱： xuyulin@yixia.com
 * 描述： 读取音频的类
 */
public class WAVFileRead {

    private String TAG = this.getClass().getSimpleName();
    private DataInputStream dataInputStream;

    public void openFile() {
        if (dataInputStream != null) {
            closeFile();
        }
        try {
            dataInputStream = new DataInputStream(new FileInputStream(AudioActivity.audioPath));
            readHeader();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        if (dataInputStream == null) {
            return;
        }
        try {
            dataInputStream.close();
            dataInputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int readWAV(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (dataInputStream == null) {
            return -1;
        }
        try {
            int bate = dataInputStream.read(audioData, offsetInBytes, sizeInBytes);
            return bate;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void readHeader() {
        WavFileHeader header = new WavFileHeader();
        try {
            byte[] four = new byte[4];
            byte[] two = new byte[2];

            header.mChunkID = "" + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte();
            Log.e(TAG, "ChunkID:" + header.mChunkID);

            dataInputStream.read(four);
            header.mChunkSize = byteArrayToInt(four);
            Log.e(TAG, "mChunkSize:" + header.mChunkSize);

            header.mFormat = "" + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte();
            Log.e(TAG, "mFormat:" + header.mFormat);

            header.mSubChunk1ID = "" + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte();
            Log.e(TAG, "mSubChunk1ID:" + header.mSubChunk1ID);

            dataInputStream.read(four);
            header.mSubChunk1Size = byteArrayToInt(four);
            Log.e(TAG, "mSubChunk1Size:" + header.mSubChunk1Size);

            dataInputStream.read(two);
            header.mAudioFormat = byteArrayToShort(two);
            Log.e(TAG, "mAudioFormat:" + header.mAudioFormat);

            dataInputStream.read(two);
            header.mNumChannel = byteArrayToShort(two);
            Log.e(TAG, "mNumChannel:" + header.mNumChannel);

            dataInputStream.read(four);
            header.mSampleRate = byteArrayToInt(four);
            Log.e(TAG, "mSampleRate:" + header.mSampleRate);

            dataInputStream.read(four);
            header.mByteRate = byteArrayToInt(four);
            Log.e(TAG, "mByteRate:" + header.mByteRate);

            dataInputStream.read(two);
            header.mBlockAlign = byteArrayToShort(two);
            Log.e(TAG, "mBlockAlign:" + header.mBlockAlign);

            dataInputStream.read(two);
            header.mBitsPerSample = byteArrayToShort(two);
            Log.e(TAG, "mBitsPerSample:" + header.mBitsPerSample);

            header.mSubChunk2ID = "" + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte() + dataInputStream.readByte();
            Log.e(TAG, "mSubChunk2ID:" + header.mSubChunk2ID);

            dataInputStream.read(four);
            header.mSubChunk1Size = byteArrayToInt(four);
            Log.e(TAG, "mSubChunk1Size:" + header.mSubChunk1Size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
