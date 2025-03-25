package commands;

import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import server.VotingServerHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class LoginCommandTest {
    @Test
    void testLoginSuccess() {
        LoginCommand command = new LoginCommand();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        VotingServerHandler handler = new VotingServerHandler();
        String[] parts = {"login", "-u=alice"};

        command.execute(ctx, parts, handler);

        verify(ctx).writeAndFlush("Выполнен вход как alice");
        assertEquals("alice", handler.getCurrentUser());
    }
}