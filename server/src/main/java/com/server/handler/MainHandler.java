package com.server.handler;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.common.util.FileUtil;
import com.common.util.StringUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    // список частей хранилища
    private static final String[] PARTS = {"server_storage/part01/", "server_storage/part02/", "D:/Install/"};
    private static final String[] INFO = {"server_storage/info/"};
    private static final String CURRENT_PART = "server_storage/part02/";
    private static final String CURRENT_INFO = "server_storage/info/";


    // директория для временных файлов
    private static final String STORAGE_TEMP = System.getProperty("storage_temp", "server_storage/temp/");

    // максимальный размер массива с данными из файла в одном FileMessage
    private static final int MAX_CHUNK_SIZE = Integer.parseInt(System.getProperty("max_chunk_size", String.valueOf(16 * 1024)));

    // сообщение с частью данных файла
    private FileMessage fileMsg = new FileMessage(MAX_CHUNK_SIZE);

    private static final int STORAGE_PATH_DEPTH = 5;
    private static final int STORAGE_PATH_LENGTH = 3;

    private Properties info = new Properties();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) return;
            if (msg instanceof FileRequest) fileRequest(ctx, (FileRequest) msg);
            else if (msg instanceof FileMessage) fileMessage(ctx, (FileMessage) msg);
            else log.debug(msg.toString());
        } catch (Exception e) {
            ctx.writeAndFlush(e);
            log.error(e.toString(), e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private String prepareFilenameForStorage(String filename) {
        StringBuilder sb = new StringBuilder(filename.length() + STORAGE_PATH_DEPTH * STORAGE_PATH_LENGTH);
        for (int i = 0; i < STORAGE_PATH_DEPTH; i++) {
            sb.append(filename, i * STORAGE_PATH_LENGTH, STORAGE_PATH_LENGTH * (i + 1)).append("/");
        }
        sb.append(filename);
        return sb.toString();
    }

    private String findFileInStorage(String[] paths, String filename) throws FileNotFoundException {
        return FileUtil.find(paths, prepareFilenameForStorage(filename));
    }

    private void fileRequest(ChannelHandlerContext ctx, FileRequest fileRequest) throws Exception {
        if (StringUtil.isEmpty(fileRequest.getHash())) {
            String filename = FileUtil.find(INFO, prepareFilenameForStorage(fileRequest.getUuid()));
            try (InputStreamReader stream = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8)) {
                this.info.load(stream);
                fileMsg.setFilename(this.info.getProperty("filename", "unknown"));
                fileMsg.setHash(this.info.getProperty("hash"));
            }
        } else
            fileMsg.setHash(fileRequest.getHash());

        String filename = FileUtil.find(INFO, prepareFilenameForStorage(fileMsg.getHash()));
        fileMsg.channelWrite(ctx, filename, fileRequest);
    }

    private void fileMessage(ChannelHandlerContext ctx, FileMessage fileMsg) throws Exception {
        // пишем кусок файла в времменую папку
        fileMsg.fileWrite(ctx, STORAGE_TEMP + fileMsg.getUuid());
        // если записали последнюю чать, то вычисляем hash, если в хранилище нет файла с таким хэшем, то переносим его туда
        if (!fileMsg.hasNextData()) {
            String hash = FileUtil.sha256Hex(STORAGE_TEMP + fileMsg.getUuid());
            // сохранем информацию о файле
            String fileInfo = prepareFilenameForStorage(fileMsg.getUuid());
            !!! необходимо создать все папки
            try (OutputStream stream = new FileOutputStream(CURRENT_INFO + fileInfo)) {
                this.info.store(stream,"");
                this.info.setProperty("filename", fileMsg.getFilename());
                this.info.setProperty("hash", hash);
            }
            String filename = prepareFilenameForStorage(hash);
            if (!FileUtil.exists(PARTS, filename)) {
                File file = new File(STORAGE_TEMP + fileMsg.getUuid());
                File newFile = new File(CURRENT_PART + filename);
                file.renameTo(newFile);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString(), cause);
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(cause).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }

}