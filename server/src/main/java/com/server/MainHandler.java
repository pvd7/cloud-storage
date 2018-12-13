package com.server;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.util.FileUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    // список частей хранилища
    private static final String[] PARTS = {"server_storage/part01/", "server_storage/part02/", "D:/Install/"};

    // директория для временных файлов
    private static final String STORAGE_TEMP = System.getProperty("storage_temp", "server_storage/temp/");

    // максимальный размер массива с данными из файла в одном FileMessage
    private static final int MAX_CHUNK_SIZE = Integer.parseInt(System.getProperty("max_chunk_size", String.valueOf(16 * 1024)));

    // сообщение с частью данных файла
    private FileMessage fileMsg = new FileMessage(MAX_CHUNK_SIZE);

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

    private void fileRequest(ChannelHandlerContext ctx, FileRequest msg) throws Exception {
        String path = FileUtil.find(PARTS, msg.getUuid());
        fileMsg.channelWrite(ctx, path, msg);
    }

    private void fileMessage(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        msg.fileWrite(ctx, STORAGE_TEMP);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString(), cause);
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(cause).addListener(ChannelFutureListener.CLOSE);
        }
    }

}