package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class VotingServer {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: VotingServer [tcp|udp]");
            return;
        }

        if (args[0].equalsIgnoreCase("tcp")) {
            startTcpServer();
        } else if (args[0].equalsIgnoreCase("udp")) {
            VotingUDPServer.start();
        } else {
            System.err.println("Invalid protocol. Use 'tcp' or 'udp'");
        }
    }

    private static void startTcpServer() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new VotingServerHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(8080).sync();
            System.out.println("TCP Server started on port 8080");
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}