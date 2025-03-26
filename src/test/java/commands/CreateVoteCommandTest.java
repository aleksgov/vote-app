package commands;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.VotingServerHandler;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class CreateVoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private CreateVoteCommand command;
    private VotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CreateVoteCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.topics.clear();
        sender = new InetSocketAddress("127.0.0.1", 8080);
    }

    @Test
    void testNonExistingTopic() {
        String[] parts = {"create", "vote", "-t=Economy"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        DatagramPacket packet = captor.getValue();
        String actualMessage = packet.content().toString(CharsetUtil.UTF_8);
        assertEquals("Раздел 'Economy' не найден.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        String[] parts = {"create", "vote"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        DatagramPacket packet = captor.getValue();
        String actualMessage = packet.content().toString(CharsetUtil.UTF_8);
        assertEquals("Неверный формат команды. Используйте: create vote -t=\"<topic>\"", actualMessage);
    }
}
