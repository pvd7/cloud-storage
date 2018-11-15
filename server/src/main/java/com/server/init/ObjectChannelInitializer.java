package com.server.init;

import com.server.handler.AuthHandler;
import com.server.handler.FileHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ObjectChannelInitializer extends ChannelInitializer<SocketChannel> {

    final static private int MAX_OBJECT_SIZE = 10 * 1024 * 1024;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new ObjectDecoder(MAX_OBJECT_SIZE, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AuthHandler(),
                new FileHandler()
        );
    }

}
