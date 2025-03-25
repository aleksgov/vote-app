package commands;

import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.Vote;
import server.VotingServerHandler;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;

class VoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private VoteCommand command;
    private VotingServerHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new VoteCommand();
        handler = new VotingServerHandler();
        handler.setCurrentUser("user1");

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote("vote1", "desc", List.of("Yes", "No"), "admin"));
        VotingServerHandler.votesByTopic.put("politics", votes);
    }

    @Test
    void testVoteWithoutLogin() {
        handler.setCurrentUser(null);
        String[] parts = {"vote", "-t=politics", "-v=vote1"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Для голосования необходимо войти в систему.");
    }

    @Test
    void testInvalidCommandFormat() {
        String[] parts = {"vote", "invalid"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Неверный формат. Используйте: vote -t=<topic> -v=<vote>");
    }

    @Test
    void testTopicNotFound() {
        String[] parts = {"vote", "-t=invalid", "-v=vote1"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Раздел не найден.");
    }
}