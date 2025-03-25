package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ServerStateTest {
    @Test
    void testSaveAndLoad() throws Exception {

        Map<String, List<String>> topics = new HashMap<>();
        topics.put("Politics", List.of("Vote1", "Vote2"));

        Map<String, List<Vote>> votes = new HashMap<>();
        votes.put("Politics", List.of(
                new Vote("Vote1", "Desc1", List.of("Yes", "No"), "user1")
        ));

        ServerState original = new ServerState(topics, votes);
        File tempFile = File.createTempFile("test-state", ".json");

        ServerState.saveToFile(tempFile.getAbsolutePath(), original);

        ServerState loaded = ServerState.loadFromFile(tempFile.getAbsolutePath());

        assertEquals(original.getTopics(), loaded.getTopics());
        assertEquals(1, loaded.getVotesByTopic().get("Politics").size());
    }
}