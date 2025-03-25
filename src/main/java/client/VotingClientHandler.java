package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class VotingClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("\nОтвет сервера: " + msg);
        if (!msg.trim().equals("Завершение работы программы.")) {
            System.out.print("Введите команду: ");
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof java.net.SocketException && cause.getMessage().contains("Connection reset"))) {
            System.err.println("Произошла ошибка: " + cause.getMessage());
        }
        ctx.close();
    }
}
