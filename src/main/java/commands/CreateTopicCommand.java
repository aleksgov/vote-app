package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;

import java.util.ArrayList;

public class CreateTopicCommand implements Command {

    @Override
    public void execute(ChannelHandlerContext ctx, String[] parts, VotingServerHandler handler) {
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
    }
}
