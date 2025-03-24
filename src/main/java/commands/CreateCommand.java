package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.VoteCreationManager;

import java.util.ArrayList;

public class CreateCommand {

    public static void execute(ChannelHandlerContext ctx, String[] parts, VotingServerHandler handler) {
        if (parts.length >= 2) {
            if ("topic".equals(parts[1])) {

                if (parts.length == 3 && parts[2].startsWith("-n=")) {
                    String topicName = parts[2].substring(3);
                    if (VotingServerHandler.topics.containsKey(topicName)) {
                        ctx.writeAndFlush("Раздел с таким названием уже существует.");
                    } else {
                        VotingServerHandler.topics.put(topicName, new ArrayList<>());
                        ctx.writeAndFlush("Раздел '" + topicName + "' создан.");
                    }
                } else {
                    ctx.writeAndFlush("Неверный формат команды create topic. Используйте: create topic -n=<topic>");
                }
            } else if ("vote".equals(parts[1])) {

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
            } else {
                ctx.writeAndFlush("Неверная команда create. Используйте: create topic или create vote");
            }
        }
    }
}
