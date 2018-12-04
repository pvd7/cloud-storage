package com.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileRequest extends AbstractMessage {

    private String id; // ID файла
    private int offset; // смещение
//    private int length; // количество байт

    public FileRequest(String id, int offset) {
        this.id = id;
        this.offset = offset;
    }

}
