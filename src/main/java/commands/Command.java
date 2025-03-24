package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;

public interface Command {
    void execute(ChannelHandlerContext ctx, String[] parts, VotingServerHandler handler);
}
