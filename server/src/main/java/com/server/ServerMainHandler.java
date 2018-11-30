package com.server;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.util.FileUtil;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ServerMainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "server_storage";
    private static final int CHUNK_FILE_SIZE = 8 * 1024;

    private FileMessage fileMsg = new FileMessage();

    private byte[] buf = new byte[CHUNK_FILE_SIZE];

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;

            if (msg instanceof FileRequest) {
                chanelFileWrite2(ctx, (FileRequest) msg);
            } else if (msg instanceof FileMessage) {
                channelFileRead((FileMessage) msg);
            } else {
                System.err.println(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void chanelFileWrite(ChannelHandlerContext ctx, FileRequest msg) throws IOException {
        System.out.println(msg.getFilename());
        try {
            String path = FileUtil.find(Server.PARTS, msg.getFilename());
            fileMsg.setFilename(msg.getFilename());
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
                fileMsg.setLength(raf.length());
                fileMsg.setTotalRead(0);
                int read;
                int part = 0;
                ChannelFuture future = null;
                while ((read = raf.read(buf)) > 0) {
                    fileMsg.setRead(read);
                    fileMsg.setData(buf);
                    fileMsg.setPart(part++);
                    fileMsg.setTotalRead(fileMsg.getTotalRead() + read);

                    future = ctx.writeAndFlush(fileMsg);
//                    System.out.println("isDone: " + future.isDone());
                    if (!future.isSuccess()) {
//                        System.out.println(future.getNow());
//                        System.out.println(future);
//                        System.out.println("isDone: " + future.isDone());
//                        System.out.println("isSuccess: " + future.isSuccess());
//                        System.out.println(fileMsg);
//                        Thread.sleep(10000);
                    }
                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(e);
        }
    }

    private void chanelFileWrite2(ChannelHandlerContext ctx, FileRequest msg) throws IOException {
        System.out.println(msg.getFilename());
        try {
            String path = FileUtil.find(Server.PARTS, msg.getFilename());
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
//                ctx.writeAndFlush(new ChunkedFile(raf, CHUNK_FILE_SIZE));
                ctx.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(e);
        }
    }

    private void channelFileRead(FileMessage msg) throws IOException {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(cause).addListener(ChannelFutureListener.CLOSE);
        }
    }

}