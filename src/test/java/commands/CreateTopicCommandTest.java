package commands;

import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.VotingServerHandler;
import java.util.ArrayList;
import static org.mockito.Mockito.*;

class CreateTopicCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private CreateTopicCommand command;
    private VotingServerHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CreateTopicCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.topics.clear();
    }

    @Test
    void testCreateNewTopicSuccess() {
        String[] validCommand = {"create", "topic", "-n=politics"};
        command.execute(ctx, validCommand, handler);
        verify(ctx).writeAndFlush("Раздел 'politics' создан.");
    }

    @Test
    void testCreateExistingTopic() {
        VotingServerHandler.topics.put("politics", new ArrayList<>());
        String[] commandWithDup = {"create", "topic", "-n=politics"};
        command.execute(ctx, commandWithDup, handler);
        verify(ctx).writeAndFlush("Раздел с таким названием уже существует.");
    }

    @Test
    void testInvalidCommandFormat() {
        String[] invalidCommand = {"create", "topic"};
        command.execute(ctx, invalidCommand, handler);
        verify(ctx).writeAndFlush("Неверный формат команды create topic. Используйте: create topic -n=<topic>");
    }
}