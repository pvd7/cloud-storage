package com.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

public final class FileServer {

    // основная директоррия с файлами
    public static final String STORAGE = "server_storage";
    // список частей хранилища
    public static final String[] PARTS = {STORAGE + "/part01/", STORAGE + "/part02/"};
    public static final int PARTS_LENGTH = PARTS.length;
//    public static final String[] PARTS = System.getProperty("parts").split(",");

    static final int PORT = Integer.parseInt(System.getProperty("port", "8023"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new StringEncoder(CharsetUtil.UTF_8), // outbound
                                    new LineBasedFrameDecoder(8192), // inbound
                                    new StringDecoder(CharsetUtil.UTF_8), // inbound
                                    new ChunkedWriteHandler(), // inbound & outbound
                                    new FileServerHandler()); // inbound
                        }
                    });
            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
