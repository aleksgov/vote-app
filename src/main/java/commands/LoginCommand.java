package commands;

import io.netty.channel.ChannelHandlerContext;
import server.BaseVotingHandler;

public class LoginCommand implements Command {

    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            BaseVotingHandler handler
    ) {
        if (parts.length == 2 && parts[1].startsWith("-u=")) {
            String currentUser = parts[1].substring(3);
            if (handler.getCurrentUser() != null && handler.getCurrentUser().equals(currentUser)) {
                handler.sendResponse(ctx, "Пользователь уже вошёл в систему.");
            } else {
                handler.setCurrentUser(currentUser);
                handler.sendResponse(ctx, "Выполнен вход как " + currentUser);
            }
        } else {
            handler.sendResponse(ctx, "Неверный формат команды login. Используйте: login -u=<aleks>");
        }
    }
}
