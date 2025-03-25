package commands;

import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.VotingServerHandler;
import static org.mockito.Mockito.*;

class CreateVoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private CreateVoteCommand command;
    private VotingServerHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CreateVoteCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.topics.clear();
    }

    @Test
    void testNonExistingTopic() {
        String[] parts = {"create", "vote", "-t=Economy"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Раздел 'Economy' не найден.");
    }

    @Test
    void testInvalidCommandFormat() {
        String[] parts = {"create", "vote"};
        command.execute(ctx, parts, handler);
        verify(ctx).writeAndFlush("Неверный формат команды. Используйте: create vote -t=\"<topic>\"");
    }
}