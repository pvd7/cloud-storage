package com.server;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.util.FileUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class ServerMainHandler extends ChannelInboundHandlerAdapter {

    private static final String STORAGE = "server_storage";
    private static final int MAX_CHUNK_SIZE = 8 * 1024;

    private FileMessage fileMsg = new FileMessage();

    private byte[] buf = new byte[MAX_CHUNK_SIZE];

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
        System.out.println(msg.getId());
        try {
            String path = FileUtil.find(Server.PARTS, msg.getId());
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
                fileMsg.setId(msg.getId());
                fileMsg.setLength(raf.length());
                int read = raf.read(buf);
                ByteBuf buf1 = ctx.alloc().buffer(read, MAX_CHUNK_SIZE);
                fileMsg.setRead(raf.read(buf1));
                fileMsg.setData(buf1);
                ctx.writeAndFlush(fileMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(e);
        }
    }

    private void chanelFileWrite2(ChannelHandlerContext ctx, FileRequest msg) throws IOException {
//        System.out.println(msg.getId());
        try {
            String path = FileUtil.find(Server.PARTS, msg.getId());
            try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
//                ctx.writeAndFlush(new ChunkedFile(raf, MAX_CHUNK_SIZE));
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