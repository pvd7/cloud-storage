package com.server;

import com.common.entity.FileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.FileOutputStream;

public class MyChunkedWriteHandler extends ChunkedWriteHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println(this.getClass().getName() + ": " + msg);
        // корректно пишет в файл
//        try (FileOutputStream file = new FileOutputStream("client_storage/tmp/" + ((FileMessage) msg).getId(), true)) {
//            file.write(((FileMessage) msg).getData(), 0, ((FileMessage) msg).getRead());
//        };
//        System.out.println(msg);
        super.write(ctx, msg, promise);
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println(msg);
        super.channelRead(ctx, msg);
    }



}
