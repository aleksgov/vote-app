package commands;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import server.UpdVotingServerHandler;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LoginCommandTest {
    @Test
    void testLoginSuccess() {
        LoginCommand command = new LoginCommand();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        UpdVotingServerHandler handler = new UpdVotingServerHandler();
        handler.lastSender = new InetSocketAddress("localhost", 8080);

        String[] parts = {"login", "-u=alice"};

        command.execute(ctx, parts, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());

        DatagramPacket packet = captor.getValue();
        String actualMessage = packet.content().toString(CharsetUtil.UTF_8);
        assertEquals("Выполнен вход как alice", actualMessage);
        assertEquals("alice", handler.getCurrentUser());
    }
}