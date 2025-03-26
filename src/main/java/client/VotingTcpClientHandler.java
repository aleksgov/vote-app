package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class VotingTcpClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("Ответ сервера: " + msg);
        System.out.print("Введите команду: ");
        if (msg.trim().equalsIgnoreCase("Завершение работы программы.")) {
            System.out.println("Соединение закрыто.");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Ошибка соединения: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("\nСоединение с сервером разорвано.");
    }
}
