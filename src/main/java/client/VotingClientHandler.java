package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class VotingClientHandler extends SimpleChannelInboundHandler<DatagramPacket>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        String msg = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println("\nОтвет сервера: " + msg);
        System.out.print("Введите команду: ");

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