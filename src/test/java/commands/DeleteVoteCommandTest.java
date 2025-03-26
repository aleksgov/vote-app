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
import server.UpdVotingServerHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class DeleteVoteCommandTest {
    @Mock
    private ChannelHandlerContext ctx;

    private DeleteVoteCommand command;
    private UpdVotingServerHandler handler;
    private InetSocketAddress sender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new DeleteVoteCommand();
        handler = new UpdVotingServerHandler();
        handler.lastSender = new InetSocketAddress("127.0.0.1", 8080); // Установка lastSender
        UpdVotingServerHandler.votesByTopic.clear();

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote("testVote", "description", List.of("Yes", "No"), "admin"));
        UpdVotingServerHandler.votesByTopic.put("politics", votes);
    }

    @Test
    void testDeleteVoteSuccess() {
        handler.setCurrentUser("admin");
        String[] validCommand = {"delete", "-t=politics", "-v=testVote"};
        command.execute(ctx, validCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Голосование \"testVote\" удалено из темы \"politics\".", actualMessage);
    }

    @Test
    void testDeleteNonExistingTopic() {
        handler.setCurrentUser("admin"); // Добавлено
        String[] invalidCommand = {"delete", "-t=fake", "-v=testVote"};
        command.execute(ctx, invalidCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Тема \"fake\" не найдена.", actualMessage);
    }

    @Test
    void testDeleteWithoutLogin() {
        String[] validCommand = {"delete", "-t=politics", "-v=testVote"};
        command.execute(ctx, validCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Вы должны войти в систему, чтобы удалить голосование.", actualMessage);
    }

    @Test
    void testDeleteNotOwner() {
        handler.setCurrentUser("user");
        String[] validCommand = {"delete", "-t=politics", "-v=testVote"};
        command.execute(ctx, validCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Голосование не найдено или у вас нет прав на его удаление.", actualMessage);
    }

    @Test
    void testInvalidCommandFormat() {
        String[] invalidCommand = {"delete", "wrong_format"};
        command.execute(ctx, invalidCommand, handler);

        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(ctx).writeAndFlush(captor.capture());
        String actualMessage = captor.getValue().content().toString(CharsetUtil.UTF_8);
        assertEquals("Неверный формат команды delete. Используйте: delete -t=<topic> -v=<vote>", actualMessage);
    }
}