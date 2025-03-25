package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class VotingClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("\nОтвет сервера: " + msg);

        if (msg.trim().equalsIgnoreCase("Завершение работы программы.")) {
            System.out.println("Соединение закрыто.");
            ctx.close();
        } else {
            System.out.print("Введите команду: ");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof java.net.SocketException && cause.getMessage().contains("Connection reset"))) {
            System.err.println("Ошибка соединения: " + cause.getMessage());
        }
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("\nСоединение с сервером разорвано.");
    }
}