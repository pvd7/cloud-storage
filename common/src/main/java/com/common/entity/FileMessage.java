package com.common.entity;

import com.common.util.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class FileMessage extends AbstractMessage {

    private String uuid;   // uuid файла
    private String hash; // hash файла
    private String filename; // имя файла
    private long length; // размер файла
    private long offset; // смещение в файле
    private int read;    // сколько байт передаем
    private byte[] data; // массив с байтами из файла

    /**
     * @param capacity массива буфера с данными
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
     * @param file директория, где лежит файл
     * @param ctx  контекст канала
     * @throws IOException исключение
     */
    public void fileWrite(ChannelHandlerContext ctx, Path file) throws IOException {
        // пишем данные в файл, если offset > 0 значит, это первая часть, поэтому создаем файл, append = (offset > 0)
        try (FileOutputStream out = new FileOutputStream(file.toFile(), offset > 0)) {
            out.write(data, 0, read);
        }
        // если есть еще данные, то отправляем запрос на следующую часть, указав в качестве смещения сколько всего байт было получено
        if (hasNextData())
            ctx.writeAndFlush(new FileRequest(uuid, getTotalRead()));
    }

    /**
     * Отправляет часть данных файла в канал
     *
     * @param ctx         контекст канала
     * @param file        путь к файлу
     * @param fileRequest запрашиваемые данные
     * @throws IOException исключение
     */
    public void channelWrite(ChannelHandlerContext ctx, File file, FileRequest fileRequest) throws IOException {
        uuid = fileRequest.getUuid();
        offset = fileRequest.getOffset();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(fileRequest.getOffset());
            read = raf.read(data);
            length = raf.length();
        }
        ctx.writeAndFlush(this);
    }

    public String getFilenameOrHash() throws DecoderException {
        return StringUtil.isEmpty(filename) ? hash : filename;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "uuid='" + uuid + '\'' +
                ", hash='" + hash + '\'' +
                ", filename='" + filename + '\'' +
                ", length=" + length +
                ", offset=" + offset +
                ", read=" + read +
//                ", data=" + Arrays.toString(data) +
                '}';
    }
}
