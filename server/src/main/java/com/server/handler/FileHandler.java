package com.server.handler;

import com.common.entity.FileMessage;
import com.common.entity.FileRequest;
import com.server.Server;
import com.server.util.FileUtil;
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
                    ctx.flush();
                }
            }
//            ctx.flush();
        } catch (IOException e) {
            ctx.writeAndFlush(e);
        }

//        String path = FileUtil.find(Server.PARTS, msg.getFilename());
//        if (Files.exists(Paths.get(STORAGE + "/" + msg.getFilename()))) {
//            FileMessage fm = new FileMessage(Paths.get(STORAGE + "/" + msg.getFilename()));
//            ctx.writeAndFlush(fm);
//        }
    }

    private void chanelFileWrite1(ChannelHandlerContext ctx, FileRequest msg) throws IOException {
        String path = STORAGE + "/" + msg.getFilename();
        if (Files.exists(Paths.get(path))) {
//            FileMessage fm = new FileMessage(Paths.get(path));
            RandomAccessFile file = new RandomAccessFile(path, "r");
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            ctx.writeAndFlush("\n");
        }
    }

    /**
     * Сохраняет файл из сообщения на диск
     *
     * @param msg сообщение с файлом
     * @throws IOException исключение
     */
    private void channelFileRead(FileMessage msg) throws IOException {
//        Files.write(Paths.get(STORAGE + "/" + msg.getFilename()), msg.getData(), StandardOpenOption.CREATE);
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