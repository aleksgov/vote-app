package commands;

import io.netty.channel.ChannelHandlerContext;
import server.BaseVotingHandler;

public interface Command {
    void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            BaseVotingHandler handler
    );
}
