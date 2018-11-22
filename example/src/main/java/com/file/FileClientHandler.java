package com.file;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class FileClientHandler extends SimpleChannelInboundHandler<String> {

    private int i = 0;
    long len = 0;
    long count = 0;
    String state = "";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        i++;
        count += msg.length();
        System.out.printf("[%d, %s/%s] ", i, count, len);
        System.out.println(msg);

        if (msg.startsWith("OK:")) {
            i = 1;
            len = Integer.parseInt(msg.substring(3).trim());
            count = 0;
            state = "OK";
            System.out.println("file " + len);
        } else {

        }

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
}
