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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class CreateTopicCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private CreateTopicCommand command;
    private UpdVotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CreateTopicCommand();
        handler = new UpdVotingServerHandler();
        handler.lastSender = new InetSocketAddress("127.0.0.1", 8080);
        UpdVotingServerHandler.topics.clear();
    }

    @Test
    void testCreateNewTopicSuccess() {
        handler.setCurrentUser("admin");
        String[] validCommand = {"create", "topic", "-n=politics"};
        command.execute(ctx, validCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Раздел 'politics' создан.", actualMessage);
    }

    @Test
    void testCreateExistingTopic() {
        UpdVotingServerHandler.topics.put("politics", new ArrayList<>());
        handler.setCurrentUser("admin");
        String[] commandWithDup = {"create", "topic", "-n=politics"};
        command.execute(ctx, commandWithDup, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Раздел с таким названием уже существует.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        handler.setCurrentUser("admin");
        String[] invalidCommand = {"create", "topic"};
        command.execute(ctx, invalidCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Неверный формат команды create topic. Используйте: create topic -n=<topic>", actualMessage);
    }
}