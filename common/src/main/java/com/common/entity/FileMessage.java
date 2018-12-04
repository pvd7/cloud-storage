package com.common.entity;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import java.util.Arrays;

@Data
public class FileMessage extends AbstractMessage {

    private String id;
    private String hash;
    private long length; // размер файла
    private int offset; // смещение
    private int read; // байт считали
    private ByteBuf data;

    @Override
    public String toString() {
        return "FileMessage{" +
                "id='" + id + '\'' +
                ", length=" + length +
                ", read=" + read +
                ", data=" + data +
                '}';
    }

}
