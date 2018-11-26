package com.client;

import com.common.entity.AbstractMessage;
import com.common.entity.AuthRequest;
import com.common.entity.FileRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8023"));

    static final String TEMP_DIR = "client_storage/temp";

    public static void main(String[] args) throws Exception {
        Object msg = null;

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.TRACE))
                    .handler(new ObjectChannelInitializer());

            Channel ch = b.connect(HOST, PORT).sync().channel();

            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null) break;

                msg = null;
                if (line.startsWith("auth:"))
                    msg = new AuthRequest("");
                else if (line.startsWith("get:"))
                    msg = new FileRequest(line.substring("get:".length()).trim());

                if (msg == null) msg = line;

                lastWriteFuture = ch.writeAndFlush(msg);

                if (!(ch.isOpen() && ch.isActive())) System.err.println("ch.isOpen(): " + ch.isOpen() + ", ch.isActive(): " + ch.isActive());

                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}

