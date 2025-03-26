package server;

import commands.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcpVotingServerHandler extends SimpleChannelInboundHandler<String> implements BaseVotingHandler {

    public static final Map<String, List<String>> topics = new ConcurrentHashMap<>();
    public static final Map<String, List<Vote>> votesByTopic = new ConcurrentHashMap<>();
    public static final Set<String> loggedUsers = ConcurrentHashMap.newKeySet();

    private String currentUser = null;
    private VoteCreationManager voteCreationManager = null;
    private final Map<String, Command> commandMap = new HashMap<>();
    private final Map<String, Vote> pendingVotes = new ConcurrentHashMap<>();

    public TcpVotingServerHandler() {
        commandMap.put("login", new LoginCommand());
        commandMap.put("view", new ViewCommand());
        commandMap.put("create topic", new CreateTopicCommand());
        commandMap.put("create vote", new CreateVoteCommand());
        commandMap.put("delete", new DeleteVoteCommand());
        commandMap.put("vote", new VoteCommand());
    }

    @Override
    public Map<String, List<String>> getTopics() {
        return topics;
    }

    @Override
    public Map<String, List<Vote>> getVotesByTopic() {
        return votesByTopic;
    }

    @Override
    public Map<String, Vote> getPendingVotes() {
        return pendingVotes;
    }

    @Override
    public String getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void setVoteCreationManager(VoteCreationManager manager) {
        this.voteCreationManager = manager;
    }

    @Override
    public void sendResponse(ChannelHandlerContext ctx, String message) {
        ctx.writeAndFlush(message + "\n");
    }

    private String[] parseTokens(String msg) {
        Pattern pattern = Pattern.compile("(\\S+=\"[^\"]+\"|\\S+)");
        Matcher matcher = pattern.matcher(msg);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).matches(".*=\".*\"")) {
                int idx = tokens.get(i).indexOf("=");
                String key = tokens.get(i).substring(0, idx + 1);
                String value = tokens.get(i).substring(idx + 1);
                value = value.replaceAll("^\"|\"$", "");
                tokens.set(i, key + value);
            }
        }
        return tokens.toArray(new String[0]);
    }

    private void handleExitCommand(ChannelHandlerContext ctx) {
        if (currentUser != null) {
            loggedUsers.remove(currentUser);
            currentUser = null;
        }
        sendResponse(ctx, "Завершение работы программы.");
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (currentUser != null) {
            loggedUsers.remove(currentUser);
            currentUser = null;
        }
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        msg = msg.trim();

        if (voteCreationManager != null && voteCreationManager.isActive()) {
            voteCreationManager.processInput(ctx, msg, (vote, topic) -> {
                votesByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(vote);
                topics.computeIfPresent(topic, (k, v) -> {
                    v.add(vote.getTitle());
                    return v;
                });
                sendResponse(ctx, "Голосование создано: " + vote);
                voteCreationManager = null;
            });
            return;
        }

        if (currentUser != null && pendingVotes.containsKey(currentUser)) {
            Vote pendingVote = pendingVotes.get(currentUser);
            if (pendingVote.getOptionVotes().containsKey(msg)) {
                pendingVote.addVote(msg, currentUser);
                sendResponse(ctx, "Ваш голос учтен.");
                pendingVotes.remove(currentUser);
            } else {
                sendResponse(ctx, "Неизвестная команда.");
            }
            return;
        }

        String[] parts = parseTokens(msg);

        if (parts.length == 0) {
            sendResponse(ctx, "Пустая команда.");
            return;
        }
        String mainCommand = parts[0].toLowerCase();

        if (!mainCommand.equals("login") && !mainCommand.equals("exit") && currentUser == null) {
            sendResponse(ctx, "Ошибка: требуется авторизация. Используйте login -u=<username>");
            return;
        }

        switch (mainCommand) {
            case "exit" -> handleExitCommand(ctx);
            case "vote" -> {
                if (parts.length != 3 || !parts[1].startsWith("-t=") || !parts[2].startsWith("-v=")) {
                    sendResponse(ctx, "Неверный формат. Используйте: vote -t=<topic> -v=<vote>");
                    return;
                }
                commandMap.get("vote").execute(ctx, parts, this);
            }
            case "create" -> {
                if (parts.length >= 2) {
                    String subCommandType = parts[1].toLowerCase();
                    Command command = commandMap.get("create " + subCommandType);
                    if (command != null) {
                        command.execute(ctx, parts, this);
                    } else {
                        sendResponse(ctx, "Неверная команда create. Используйте: create topic или create vote");
                    }
                } else {
                    sendResponse(ctx, "Недостаточно аргументов. Используйте: create topic или create vote");
                }
            }
            default -> {
                Command command = commandMap.get(mainCommand);
                if (command != null) {
                    command.execute(ctx, parts, this);
                } else {
                    sendResponse(ctx, "Неизвестная команда.");
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}