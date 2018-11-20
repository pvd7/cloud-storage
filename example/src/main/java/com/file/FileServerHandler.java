/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.file;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileServerHandler extends SimpleChannelInboundHandler<String> {

    private final static String STORAGE = "server_storage";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("HELLO: Type the name of the file to retrieve.\n");
    }

    private Path exist(String dir) throws IOException {
        Files.list(Paths.get(STORAGE)).map(Path::getFileName).forEach(System.out::println);
        return null;
    };

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String storage;
        Files.list(Paths.get(STORAGE)).map(Path::getFileName).forEach(System.out::println);
//        o -> {if (Files.exists(o + ))}


        if (Files.exists(Paths.get(STORAGE + "/" + msg))) {
            RandomAccessFile raf = null;
            long length = -1;
            try {
                raf = new RandomAccessFile(msg, "r");
                length = raf.length();
            } catch (Exception e) {
                ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
                return;
            } finally {
                if (length < 0 && raf != null) {
                    raf.close();
                }
            }

            ctx.write("OK: " + raf.length() + '\n');
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
            ctx.writeAndFlush("\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
    }
}

