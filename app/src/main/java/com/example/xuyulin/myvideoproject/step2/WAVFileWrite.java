package com.example.xuyulin.myvideoproject.step2;

import android.os.Environment;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 作者： xuyulin on 2018/5/31.
 * 邮箱： xuyulin@yixia.com
 * 描述： 将音频写成wva文件
 */
public class WAVFileWrite {
    public static final String filePath = Environment.getExternalStorageDirectory() + "/daimeng.wav";
    private DataOutputStream outputStream;
    private int audioSize = 0;

    public void openFile(int sampleRateInHz, int bitsPerSample, int channels) {
        audioSize = 0;
        if (outputStream != null) {
            closeFile();
        }
        try {
            outputStream = new DataOutputStream(new FileOutputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writeHeader(sampleRateInHz, bitsPerSample, channels);
    }

    public void closeFile() {
        if (outputStream != null) {
            try {
                writeDataSize();
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeWVA(byte[] audioByte) {
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.write(audioByte, 0, audioByte.length);
            audioSize += audioByte.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDataSize() {
        if (outputStream == null) {
            return;
        }
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
            randomAccessFile.seek(WavFileHeader.WAV_CHUNKSIZE_OFFSET);
            randomAccessFile.write(intToByteArray((audioSize + WavFileHeader.WAV_CHUNKSIZE_EXCLUDE_DATA)), 0, 4);
            randomAccessFile.seek(WavFileHeader.WAV_SUB_CHUNKSIZE2_OFFSET);
            randomAccessFile.write(intToByteArray((audioSize)), 0, 4);
            randomAccessFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean writeHeader(int sampleRateInHz, int bitsPerSample, int channels) {
        if (outputStream == null) {
            return false;
        }
        WavFileHeader wavFileHeader = new WavFileHeader(sampleRateInHz, bitsPerSample, channels);
        try {
            outputStream.writeBytes(wavFileHeader.mChunkID);
            outputStream.write(intToByteArray(wavFileHeader.mChunkSize), 0, 4);
            outputStream.writeBytes(wavFileHeader.mFormat);
            outputStream.writeBytes(wavFileHeader.mSubChunk1ID);
            outputStream.write(intToByteArray(wavFileHeader.mSubChunk1Size), 0, 4);
            outputStream.write(shortToByteArray(wavFileHeader.mAudioFormat), 0, 2);
            outputStream.write(shortToByteArray(wavFileHeader.mNumChannel), 0, 2);
            outputStream.write(intToByteArray(wavFileHeader.mSampleRate), 0, 4);
            outputStream.write(intToByteArray(wavFileHeader.mByteRate), 0, 4);
            outputStream.write(shortToByteArray(wavFileHeader.mBlockAlign), 0, 2);
            outputStream.write(shortToByteArray(wavFileHeader.mBitsPerSample), 0, 2);
            outputStream.writeBytes(wavFileHeader.mSubChunk2ID);
            outputStream.write(intToByteArray(wavFileHeader.mSubChunk2Size), 0, 4);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

}
