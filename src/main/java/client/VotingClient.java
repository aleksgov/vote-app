package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class VotingClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new VotingClientHandler()
                            );
                        }
                    });

            ChannelFuture f = b.connect(HOST, PORT).sync();
            System.out.println("Клиент подключился к серверу на порту 8080");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите команду: ");
            while (true) {
                String command = scanner.nextLine();
                f.channel().writeAndFlush(command);
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
