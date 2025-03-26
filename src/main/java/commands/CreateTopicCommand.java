package commands;

import io.netty.channel.ChannelHandlerContext;
import server.BaseVotingHandler;
import java.util.ArrayList;

public class CreateTopicCommand implements Command {

    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            BaseVotingHandler handler
    ) {
        if (parts.length == 3 && parts[2].startsWith("-n=")) {
            String topicName = parts[2].substring(3);
            if (handler.getTopics().containsKey(topicName)) {
                handler.sendResponse(ctx, "Раздел с таким названием уже существует.");
            } else {
                handler.getTopics().put(topicName, new ArrayList<>());
                handler.sendResponse(ctx, "Раздел '" + topicName + "' создан.");
            }
        } else {
            handler.sendResponse(ctx, "Неверный формат команды create topic. Используйте: create topic -n=<topic>");
        }
    }
}
