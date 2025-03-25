package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import java.net.InetSocketAddress;

public class LoginCommand implements Command {

    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            VotingServerHandler handler,
            InetSocketAddress sender
    ) {
        if (parts.length == 2 && parts[1].startsWith("-u=")) {
            String currentUser = parts[1].substring(3);
            if (VotingServerHandler.loggedUsers.contains(currentUser)) {
                handler.sendResponse(ctx, sender, "Пользователь уже вошёл в систему.");
            } else {
                VotingServerHandler.loggedUsers.add(currentUser);
                handler.setCurrentUser(currentUser);
                handler.sendResponse(ctx, sender, "Выполнен вход как " + currentUser);
            }
        } else {
            handler.sendResponse(ctx, sender, "Неверный формат команды login. Используйте: login -u=имя");
        }
    }
}