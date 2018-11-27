package com.common.entity;

import java.util.Arrays;

public class FileMessage extends AbstractMessage {

    private String filename;
    private String hash;
    private long length;
    private int read;
    private byte[] data;
    private int totalRead;
    private int part;

    @Override
    public String toString() {
        return "FileMessage{" +
                "filename='" + filename + '\'' +
                ", length=" + length +
                ", read=" + read +
                ", totalRead=" + totalRead +
                ", part=" + part +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public int getRead() {
        return read;
    }

    public byte[] getData() {
        return data;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getTotalRead() {
        return totalRead;
    }

    public void setTotalRead(int totalRead) {
        this.totalRead = totalRead;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }
}
