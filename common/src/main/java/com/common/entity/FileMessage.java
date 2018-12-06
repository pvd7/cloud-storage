package com.common.entity;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class FileMessage extends AbstractMessage {

    private String id;   // id файла
    private String hash; // hash файла
    private long length; // размер файла
    private long offset; // смещение в файле
    private int read;    // сколько байт передаем
    private byte[] data; // массив с байтами из файла

    /**
     * Конструктор
     *
     * @param capacity размер буфера с данными
     */
    public FileMessage(int capacity) {
        data = new byte[capacity];
    }

    /**
     * Сколько байт было уже считано из передаваемого файла
     *
     * @return количество байт
     */
    public long getTotalRead() {
        return offset + read;
    }

    /**
     * Все ли данные были переданы
     *
     * @return false - нет больше данных, true - их есть у меня
     */
    public boolean hasNextData() {
        return length > offset + read;
    }

    /**
     * Сохраняет полученные данные в файл
     * и если это была не последняя часть файла, то отпраяляет запрос на следующую часть данных
     *
     * @param storage директория, где лежит файл
     * @param ctx     контекст канала
     * @throws IOException исключение
     */
    public void fileWrite(ChannelHandlerContext ctx, String storage) throws IOException {
        // пишем данные в файл, если offset > 0 значит, это первая часть, поэтому создаем файл, append = (offset > 0)
        try (FileOutputStream file = new FileOutputStream(storage + id, offset > 0)) {
            file.write(data, 0, read);
        }
        // если есть еще данные, то отправляем запрос на следующую часть, указав в качестве смещения сколько всего байт было получено
        if (hasNextData())
            ctx.writeAndFlush(new FileRequest(id, getTotalRead()));

        log.debug(this.toString());
    }

    /**
     * Отправляет часть данных файла в канал
     *
     * @param ctx         контекст канала
     * @param path        путь к файлу
     * @param fileRequest запрашиваемые данные
     * @throws IOException исключение
     */
    public void channelWrite(ChannelHandlerContext ctx, String path, FileRequest fileRequest) throws IOException {
        log.debug(fileRequest.toString());

        id = fileRequest.getId();
        hash = fileRequest.getId();
        offset = fileRequest.getOffset();
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(fileRequest.getOffset());
            read = raf.read(data);
            length = raf.length();
        }
        ctx.writeAndFlush(this);

        log.debug(this.toString());
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "id='" + id + '\'' +
                ", hash='" + hash + '\'' +
                ", length=" + length +
                ", offset=" + offset +
                ", read=" + read +
                ", data=" + Arrays.toString(data) +
                '}';
    }

}
