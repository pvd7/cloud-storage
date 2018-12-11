package com.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileRequest extends AbstractMessage {

    private String id; // ID файла
    private UUID uuid;
    private long offset; // смещение

    /**
     * @param id     ID файла
     * @param offset начальная позиция в файле
     */
    public FileRequest(String id, long offset) {
        this.id = id;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "FileRequest{" +
                "id='" + id + '\'' +
                ", offset=" + offset +
                '}';
    }

}
