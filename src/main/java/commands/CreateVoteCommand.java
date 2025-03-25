package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.VoteCreationManager;
import java.net.InetSocketAddress;

public class CreateVoteCommand implements Command {

    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            VotingServerHandler handler,
            InetSocketAddress sender
    ) {
        if (parts.length >= 3 && parts[1].equalsIgnoreCase("vote") && parts[2].startsWith("-t=")) {

            StringBuilder topicBuilder = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                String part = parts[i].replaceAll("^\"|\"$", "");
                topicBuilder.append(part).append(" ");
            }
            String topic = topicBuilder.toString().replaceFirst("-t=", "").trim();

            if (!VotingServerHandler.topics.containsKey(topic)) {
                handler.sendResponse(ctx, sender, "Раздел '" + topic + "' не найден.");
                return;
            }
            handler.setVoteCreationManager(new VoteCreationManager(topic, handler.getCurrentUser()));
            handler.sendResponse(ctx, sender, "Введите название голосования:");
        } else {
            handler.sendResponse(ctx, sender, "Неверный формат команды. Используйте: create vote -t=\"<topic>\"");
        }
    }
}