package com.file;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileServerHandler extends SimpleChannelInboundHandler<String> {

    private final static String CMD_GET = "GET:";
    private final static int CMD_GET_LEN = CMD_GET.length();

    private String fileExists(ChannelHandlerContext ctx, String file) {
        try {
            for (int i = 0; i < FileServer.PARTS_LENGTH; i++) {
                if (Files.exists(Paths.get(FileServer.PARTS[i] + file)))
                    return FileServer.PARTS[i] + file;
            }
        } catch (Exception e) {
            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
            return "";
        }
        return "";
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("HELLO: Type the name of the file to retrieve.\n");
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
        String path = fileExists(ctx, file);
        if (path.equals("")) {
            ctx.writeAndFlush("ERR: file not found: " + file + "\n");
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
                long len = raf.length();
                ctx.write("OK: " + len + '\n');
                ctx.write(new DefaultFileRegion(raf.getChannel(), 0, len));
//                ctx.write(new ChunkedFile(raf));
                ctx.writeAndFlush("\n");
            } catch (IOException e) {
                ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
            }
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

