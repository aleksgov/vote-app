package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import commands.Command;
import commands.CreateTopicCommand;
import commands.CreateVoteCommand;
import commands.ViewCommand;
import commands.LoginCommand;

import java.util.*;

public class VotingServerHandler extends SimpleChannelInboundHandler<String> {

    public static final Map<String, List<String>> topics = new HashMap<>();
    public static final Map<String, List<Vote>> votesByTopic = new HashMap<>();
    public static final Set<String> loggedUsers = new HashSet<>();

    private String currentUser = null;
    private VoteCreationManager voteCreationManager = null;

    private Map<String, Command> commandMap = new HashMap<>();

    public VotingServerHandler() {
        commandMap.put("login", new LoginCommand());
        commandMap.put("view", new ViewCommand());
        commandMap.put("create topic", new CreateTopicCommand());
        commandMap.put("create vote", new CreateVoteCommand());
    }

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

        if (mainCommand.equals("create")) {
            if (parts.length >= 2) {
                String subCommandType = parts[1];
                Command command = commandMap.get("create " + subCommandType);
                if (command != null) {
                    command.execute(ctx, parts, this);
                } else {
                    ctx.writeAndFlush("Неверная команда create. Используйте: create topic или create vote");
                }
            }
        } else {
            Command command = commandMap.get(mainCommand);
            if (command != null) {
                command.execute(ctx, parts, this);
            } else {
                ctx.writeAndFlush("Неизвестная команда.");
            }
        }
    }
}