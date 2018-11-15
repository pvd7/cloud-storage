package com.server.init;

import com.server.handler.ChunkFileServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

public class ChunkFileChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new StringEncoder(CharsetUtil.UTF_8),
                new LineBasedFrameDecoder(8192),
                new StringDecoder(CharsetUtil.UTF_8),
                new ChunkedWriteHandler(),
                new ChunkFileServerHandler());
    }

}
