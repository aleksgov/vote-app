package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import commands.LoginCommand;
import commands.CreateCommand;
import commands.ViewCommand;

import java.util.*;

public class VotingServerHandler extends SimpleChannelInboundHandler<String> {

    public static final Map<String, List<String>> topics = new HashMap<>();
    public static final Map<String, List<Vote>> votesByTopic = new HashMap<>();
    public static final Set<String> loggedUsers = new HashSet<>();

    private String currentUser = null;
    private VoteCreationManager voteCreationManager = null;

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public void setVoteCreationManager(VoteCreationManager manager) {
        this.voteCreationManager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (voteCreationManager != null && voteCreationManager.isActive()) {
            voteCreationManager.processInput(ctx, msg, (vote, topic) -> {
                votesByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(vote);
                ctx.writeAndFlush("Голосование создано: " + vote);
                voteCreationManager = null;
            });
            return;
        }

        String[] parts = msg.split(" ");
        if (parts.length == 0) {
            ctx.writeAndFlush("Пустая команда.");
            return;
        }
        String mainCommand = parts[0];

        switch (mainCommand) {
            case "login":
                LoginCommand.execute(ctx, parts, this);
                break;

            case "create":
                CreateCommand.execute(ctx, parts, this);
                break;

            case "view":
                ViewCommand.execute(ctx, parts);
                break;

            default:
                ctx.writeAndFlush("Неизвестная команда.");
        }
    }
}
