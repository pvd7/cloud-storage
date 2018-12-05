package com.client.init;

import com.client.MainHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    private static final int MAX_OBJECT_SIZE = Integer.parseInt(System.getProperty("max_object_size", String.valueOf(10 * 1024 * 1024)));

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new ObjectDecoder(MAX_OBJECT_SIZE, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new ChunkedWriteHandler(),
                new MainHandler()
        );
    }

}
