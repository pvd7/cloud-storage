package com.client;

import com.common.entity.AuthorizedResponse;
import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.common.entity.UnauthorizedResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    private final static String STORAGE = "client_storage";
    public static final String STORAGE_TEMP = "client_storage/temp/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;
            if (msg instanceof AuthorizedResponse) authorizedResponse((AuthorizedResponse) msg);
            else if (msg instanceof UnauthorizedResponse) unauthorizedResponse((UnauthorizedResponse) msg);
            else if (msg instanceof FileMessage) fileMessage(ctx, (FileMessage) msg);
            else if (msg instanceof Exception) exceptionMessage((Exception) msg);
            else log.debug(msg.toString());
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void unauthorizedResponse(UnauthorizedResponse msg) {
    }

    private void authorizedResponse(AuthorizedResponse msg) {
    }

    private void exceptionMessage(Exception e) {
        log.error(e.toString(), e);
    }

    private void fileMessage(ChannelHandlerContext ctx, FileMessage fileMsg) throws IOException {
        fileMsg.writeAndRequest(STORAGE_TEMP, ctx);
//        try (FileOutputStream file = new FileOutputStream(STORAGE_TEMP + fileMsg.getId(), true)) {
//            file.write(fileMsg.getData(), 0, fileMsg.getRead());
//        }
//
//        if (fileMsg.hasNextData()) ctx.writeAndFlush(new FileRequest(fileMsg.getId(), fileMsg.getTotalRead()));
//
//        log.info(fileMsg.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString(), cause);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

}