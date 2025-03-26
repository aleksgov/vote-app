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

class VoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private VoteCommand command;
    private VotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new VoteCommand();
        handler = new VotingServerHandler();
        handler.setCurrentUser("user1");

        sender = new InetSocketAddress("127.0.0.1", 8080);

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote("vote1", "desc", List.of("Yes", "No"), "admin"));
        VotingServerHandler.votesByTopic.put("politics", votes);
    }

    @Test
    void testVoteWithoutLogin() {
        handler.setCurrentUser(null);
        String[] parts = {"vote", "-t=politics", "-v=vote1"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Для голосования необходимо войти в систему.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        String[] parts = {"vote", "invalid"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Неверный формат. Используйте: vote -t=<topic> -v=<vote>", actualMessage);
    }

    @Test
    void testTopicNotFound() {
        String[] parts = {"vote", "-t=invalid", "-v=vote1"};
        command.execute(ctx, parts, handler, sender);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);

        assertEquals("Раздел не найден.", actualMessage);
    }
}
