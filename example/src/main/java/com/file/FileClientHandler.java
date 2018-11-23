package com.file;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class FileClientHandler extends SimpleChannelInboundHandler<String> {

//    int i = 0;

    private OutputStream outFile = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws IOException {


        if (outFile != null) {
            outFile.write(msg.getBytes(CharsetUtil.UTF_8));
        } else {
            if (msg.startsWith("OK:")) {
                outFile = Files.newOutputStream(Paths.get(FileClient.TEMP_DIR + "/" + UUID.randomUUID()), StandardOpenOption.CREATE);
                System.err.println("create file");
//                i = 0;
            } else if (msg.startsWith("HELLO:")) {
                System.err.println(msg);
            }
        }

//        System.err.println("[" + i++ + "] " + msg);
//        System.err.println(msg);

        if (!(ctx.channel().isOpen() && ctx.channel().isActive())) {
            System.err.println("ctx.channel().isOpen(): " + ctx.channel().isOpen()
                    + ", ctx.channel().isActive(): " + ctx.channel().isActive());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.err.println("channelReadComplete");
        if (outFile != null) {
            outFile.close();
            outFile = null;
            System.err.println("close file");
        }
    }



}
