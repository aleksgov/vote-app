package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.Vote;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class ViewCommand implements Command {

    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            VotingServerHandler handler,
            InetSocketAddress sender
    ) {
        if (parts.length == 1) {
            StringBuilder response = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : VotingServerHandler.topics.entrySet()) {
                response.append(entry.getKey())
                        .append(" (голосов=")
                        .append(entry.getValue().size())
                        .append(")\n");
            }
            if (response.isEmpty()) {
                handler.sendResponse(ctx, sender, "Нет доступных разделов.");
            } else {
                handler.sendResponse(ctx, sender, response.toString());
            }
        } else if (parts.length == 2 && parts[1].startsWith("-t=")) {
            String requestedTopic = parts[1].substring(3);
            List<String> topicVotes = VotingServerHandler.topics.get(requestedTopic);
            if (topicVotes != null) {
                StringBuilder votesList = new StringBuilder();
                topicVotes.forEach(vote -> votesList.append(vote).append("\n"));
                handler.sendResponse(ctx, sender, "Раздел '" + requestedTopic + "'\nГолосования:\n" + votesList);
            } else {
                handler.sendResponse(ctx, sender, "Раздел не найден.");
            }
        } else if (parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")) {
            String requestedTopic = parts[1].substring(3);
            String requestedVote = parts[2].substring(3);
            List<Vote> votes = VotingServerHandler.votesByTopic.get(requestedTopic);
            if (votes == null) {
                handler.sendResponse(ctx, sender, "Тема '" + requestedTopic + "' не найдена.");
                return;
            }
            Vote foundVote = null;
            for (Vote vote : votes) {
                if (vote.getTitle().equals(requestedVote)) {
                    foundVote = vote;
                    break;
                }
            }
            if (foundVote == null) {
                handler.sendResponse(ctx, sender, "Голосование '" + requestedVote + "' не найдено в теме '" + requestedTopic + "'.");
            } else {
                StringBuilder response = new StringBuilder();
                response.append("Тема голосования: ").append(foundVote.getTitle()).append("\n");
                response.append("Описание голосования: ").append(foundVote.getDescription()).append("\n");
                response.append("Варианты ответа и количество голосов:\n");
                Map<String, Integer> optionVotes = foundVote.getOptionVotes();
                for (Map.Entry<String, Integer> entry : optionVotes.entrySet()) {
                    response.append(entry.getKey()).append(" - ").append(entry.getValue()).append(" голосов\n");
                }
                handler.sendResponse(ctx, sender, response.toString());
            }
        } else {
            handler.sendResponse(ctx, sender, "Неверный формат команды view. Используйте: view -t=<topic> или view -t=<topic> -v=<vote>");
        }
    }
}