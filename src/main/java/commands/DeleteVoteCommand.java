package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.Vote;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class DeleteVoteCommand implements Command {

    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            VotingServerHandler handler,
            InetSocketAddress sender
    ) {
        if (parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")) {
            String topic = parts[1].substring(3);
            String voteTitle = parts[2].substring(3);

            Map<String, List<Vote>> votesByTopic = VotingServerHandler.votesByTopic;
            List<Vote> votes = votesByTopic.get(topic);

            if (votes == null) {
                handler.sendResponse(ctx, sender, "Тема \"" + topic + "\" не найдена.");
                return;
            }

            String currentUser = handler.getCurrentUser();
            if (currentUser == null) {
                handler.sendResponse(ctx, sender, "Вы должны войти в систему, чтобы удалить голосование.");
                return;
            }

            Vote voteToRemove = null;
            for (Vote vote : votes) {
                if (vote.getTitle().equals(voteTitle) && vote.getCreator().equals(currentUser)) {
                    voteToRemove = vote;
                    break;
                }
            }

            if (voteToRemove != null) {
                votes.remove(voteToRemove);

                List<String> topicVoteTitles = VotingServerHandler.topics.get(topic);
                if (topicVoteTitles != null) {
                    topicVoteTitles.removeIf(title -> title.equalsIgnoreCase(voteTitle));
                }
                handler.sendResponse(ctx, sender, "Голосование \"" + voteTitle + "\" удалено из темы \"" + topic + "\".");
            } else {
                handler.sendResponse(ctx, sender, "Голосование не найдено или у вас нет прав на его удаление.");
            }
        } else {
            handler.sendResponse(ctx, sender, "Неверный формат команды delete. Используйте: delete -t=topic -v=<vote>");
        }
    }
}