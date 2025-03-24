package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;

public class LoginCommand {

    public static void execute(ChannelHandlerContext ctx, String[] parts, VotingServerHandler handler) {
        if (parts.length == 2 && parts[1].startsWith("-u=")) {
            String currentUser = parts[1].substring(3);
            if (VotingServerHandler.loggedUsers.contains(currentUser)) {
                ctx.writeAndFlush("Пользователь уже вошёл в систему.");
            } else {
                VotingServerHandler.loggedUsers.add(currentUser);
                handler.setCurrentUser(currentUser);
                ctx.writeAndFlush("Выполнен вход как " + currentUser);
            }
        } else {
            ctx.writeAndFlush("Неверный формат команды login. Используйте: login -u=имя");
        }
    }
}
