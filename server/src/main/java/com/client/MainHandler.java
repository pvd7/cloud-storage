package com.client;

import com.common.entity.AuthorizedResponse;
import com.common.entity.FileMessage;
import com.common.entity.UnauthorizedResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "server_storage";

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

    private void fileMessage(FileMessage msg) {
        System.out.printf("%s: %d of %d\n", msg.getFilename(), msg.getRead(), msg.getLength());
        System.out.println(new String(msg.getData(), 0, msg.getRead(), StandardCharsets.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}