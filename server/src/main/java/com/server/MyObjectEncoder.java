package com.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.Serializable;

public class MyObjectEncoder extends ObjectEncoder {

    byte[] b;

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
//        System.out.println(msg);
//        System.out.println(out.getBytes(0, b));
//        System.out.println(b);
        super.encode(ctx, msg, out);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, Serializable msg, boolean preferDirect) throws Exception {
//        System.out.println(this.getClass().getName() + ": " + msg);
        return super.allocateBuffer(ctx, msg, preferDirect);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        System.out.println(msg);
        super.write(ctx, msg, promise);
    }
}
