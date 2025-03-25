package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.DatagramPacketDecoder;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class VotingClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: VotingClient [tcp|udp]");
            return;
        }

        if (args[0].equalsIgnoreCase("tcp")) {
            startTcpClient();
        } else if (args[0].equalsIgnoreCase("udp")) {
            startUdpClient();
        } else {
            System.err.println("Неверный протокол. Используйте 'tcp' или 'udp'");
        }
    }

    private static void startTcpClient() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ch.pipeline().addLast(
                                    new DatagramPacketDecoder(new StringDecoder(CharsetUtil.UTF_8)),
                                    new DatagramPacketEncoder<>(new StringEncoder(CharsetUtil.UTF_8)),
                                    new VotingClientHandler()
                            );
                        }
                    });

            ChannelFuture f = b.connect(HOST, PORT).sync();
            System.out.println("Клиент подключился к серверу на порту 8080");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите команду: ");
            while (true) {
                String command = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(command)) {
                    f.channel().writeAndFlush("exit").sync();
                    f.channel().close().sync();
                    break;
                }
                f.channel().writeAndFlush(command);
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    private static void startUdpClient() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            // Удаляем декодеры/энкодеры для работы с DatagramPacket напрямую
                            ch.pipeline().addLast(new VotingClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_BROADCAST, true);

            Channel channel = b.bind(0).sync().channel();
            System.out.println("UDP-Клиент подключен к серверу на порту 8080");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Введите команду: ");
                String command = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }
                channel.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(command, CharsetUtil.UTF_8),
                        new InetSocketAddress(HOST, PORT)
                ));
            }
        } finally {
            group.shutdownGracefully();
        }
    }

}