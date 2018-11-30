package com.client;

import com.common.entity.AuthorizedResponse;
import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.common.entity.UnauthorizedResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.*;

public class ClientMainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "client_storage";
    public static final String STORAGE_TEMP = "client_storage/temp/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;

            if (msg instanceof AuthorizedResponse) {
                authorizedResponse((AuthorizedResponse) msg);
            } else if (msg instanceof UnauthorizedResponse) {
                unauthorizedResponse((UnauthorizedResponse) msg);
            } else if (msg instanceof FileMessage) {
                fileMessage((FileMessage) msg);
            } else if (msg instanceof Exception) {
                exceptionMessage((Exception) msg);
            } else
                System.out.println(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void unauthorizedResponse(UnauthorizedResponse msg) {
    }

    private void authorizedResponse(AuthorizedResponse msg) {
    }

    private void exceptionMessage(Exception msg) {
        System.out.println(msg);
    }

    private void fileMessage(FileMessage msg) throws IOException {
        try (FileOutputStream file = new FileOutputStream(STORAGE_TEMP + msg.getFilename(), true)) {
            file.write(msg.getData(), 0, msg.getRead());
        };
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

}