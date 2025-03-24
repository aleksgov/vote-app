package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;

import java.util.List;
import java.util.Map;

public class ViewCommand {

    public static void execute(ChannelHandlerContext ctx, String[] parts) {
        if (parts.length == 1) {
            StringBuilder response = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : VotingServerHandler.topics.entrySet()) {
                response.append(entry.getKey())
                        .append(" (голосов=")
                        .append(entry.getValue().size())
                        .append(")\n");
            }
            if (response.length() == 0) {
                ctx.writeAndFlush("Нет доступных разделов.");
            } else {
                ctx.writeAndFlush(response.toString());
            }
        } else if (parts.length == 2 && parts[1].startsWith("-t=")) {
            String requestedTopic = parts[1].substring(3);
            List<String> topicVotes = VotingServerHandler.topics.get(requestedTopic);
            if (topicVotes != null) {
                ctx.writeAndFlush("Раздел '" + requestedTopic + "' - голосов: " + topicVotes);
            } else {
                ctx.writeAndFlush("Раздел не найден.");
            }
        } else {
            ctx.writeAndFlush("Неверный формат команды view. Используйте: view -t=<topic>");
        }
    }
}
