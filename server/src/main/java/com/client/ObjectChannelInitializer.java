package com.client;

import com.common.Config;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ObjectChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new ObjectDecoder(Config.MAX_OBJECT_SIZE, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new ChunkedWriteHandler(),
                new ClientMainHandler()
        );
    }

}
