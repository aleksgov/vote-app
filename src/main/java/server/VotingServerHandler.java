package server;

import commands.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VotingServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    public static volatile Map<String, List<String>> topics = new ConcurrentHashMap<>();
    public static volatile Map<String, List<Vote>> votesByTopic = new ConcurrentHashMap<>();
    public static volatile Set<String> loggedUsers = ConcurrentHashMap.newKeySet();

    private String currentUser = null;
    private VoteCreationManager voteCreationManager = null;
    private final Map<String, Command> commandMap = new HashMap<>();
    private final Map<String, Vote> pendingVotes = new ConcurrentHashMap<>();

    public Map<String, Vote> getPendingVotes() {
        return pendingVotes;
    }

    public VotingServerHandler() {
        commandMap.put("login", new LoginCommand());
        commandMap.put("view", new ViewCommand());
        commandMap.put("create topic", new CreateTopicCommand());
        commandMap.put("create vote", new CreateVoteCommand());
        commandMap.put("delete", new DeleteVoteCommand());
        commandMap.put("vote", new VoteCommand());
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public void setVoteCreationManager(VoteCreationManager manager) {
        this.voteCreationManager = manager;
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

    public void sendResponse(ChannelHandlerContext ctx, InetSocketAddress recipient, String message) {
        ctx.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                recipient
        ));
    }

    private void handleExitCommand(ChannelHandlerContext ctx, InetSocketAddress sender) {
        if (currentUser != null) {
            loggedUsers.remove(currentUser);
            currentUser = null;
        }
        sendResponse(ctx, sender, "Завершение работы программы.");
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
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        String msg = packet.content().toString(CharsetUtil.UTF_8);
        InetSocketAddress sender = packet.sender();

        if (voteCreationManager != null && voteCreationManager.isActive()) {
            voteCreationManager.processInput(ctx, msg, (vote, topic) -> {
                votesByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(vote);
                topics.computeIfPresent(topic, (k, v) -> {
                    v.add(vote.getTitle());
                    return v;
                });
                sendResponse(ctx, sender, "Голосование создано: " + vote);
                voteCreationManager = null;
            });
            return;
        }

        if (currentUser != null && pendingVotes.containsKey(currentUser)) {
            Vote pendingVote = pendingVotes.get(currentUser);
            if (pendingVote.getOptionVotes().containsKey(msg)) {
                pendingVote.addVote(msg, currentUser);
                sendResponse(ctx, sender, "Ваш голос учтен.");
                pendingVotes.remove(currentUser);
            } else {
                sendResponse(ctx, sender, "Неизвестная команда.");
            }
            return;
        }

        String[] parts = parseTokens(msg);

        if (parts.length == 0) {
            sendResponse(ctx, sender, "Пустая команда.");
            return;
        }
        String mainCommand = parts[0].toLowerCase();

        if (!mainCommand.equals("login") && !mainCommand.equals("exit") && currentUser == null) {
            sendResponse(ctx, sender, "Ошибка: требуется авторизация. Используйте login -u=<username>");
            return;
        }

        if (mainCommand.equals("exit")) {
            handleExitCommand(ctx, sender);
            return;
        }

        if (mainCommand.equals("vote")) {
            if (parts.length != 3 || !parts[1].startsWith("-t=") || !parts[2].startsWith("-v=")) {
                sendResponse(ctx, sender, "Неверный формат. Используйте: vote -t=<topic> -v=<vote>");
                return;
            }
            commandMap.get("vote").execute(ctx, parts, this, sender);
            return;
        }

        if (mainCommand.equals("create")) {
            if (parts.length >= 2) {
                String subCommandType = parts[1].toLowerCase();
                Command command = commandMap.get("create " + subCommandType);
                if (command != null) {
                    command.execute(ctx, parts, this, sender);
                } else {
                    sendResponse(ctx, sender, "Неверная команда create. Используйте: create topic или create vote");
                }
            } else {
                sendResponse(ctx, sender, "Недостаточно аргументов. Используйте: create topic или create vote");
            }
        } else {
            Command command = commandMap.get(mainCommand);
            if (command != null) {
                command.execute(ctx, parts, this, sender);
            } else {
                sendResponse(ctx, sender, "Неизвестная команда.");
            }
        }
    }
}