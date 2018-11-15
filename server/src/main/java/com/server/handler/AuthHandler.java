package com.server.handler;

import com.common.entity.AuthRequest;
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

    private boolean isAuth = true;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;

            if (msg instanceof AuthRequest) {
                isAuth = ((AuthRequest) msg).isValid("");
                if (!isAuth) ctx.writeAndFlush(new UnauthorizedResponse());
            } else if (isAuth)
                ctx.fireChannelRead(msg);
            else
                ctx.writeAndFlush(new UnauthorizedResponse());
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
