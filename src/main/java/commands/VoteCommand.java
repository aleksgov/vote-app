package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;
import server.Vote;
import java.util.List;

public class VoteCommand implements Command {
    @Override
    public void execute(ChannelHandlerContext ctx, String[] parts, VotingServerHandler handler) {
        if (handler.getCurrentUser() == null) {
            ctx.writeAndFlush("Для голосования необходимо войти в систему.");
            return;
        }

        if (parts.length != 3 || !parts[1].startsWith("-t=") || !parts[2].startsWith("-v=")) {
            ctx.writeAndFlush("Неверный формат. Используйте: vote -t=<topic> -v=<vote>");
            return;
        }

        String topic = parts[1].substring(3);
        String voteTitle = parts[2].substring(3);
        List<Vote> votes = VotingServerHandler.votesByTopic.get(topic);

        if (votes == null) {
            ctx.writeAndFlush("Раздел не найден.");
            return;
        }

        Vote targetVote = votes.stream()
                .filter(v -> v.getTitle().equals(voteTitle))
                .findFirst()
                .orElse(null);

        if (targetVote == null) {
            ctx.writeAndFlush("Голосование не найдено.");
            return;
        }

        ctx.writeAndFlush("Варианты ответа:\n" + String.join("\n", targetVote.getOptionVotes().keySet()));
        ctx.writeAndFlush("\nВведите номер варианта: ");

        handler.getPendingVotes().put(handler.getCurrentUser(), targetVote);
    }
}