package com.client.handler;

import com.common.entity.AuthorizedResponse;
import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.common.entity.UnauthorizedResponse;
import com.common.util.FileUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "client_storage";
//    public static final String STORAGE_TEMP = "client_storage/temp/";
    private static final Path STORAGE_TEMP = Paths.get("client_storage/temp/");

    // максимальный размер массива с данными из файла в одном FileMessage
    private static final int MAX_CHUNK_SIZE = Integer.parseInt(System.getProperty("max_chunk_size", String.valueOf(16 * 1024)));

    // сообщение с частью данных файла
    private FileMessage fileMsg = new FileMessage(MAX_CHUNK_SIZE);

    public static final Map<String, File> uploadFiles = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;
            if (msg instanceof AuthorizedResponse) authorizedResponse((AuthorizedResponse) msg);
            else if (msg instanceof UnauthorizedResponse) unauthorizedResponse((UnauthorizedResponse) msg);
            else if (msg instanceof FileRequest) fileRequest(ctx, (FileRequest) msg);
            else if (msg instanceof FileMessage) fileMessage(ctx, (FileMessage) msg);
            else if (msg instanceof Exception) exceptionMessage((Exception) msg);
            else log.debug(msg.toString());
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void fileRequest(ChannelHandlerContext ctx, FileRequest msg) throws IOException {
        File file = uploadFiles.get(msg.getUuid());
        int size = uploadFiles.size();
        if (file != null) {
            fileMsg.setFilename(file.getName());
            fileMsg.channelWrite(ctx, file, msg);

            if (!fileMsg.hasNextData()) {
                uploadFiles.remove(msg.getUuid());
                log.debug("sent last part of file ({}): {}, {}", size, file, msg);
            }
        } else {
            log.error("requested file was not found in the queue ({}): {}", size, msg);
        }
    }

    private void unauthorizedResponse(UnauthorizedResponse msg) {
    }

    private void authorizedResponse(AuthorizedResponse msg) {
    }

    private void exceptionMessage(Exception e) {
        log.error(e.toString(), e);
    }

    private void fileMessage(ChannelHandlerContext ctx, FileMessage msg) throws IOException, DecoderException {
        Path file = STORAGE_TEMP.resolve(msg.getFilenameOrHash());
        msg.fileWrite(ctx, file);
        if (!msg.hasNextData()) {
            log.debug(FileUtil.sha256Hex(file));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString(), cause);
        ctx.close();
    }

}