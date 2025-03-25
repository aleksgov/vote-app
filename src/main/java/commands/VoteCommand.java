package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.Vote;
import java.net.InetSocketAddress;
import java.util.List;

public class VoteCommand implements Command {
    @Override
    public void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            VotingServerHandler handler,
            InetSocketAddress sender
    ) {
        if (handler.getCurrentUser() == null) {
            handler.sendResponse(ctx, sender, "Для голосования необходимо войти в систему.");
            return;
        }

        if (parts.length != 3 || !parts[1].startsWith("-t=") || !parts[2].startsWith("-v=")) {
            handler.sendResponse(ctx, sender, "Неверный формат. Используйте: vote -t=<topic> -v=<vote>");
            return;
        }

        String topic = parts[1].substring(3);
        String voteTitle = parts[2].substring(3);
        List<Vote> votes = VotingServerHandler.votesByTopic.get(topic);

        if (votes == null) {
            handler.sendResponse(ctx, sender, "Раздел не найден.");
            return;
        }

        Vote targetVote = votes.stream()
                .filter(v -> v.getTitle().equals(voteTitle))
                .findFirst()
                .orElse(null);

        if (targetVote == null) {
            handler.sendResponse(ctx, sender, "Голосование не найдено.");
            return;
        }

        handler.sendResponse(ctx, sender, "Варианты ответа:\n" + String.join("\n", targetVote.getOptions()));
        handler.sendResponse(ctx, sender, "\nВведите вариант ответа: ");

        handler.getPendingVotes().put(handler.getCurrentUser(), targetVote);
    }
}