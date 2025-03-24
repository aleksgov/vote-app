package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.VoteCreationManager;

public class CreateVoteCommand implements Command {

    @Override
    public void execute(ChannelHandlerContext ctx, String[] parts, VotingServerHandler handler) {
        if (parts.length == 3 && parts[2].startsWith("-t=")) {
            String topic = parts[2].substring(3);
            if (!VotingServerHandler.topics.containsKey(topic)) {
                ctx.writeAndFlush("Раздел '" + topic + "' не найден.");
                return;
            }
            handler.setVoteCreationManager(new VoteCreationManager(topic));
            ctx.writeAndFlush("Введите название голосования:");
        } else {
            ctx.writeAndFlush("Неверный формат команды create vote. Используйте: create vote -t=<topic>");
        }
    }
}

