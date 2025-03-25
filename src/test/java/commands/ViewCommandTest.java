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

class ViewCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private ViewCommand command;
    private VotingServerHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new ViewCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.topics.clear();
        VotingServerHandler.votesByTopic.clear();

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
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Politics (голосов=1)\n");
    }

    @Test
    void testViewEmptyTopics() {
        VotingServerHandler.topics.clear();
        String[] parts = {"view"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Нет доступных разделов.");
    }

    @Test
    void testViewSpecificTopic() {
        String[] parts = {"view", "-t=Politics"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Раздел 'Politics'\nГолосования:\nElections\n");
    }

    @Test
    void testViewNonExistingTopic() {
        String[] parts = {"view", "-t=Sports"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Раздел не найден.");
    }

    @Test
    void testInvalidCommandFormat() {
        String[] parts = {"view", "-invalid"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Неверный формат команды view. Используйте: view -t=<topic> или view -t=<topic> -v=<vote>");
    }
}