package com.file;

import com.file.util.FileUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileServerHandler extends SimpleChannelInboundHandler<String> {

    private final static String CMD_GET = "GET:";
    private final static int CMD_GET_LEN = CMD_GET.length();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("HELLO: Type the name of the file to retrieve (please enter: get:<uuid>).\n");

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (msg.toUpperCase().startsWith(CMD_GET)) {
            downloadFile(ctx, msg);
        } else {
            ctx.writeAndFlush("ERR: command not found.\n");
        }
    }

    private void downloadFile(ChannelHandlerContext ctx, String msg) {
        String file = msg.substring(CMD_GET_LEN).trim();
        try {
            String path = FileUtil.find(FileServer.PARTS, file);
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
                long len = raf.length();
                ctx.write("OK: " + len + '\n');
//                ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
                ctx.write(new ChunkedFile(raf, 1024 * 1024 * 1024));
                ctx.writeAndFlush("\n");
            }
        } catch (IOException e) {
            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
    }

}

