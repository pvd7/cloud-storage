package com.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileRequest extends AbstractMessage {

    private String uuid; // ID файла
//    private UUID uuid; // UUID файла
    private long offset; // смещение

    /**
     * @param uuid     ID файла
     * @param offset начальная позиция в файле
     */
    public FileRequest(String uuid, long offset) {
        this.uuid = uuid;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "FileRequest{" +
                "uuid='" + uuid + '\'' +
                ", offset=" + offset +
                '}';
    }

}
