package com.server.handler;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileHandler extends ChannelInboundHandlerAdapter {

    final static String STORAGE = "server_storage";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;

            if (msg instanceof FileRequest) {
                chanelFileWrite(ctx, (FileRequest) msg);
            } else if (msg instanceof FileMessage) {
                channelFileRead((FileMessage) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * Пишет {@link FileMessage} в канал
     *
     * @param ctx контекст
     * @param msg входящее сообщение {@link FileRequest}
     * @throws IOException исключение
     */
    private void chanelFileWrite(ChannelHandlerContext ctx, FileRequest msg) throws IOException {
        if (Files.exists(Paths.get(STORAGE + "/" + msg.getFilename()))) {
            FileMessage fm = new FileMessage(Paths.get(STORAGE + "/" + msg.getFilename()));
            ctx.writeAndFlush(fm);
        }
    }

    /**
     * Сохраняет файл из сообщения на диск
     *
     * @param msg сообщение с файлом
     * @throws IOException исключение
     */
    private void channelFileRead(FileMessage msg) throws IOException {
        Files.write(Paths.get(STORAGE + "/" + msg.getFilename()), msg.getData(), StandardOpenOption.CREATE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}