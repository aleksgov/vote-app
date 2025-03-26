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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class CreateTopicCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private CreateTopicCommand command;
    private VotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CreateTopicCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.topics.clear();
        sender = new InetSocketAddress("127.0.0.1", 8080);
    }

    @Test
    void testCreateNewTopicSuccess() {
        String[] validCommand = {"create", "topic", "-n=politics"};
        command.execute(ctx, validCommand, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        DatagramPacket packet = captor.getValue();
        String actualMessage = packet.content().toString(CharsetUtil.UTF_8);

        assertEquals("Раздел 'politics' создан.", actualMessage);
    }

    @Test
    void testCreateExistingTopic() {
        VotingServerHandler.topics.put("politics", new ArrayList<>());
        String[] commandWithDup = {"create", "topic", "-n=politics"};
        command.execute(ctx, commandWithDup, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        DatagramPacket packet = captor.getValue();
        String actualMessage = packet.content().toString(CharsetUtil.UTF_8);

        assertEquals("Раздел с таким названием уже существует.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        String[] invalidCommand = {"create", "topic"};
        command.execute(ctx, invalidCommand, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        DatagramPacket packet = captor.getValue();
        String actualMessage = packet.content().toString(CharsetUtil.UTF_8);

        assertEquals("Неверный формат команды create topic. Используйте: create topic -n=<topic>", actualMessage);
    }
}
