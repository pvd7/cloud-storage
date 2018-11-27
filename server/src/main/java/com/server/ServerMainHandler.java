package com.server;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.util.FileUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ServerMainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "server_storage";
    private static final int CHUNK_FILE_SIZE = 8 * 1024;

    private FileMessage fileMessage = new FileMessage();

    private byte[] buf = new byte[CHUNK_FILE_SIZE];

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;

            if (msg instanceof FileRequest) {
                chanelFileWrite(ctx, (FileRequest) msg);
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
            fileMessage.setFilename(msg.getFilename());
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
                fileMessage.setLength(raf.length());
                fileMessage.setTotalRead(0);
                int read;
                int part = 0;
                ChannelFuture future = null;
                while ((read = raf.read(buf)) > 0) {
                    fileMessage.setRead(read);
                    fileMessage.setData(buf);
                    fileMessage.setPart(part++);
                    fileMessage.setTotalRead(fileMessage.getTotalRead() + read);

                    future = ctx.writeAndFlush(fileMessage);

                    System.out.println("isDone: " + future.isDone());
                    System.out.println("isSuccess: " + future.isSuccess());
                    System.out.println("getTotalRead: " + fileMessage.getTotalRead());
                    System.out.println("getRead: " + fileMessage.getRead());

//                    if (!future.isSuccess()) {
//                        Throwable throwable = future.cause();
//                        throwable.printStackTrace();
//                        break;
//                    }

//                    while (!future.isDone()) {  };

                }
//                ctx.flush();
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