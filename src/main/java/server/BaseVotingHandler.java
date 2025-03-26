package server;

import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Map;

public interface BaseVotingHandler {
    void setCurrentUser(String currentUser);
    String getCurrentUser();
    Map<String, Vote> getPendingVotes();
    void setVoteCreationManager(VoteCreationManager manager);
    void sendResponse(ChannelHandlerContext ctx, String message);

    Map<String, List<String>> getTopics();
    Map<String, List<Vote>> getVotesByTopic();
}
