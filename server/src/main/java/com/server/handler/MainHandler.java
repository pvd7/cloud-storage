package com.server.handler;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.common.util.FileUtil;
import com.common.util.StringUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    // список частей хранилища
//    private static final String[] STORAGE_PARTS = {"server_storage/part01/", "server_storage/part02/", "D:/Install/"};
    private static final Path[] STORAGE_PARTS = {
            Paths.get("server_storage/part01")
            , Paths.get("server_storage/part02")
            , Paths.get("D:/Install/")};

    //    private static final String[] STORAGE_INFO = {"server_storage/info/"};
    private static final Path[] STORAGE_INFO = {Paths.get("server_storage/info/")};
    //    private static final String CURRENT_PART = "server_storage/part02/";
    private static final Path CURRENT_PART = Paths.get("server_storage/part02/");
    //    private static final String CURRENT_INFO = "server_storage/info/";
    private static final Path CURRENT_INFO = Paths.get("server_storage/info/");

    // директория для временных файлов
    private static final Path STORAGE_TEMP = Paths.get(System.getProperty("storage_temp", "server_storage/temp/"));

    // максимальный размер массива с данными из файла в одном FileMessage
    private static final int MAX_CHUNK_SIZE = Integer.parseInt(System.getProperty("max_chunk_size", String.valueOf(16 * 1024)));

    // сообщение с частью данных файла
    private FileMessage fileMsg = new FileMessage(MAX_CHUNK_SIZE);

    private static final int STORAGE_PATH_DEPTH = 5;
    private static final int STORAGE_PATH_LENGTH = 3;

    private static final String FILE_DATA_EXTENSION = ".data";
    private static final String FILE_INFO_EXTENSION = ".info";

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.toString(), cause);
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(cause).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static Path getPathForStorage(@NonNull String uuid) {
        Path path = Paths.get("");
        for (int i = 0; i < STORAGE_PATH_DEPTH; i++) {
            path = path.resolve(uuid.substring(i * STORAGE_PATH_LENGTH, (i + 1) * STORAGE_PATH_LENGTH));
        }
        return path;
    }

    private Path findFileInPaths(String uuid, Path[] paths) throws FileNotFoundException {
        return FileUtil.find(getPathForStorage(uuid).resolve(uuid), paths);
    }

    private void fileRequest(ChannelHandlerContext ctx, FileRequest fileRequest) throws Exception {
        if (StringUtil.isEmpty(fileRequest.getHash())) {
            Path file = findFileInPaths(fileRequest.getUuid() + FILE_INFO_EXTENSION, STORAGE_INFO);
            try (InputStreamReader stream = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
                this.info.load(stream);
                fileMsg.setFilename(this.info.getProperty("filename", "unknown"));
                fileMsg.setHash(this.info.getProperty("hash"));
            }
        } else
            fileMsg.setHash(fileRequest.getHash());

        Path file = findFileInPaths(fileMsg.getHash() + FILE_DATA_EXTENSION, STORAGE_PARTS);
        fileMsg.channelWrite(ctx, file.toFile(), fileRequest);
    }

    private void fileMessage(ChannelHandlerContext ctx, FileMessage fileMsg) throws Exception {
        // сохраняем пришедшие данные в временный файл
        Path fileTemp = STORAGE_TEMP.resolve(fileMsg.getUuid());
        fileMsg.fileWrite(ctx, fileTemp);

        // если записали последнюю часть, то сохраняем описание файла и переносим файл в хранилище
        if (!fileMsg.hasNextData()) {
            String uuid = fileMsg.getUuid();
            String hash = FileUtil.sha256Hex(fileTemp);
            storeFileInfo(uuid, fileMsg.getFilename(), hash);
            storeFileData(fileTemp, hash);
            log.debug(fileMsg.toString());
            log.debug(hash);
        }
    }

    private static void storeFileInfo(String uuid, String filename, String hash) throws IOException {
        Path path = CURRENT_INFO.resolve(getPathForStorage(uuid));
        Path file = Files.createDirectories(path)
                .resolve(uuid + FILE_INFO_EXTENSION);
        try (OutputStream stream = Files.newOutputStream(file)) {
            Properties prop = new Properties();
            prop.setProperty("filename", filename);
            prop.setProperty("hash", hash);
            prop.store(stream, "file description");
        }
    }

    private static void storeFileData(Path source, String hash) throws IOException {
        Path pathForStorage = getPathForStorage(hash);
        Path file = Paths.get(hash + FILE_DATA_EXTENSION);
        if (!FileUtil.exists(STORAGE_PARTS, pathForStorage.resolve(file))) {
            Path target = Files.createDirectories(CURRENT_PART.resolve(pathForStorage))
                    .resolve(file);
            Files.move(source, target);
        } else {
            Files.delete(source);
        }
    }

    public static void main(String[] args) throws IOException {
        String uuid = "d39f461c2af04794a41b009e78ebe56a";
    }

}