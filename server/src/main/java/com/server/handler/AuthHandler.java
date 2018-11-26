package com.server.handler;

import com.common.entity.AuthRequest;
import com.common.entity.AuthorizedResponse;
import com.common.entity.UnauthorizedResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Проверка авторизации клента
 * если клиент уже был автоизирован, то прокидываем сообщение дальше,
 * иначе если токен не валидный или сообщение не {@link AuthRequest}, то отвечаем {@link UnauthorizedResponse}
 */
public class AuthHandler extends ChannelInboundHandlerAdapter {

    private boolean isAuthorized = true;

    private AuthorizedResponse authorizedResponse = new AuthorizedResponse();
    private UnauthorizedResponse unauthorizedResponse = new UnauthorizedResponse();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;

            if (msg instanceof AuthRequest) {
                isAuthorized = ((AuthRequest) msg).isValid("");
                if (isAuthorized) ctx.writeAndFlush(authorizedResponse);
                else ctx.writeAndFlush(unauthorizedResponse);
            } else {
                if (isAuthorized) ctx.fireChannelRead(msg);
                else ctx.writeAndFlush(unauthorizedResponse);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
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
