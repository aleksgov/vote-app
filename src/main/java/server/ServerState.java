package server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerState {
    private Map<String, List<String>> topics;
    private Map<String, List<Vote>> votesByTopic;

    public ServerState() {
    }

    public ServerState(Map<String, List<String>> topics,
                       Map<String, List<Vote>> votesByTopic) {
        this.topics = topics;
        this.votesByTopic = votesByTopic;
    }

    public Map<String, List<String>> getTopics() { return topics; }
    public Map<String, List<Vote>> getVotesByTopic() { return votesByTopic; }

    public void setTopics(Map<String, List<String>> topics) { this.topics = topics; }
    public void setVotesByTopic(Map<String, List<Vote>> votesByTopic) { this.votesByTopic = votesByTopic; }

    public static void saveToFile(String filename, ServerState state) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.writeValue(new File(filename), state);
    }

    public static ServerState loadFromFile(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filename), ServerState.class);
    }
}