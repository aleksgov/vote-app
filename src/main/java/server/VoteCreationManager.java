package server;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class VoteCreationManager {
    private int step;
    private String voteTitle;
    private String voteDescription;
    private int voteOptionCount;
    private int currentOptionCount;
    private List<String> voteOptions;
    private final String topic;
    private final String creator;

    public VoteCreationManager(String topic, String creator) {
        this.topic = topic;
        this.creator = creator;
        this.step = 1;
        this.voteOptions = new ArrayList<>();
    }

    public void processInput(ChannelHandlerContext ctx, String msg, VoteCreationCallback callback) {
        switch (step) {
            case 1:
                voteTitle = msg.trim();

                if (VotingServerHandler.topics.get(topic).contains(voteTitle)) {
                    ctx.writeAndFlush("Голосование с названием '" + voteTitle + "' уже существует!");
                    reset();
                    return;
                }

                step = 2;
                ctx.writeAndFlush("Введите тему голосования (описание):");
                break;
            case 2:
                voteDescription = msg.trim();
                step = 3;
                ctx.writeAndFlush("Введите количество вариантов ответа:");
                break;
            case 3:
                try {
                    voteOptionCount = Integer.parseInt(msg.trim());
                    if (voteOptionCount <= 0) {
                        ctx.writeAndFlush("Количество вариантов должно быть положительным числом. Введите количество вариантов ответа:");
                        return;
                    }
                } catch (NumberFormatException e) {
                    ctx.writeAndFlush("Неверный формат числа. Введите количество вариантов ответа:");
                    return;
                }
                currentOptionCount = 0;
                step = 4;
                ctx.writeAndFlush("Введите вариант ответа #1:");
                break;
            default:
                voteOptions.add(msg.trim());
                currentOptionCount++;
                if (currentOptionCount < voteOptionCount) {
                    ctx.writeAndFlush("Введите вариант ответа #" + (currentOptionCount + 1) + ":");
                } else {

                    Vote newVote = new Vote(voteTitle, voteDescription, voteOptions, creator);
                    callback.onVoteCreated(newVote, topic);
                    reset();
                }
                break;
        }
    }

    public boolean isActive() {
        return step > 0;
    }

    private void reset() {
        step = 0;
        voteTitle = null;
        voteDescription = null;
        voteOptionCount = 0;
        currentOptionCount = 0;
        voteOptions = null;
    }

    public interface VoteCreationCallback {
        void onVoteCreated(Vote vote, String topic);
    }
}
