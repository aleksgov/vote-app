package commands;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.UpdVotingServerHandler;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class CreateVoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private CreateVoteCommand command;
    private UpdVotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CreateVoteCommand();
        handler = new UpdVotingServerHandler();
        handler.lastSender = new InetSocketAddress("127.0.0.1", 8080); // Установка lastSender
        UpdVotingServerHandler.topics.clear();
    }

    @Test
    void testNonExistingTopic() {
        handler.setCurrentUser("user");
        String[] parts = {"create", "vote", "-t=Economy"};
        command.execute(ctx, parts, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Раздел 'Economy' не найден.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        handler.setCurrentUser("user");
        String[] parts = {"create", "vote"};
        command.execute(ctx, parts, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Неверный формат команды. Используйте: create vote -t=\"<topic>\"", actualMessage);
    }
}