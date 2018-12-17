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
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "client_storage";
    public static final String STORAGE_TEMP = "client_storage/temp/";

    // максимальный размер массива с данными из файла в одном FileMessage
    private static final int MAX_CHUNK_SIZE = Integer.parseInt(System.getProperty("max_chunk_size", String.valueOf(16 * 1024)));

    // сообщение с частью данных файла
    public FileMessage fileMsg = new FileMessage(MAX_CHUNK_SIZE);

    public static final Map<String, String> uploadFiles = new HashMap<>();

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
        String path = uploadFiles.get(msg.getUuid());
        fileMsg.channelWrite(ctx, path, msg);
    }

    private void unauthorizedResponse(UnauthorizedResponse msg) {
    }

    private void authorizedResponse(AuthorizedResponse msg) {
    }

    private void exceptionMessage(Exception e) {
        log.error(e.toString(), e);
    }

    private void fileMessage(ChannelHandlerContext ctx, FileMessage msg) throws IOException, DecoderException {
        msg.fileWrite(ctx, STORAGE_TEMP + msg.getFilenameOrHash());
        if (!msg.hasNextData()) {
            log.debug(FileUtil.sha256Hex(STORAGE_TEMP + msg.getFilenameOrHash()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString(), cause);
        ctx.close();
    }

}