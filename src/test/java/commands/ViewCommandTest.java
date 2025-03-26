package commands;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.Vote;
import server.VotingServerHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class ViewCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private ViewCommand command;
    private VotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new ViewCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.topics.clear();
        VotingServerHandler.votesByTopic.clear();

        sender = new InetSocketAddress("127.0.0.1", 8080);

        List<String> politicsVotes = new ArrayList<>();
        politicsVotes.add("Elections");
        VotingServerHandler.topics.put("Politics", politicsVotes);

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote("Elections", "Presidential elections", List.of("Candidate A", "Candidate B"), "admin"));
        VotingServerHandler.votesByTopic.put("Politics", votes);
    }

    @Test
    void testViewAllTopics() {
        String[] parts = {"view"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Politics (голосов=1)\n", actualMessage);
    }

    @Test
    void testViewEmptyTopics() {
        VotingServerHandler.topics.clear();
        String[] parts = {"view"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Нет доступных разделов.", actualMessage);
    }

    @Test
    void testViewSpecificTopic() {
        String[] parts = {"view", "-t=Politics"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Раздел 'Politics'\nГолосования:\nElections\n", actualMessage);
    }

    @Test
    void testViewNonExistingTopic() {
        String[] parts = {"view", "-t=Sports"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Раздел не найден.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        String[] parts = {"view", "-invalid"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Неверный формат команды view. Используйте: view -t=<topic> или view -t=<topic> -v=<vote>", actualMessage);
    }
}
