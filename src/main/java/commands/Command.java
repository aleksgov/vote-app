package commands;

import io.netty.channel.ChannelHandlerContext;
import server.VotingServerHandler;

import java.net.InetSocketAddress;

public interface Command {
    void execute(
            ChannelHandlerContext ctx,
            String[] parts,
            VotingServerHandler handler,
             InetSocketAddress sender
    );
}
