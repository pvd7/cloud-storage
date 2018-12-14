package com.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileRequest extends AbstractMessage {

    private String uuid; // ID файла
    private long offset; // смещение
    private String hash; // UUID файла
    private String filename; // имя файла

    /**
     * @param uuid     ID файла
     * @param offset начальная позиция в файле
     */
    public FileRequest(String uuid, long offset) {
        this.uuid = uuid;
        this.offset = offset;
    }

    public FileRequest(long offset, String hash, String filename) {
        this.offset = offset;
        this.hash = hash;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "FileRequest{" +
                "uuid='" + uuid + '\'' +
                ", offset=" + offset +
                '}';
    }

}
