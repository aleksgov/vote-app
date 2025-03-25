package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class VotingUDPClient {
    public static void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ch.pipeline().addLast(
                                    new StringDecoder(StandardCharsets.UTF_8),
                                    new StringEncoder(StandardCharsets.UTF_8),
                                    new UDPClientHandler()
                            );
                        }
                    });

            Channel channel = b.bind(0).sync().channel();
            System.out.println("UDP Client started");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите команду: ");
            while (true) {
                String command = scanner.nextLine();
                channel.writeAndFlush(new DatagramPacket(
                        channel.alloc().buffer().writeBytes(command.getBytes()),
                        new InetSocketAddress("localhost", 8080)
                ));

                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    static class UDPClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            System.out.println("\nServer response: " + msg);
            if (!msg.trim().equals("Завершение работы программы.")) {
                System.out.print("Введите команду: ");
            }
        }
    }
}