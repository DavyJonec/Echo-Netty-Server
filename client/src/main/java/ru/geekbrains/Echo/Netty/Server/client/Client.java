package ru.geekbrains.Echo.Netty.Server.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;


public class Client {

    private final int port;

    public static void main(String[] args) {
        try {
            new Client(12770).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Client(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new LineBasedFrameDecoder(512),
                                    new StringEncoder(),
                                    new StringDecoder(),
                                    new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
                                            System.out.println(msg);
                                        }
                                    });
                        }
                    });

            Channel channel = bootstrap.connect("localhost", port).sync().channel();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                if ("stop".equals(message)) {
                    break;
                }

                channel.writeAndFlush(message + System.lineSeparator());
            }
            channel.close();
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
