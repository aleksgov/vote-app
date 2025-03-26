package commands;

import io.netty.channel.ChannelHandlerContext;
import server.BaseVotingHandler;
import server.Vote;
import java.util.List;

public class VoteCommand implements Command {
    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            BaseVotingHandler handler
    ) {
        if (handler.getCurrentUser() == null) {
            handler.sendResponse(ctx, "Для голосования необходимо войти в систему.");
            return;
        }

        if (parts.length != 3 || !parts[1].startsWith("-t=") || !parts[2].startsWith("-v=")) {
            handler.sendResponse(ctx, "Неверный формат. Используйте: vote -t=<topic> -v=<vote>");
            return;
        }

        String topic = parts[1].substring(3);
        String voteTitle = parts[2].substring(3);
        List<Vote> votes = handler.getVotesByTopic().get(topic);

        if (votes == null) {
            handler.sendResponse(ctx, "Раздел не найден.");
            return;
        }

        Vote targetVote = votes.stream()
                .filter(v -> v.getTitle().equals(voteTitle))
                .findFirst()
                .orElse(null);

        if (targetVote == null) {
            handler.sendResponse(ctx, "Голосование не найдено.");
            return;
        }

        handler.sendResponse(ctx, "Варианты ответа:\n" + String.join("\n", targetVote.getOptions()));
        handler.sendResponse(ctx, "\nВведите вариант ответа: ");

        handler.getPendingVotes().put(handler.getCurrentUser(), targetVote);
    }
}