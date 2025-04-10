package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class VotingServer {
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: VotingServer [tcp|udp]");
            return;
        }

        startConsoleHandler();

        if (args[0].equalsIgnoreCase("tcp")) {
            startTcpServer();
        } else if (args[0].equalsIgnoreCase("udp")) {
            startUdpServer();
        } else {
            System.err.println("Invalid protocol. Use 'tcp' or 'udp'");
        }
    }

    private static void startConsoleHandler() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (isRunning.get()) {
                String command = scanner.nextLine().trim();
                handleServerCommand(command);
            }
            scanner.close();
        }).start();
    }

    private static void handleServerCommand(String command) {
        String[] parts = command.split(" ", 2);
        switch (parts[0].toLowerCase()) {
            case "save":
                if (parts.length < 2) {
                    System.out.println("Использование: save <filename>");
                    return;
                }
                saveState(parts[1]);
                break;
            case "load":
                if (parts.length < 2) {
                    System.out.println("Использование: load <filename>");
                    return;
                }
                loadState(parts[1]);
                break;
            case "exit":
                shutdown();
                break;
            default:
                System.out.println("Неизвестная серверная команда: " + command);
        }
    }

    private static void saveState(String filename) {
        try {
            ServerState.saveToFile(filename, new ServerState(
                    UpdVotingServerHandler.topics,
                    UpdVotingServerHandler.votesByTopic
            ));
            System.out.println("Сохранено в: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private static void loadState(String filename) {
        try {
            ServerState state = ServerState.loadFromFile(filename);
            UpdVotingServerHandler.topics.clear();
            UpdVotingServerHandler.topics.putAll(state.getTopics());
            UpdVotingServerHandler.votesByTopic.clear();
            UpdVotingServerHandler.votesByTopic.putAll(state.getVotesByTopic());

            System.out.println("Загружено из: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    private static void shutdown() {
        isRunning.set(false);
        System.out.println("Выключение сервера...");
        System.exit(0);
    }

    static void startTcpServer() throws InterruptedException {
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
                                    new TcpVotingServerHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(8080).sync();
            System.out.println("TCP-Сервер запущен на порту 8080");
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static void startUdpServer() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ch.pipeline().addLast(new UpdVotingServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BROADCAST, true);

            ChannelFuture f = b.bind(8080).sync();
            System.out.println("UDP-Сервер запущен на порту 8080");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}