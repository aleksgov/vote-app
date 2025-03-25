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

class DeleteVoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private DeleteVoteCommand command;
    private VotingServerHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new DeleteVoteCommand();
        handler = new VotingServerHandler();
        VotingServerHandler.votesByTopic.clear();

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote("testVote", "description", List.of("Yes", "No"), "admin"));
        VotingServerHandler.votesByTopic.put("politics", votes);
    }

    @Test
    void testDeleteVoteSuccess() {
        handler.setCurrentUser("admin");
        String[] validCommand = {"delete", "-t=politics", "-v=testVote"};
        command.execute(ctx, validCommand, handler);
        verify(ctx).writeAndFlush("Голосование \"testVote\" удалено из темы \"politics\".");
    }

    @Test
    void testDeleteNonExistingTopic() {
        String[] invalidCommand = {"delete", "-t=fake", "-v=testVote"};
        command.execute(ctx, invalidCommand, handler);
        verify(ctx).writeAndFlush("Тема \"fake\" не найдена.");
    }

    @Test
    void testDeleteWithoutLogin() {
        handler.setCurrentUser(null);
        String[] validCommand = {"delete", "-t=politics", "-v=testVote"};
        command.execute(ctx, validCommand, handler);
        verify(ctx).writeAndFlush("Вы должны войти в систему, чтобы удалить голосование.");
    }

    @Test
    void testDeleteNotOwner() {
        handler.setCurrentUser("user");
        String[] validCommand = {"delete", "-t=politics", "-v=testVote"};
        command.execute(ctx, validCommand, handler);
        verify(ctx).writeAndFlush("Голосование не найдено или у вас нет прав на его удаление.");
    }

    @Test
    void testInvalidCommandFormat() {
        String[] invalidCommand = {"delete", "wrong_format"};
        command.execute(ctx, invalidCommand, handler);
        verify(ctx).writeAndFlush("Неверный формат команды delete. Используйте: delete -t=topic -v=<vote>");
    }
}