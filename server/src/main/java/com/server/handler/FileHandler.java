package com.server.handler;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.Server;
import com.server.util.FileUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler extends ChannelInboundHandlerAdapter {

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
                int read;
                while ((read = raf.read(buf)) > 0) {
                    fileMessage.setRead(read);
                    fileMessage.setData(buf);
                    ctx.writeAndFlush(fileMessage);
                }
            }
//            ctx.flush();
        } catch (IOException e) {
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