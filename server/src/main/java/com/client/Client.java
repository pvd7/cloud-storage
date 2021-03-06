package com.client;

import com.client.handler.MainHandler;
import com.client.init.ChannelInitializer;
import com.common.entity.AuthRequest;
import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.function.BiConsumer;

@Slf4j
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
                    .handler(new ChannelInitializer());

            Channel ch = b.connect(HOST, PORT).sync().channel();

            Boolean postedMsg;
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                postedMsg = false;
                String line = in.readLine();
                if (line == null) break;

                msg = null;
                if (line.startsWith("auth:"))
                    msg = new AuthRequest("");
                else if (line.startsWith("get:"))
                    msg = new FileRequest(line.substring("get:".length()).trim(), 0);
                else if (line.startsWith("id:"))
                    msg = new FileRequest(line.substring("id:".length()).trim(), 0);
                else if (line.startsWith("hash:"))
                    msg = new FileRequest(0, line.substring("hash:".length()).trim(), "new file");
                else if (line.startsWith("post:")) {
                    Path path = Paths.get("client_storage/" + line.substring("post:".length()).trim());

                    UUID uuid = UUID.randomUUID();

                    MainHandler.uploadFiles.put(uuid.toString().replace("-", ""), path.toFile());

                    FileMessage fileMsg = new FileMessage(8 * 1024);
                    fileMsg.setUuid(uuid.toString().replace("-", ""));
                    fileMsg.setFilename(path.getFileName().toString());

                    try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
                        fileMsg.setLength(file.length());
                    }
                    msg = fileMsg;
                } else if (line.startsWith("dir:")) {
                    postedMsg = true;
                    Path path = Paths.get(line.substring("dir:".length()).trim());
                    Files.walkFileTree(path, new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            log.debug(file.toString());
                            postFileMsg(ch, file.toFile());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    log.debug(String.valueOf(MainHandler.uploadFiles.size()));
                    MainHandler.uploadFiles.forEach((s, file) -> System.out.println(file));
                }

                if (!postedMsg) {
                    if (msg == null) msg = line;
                    lastWriteFuture = ch.writeAndFlush(msg);
                }

                if (!(ch.isOpen() && ch.isActive()))
                    System.err.println("ch.isOpen(): " + ch.isOpen() + ", ch.isActive(): " + ch.isActive());

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

    private static void postFileMsg(Channel ch, File file) throws IOException {
        while (MainHandler.uploadFiles.size() > 10) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        UUID uuid = UUID.randomUUID();

        FileMessage fileMsg = new FileMessage(8 * 1024);
        fileMsg.setUuid(uuid.toString().replace("-", ""));
        fileMsg.setFilename(file.getName());

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            fileMsg.setLength(raf.length());
        }

        if (fileMsg.getLength() > 0)
            MainHandler.uploadFiles.put(uuid.toString().replace("-", ""), file);
        else
            fileMsg.setDescription(file.toString());

        ch.writeAndFlush(fileMsg);

//        ChannelFuture future = ch.writeAndFlush(fileMsg);
//        future.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                future.getNow()
//            }
//        });
    }

}

