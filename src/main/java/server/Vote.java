package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Vote {
    private String title;
    private String description;
    private List<String> options;
    private Map<String, Integer> optionVotes;
    private String creator;
    private Map<String, String> userVotes;

    // Конструктор для ручного создания
    public Vote(String title, String description, List<String> options, String creator) {
        this.title = title;
        this.description = description;
        this.options = options;
        this.creator = creator;
        this.optionVotes = new HashMap<>();
        this.userVotes = new HashMap<>();
        options.forEach(option -> optionVotes.put(option, 0));
    }

    // Аннотированный конструктор для Jackson
    @JsonCreator
    public Vote(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("options") List<String> options,
            @JsonProperty("creator") String creator,
            @JsonProperty("optionVotes") Map<String, Integer> optionVotes,
            @JsonProperty("userVotes") Map<String, String> userVotes) {
        this.title = title;
        this.description = description;
        this.options = options;
        this.creator = creator;
        this.optionVotes = optionVotes != null ? optionVotes : new HashMap<>();
        this.userVotes = userVotes != null ? userVotes : new HashMap<>();
    }

    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOptions() {
        return options;
    }

    public Map<String, Integer> getOptionVotes() {
        return optionVotes;
    }

    public String getCreator() {
        return creator;
    }

    public Map<String, String> getUserVotes() {
        return userVotes;
    }

    // Остальные методы
    public boolean addVote(String option, String username) {
        if (!optionVotes.containsKey(option) || userVotes.containsKey(username)) {
            return false;
        }
        optionVotes.put(option, optionVotes.get(option) + 1);
        userVotes.put(username, option);
        return true;
    }

    @Override
    public String toString() {
        return title + " (" + description + "): " + options;
    }
}