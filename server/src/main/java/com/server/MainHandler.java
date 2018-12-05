package com.server;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.util.FileUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.RandomAccessFile;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    // максимальный размер массива с данными из файла в одном FileMessage
    private static final int MAX_CHUNK_SIZE = Integer.parseInt(System.getProperty("max_chunk_size", String.valueOf(8 * 1024)));;
    // буфер, в который считываются данные из файла
    private byte[] buffer = new byte[MAX_CHUNK_SIZE];

    private FileMessage fileMsg = new FileMessage();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) return;
            if (msg instanceof FileRequest) chanelFileWrite(ctx, (FileRequest) msg);
            else if (msg instanceof FileMessage) channelFileRead((FileMessage) msg);
            else log.debug(msg.toString());
        } catch (Exception e) {
            ctx.writeAndFlush(e);
            log.error(e.toString(), e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void chanelFileWrite(ChannelHandlerContext ctx, FileRequest fileRequest) throws Exception {
        log.debug(fileRequest.toString());

        String path = FileUtil.find(Server.PARTS, fileRequest.getId());
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(fileRequest.getOffset());
            fileMsg.setRead(raf.read(buffer));
            fileMsg.setData(buffer);
            fileMsg.setOffset(fileRequest.getOffset());
            fileMsg.setId(fileRequest.getId());
            fileMsg.setLength(raf.length());
            ctx.writeAndFlush(fileMsg);
        }

        log.debug(fileMsg.toString());
    }

    private void channelFileRead(FileMessage fileMsg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString(), cause);
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(cause).addListener(ChannelFutureListener.CLOSE);
        }
    }

}