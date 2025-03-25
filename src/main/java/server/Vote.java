package server;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Vote {
    private String title;
    private String description;
    private List<String> options;
    private Map<String, Integer> optionVotes;
    private String creator;
    private Map<String, String> userVotes; // Хранит, кто за что проголосовал

    public Vote(String title, String description, List<String> options, String creator) {
        this.title = title;
        this.description = description;
        this.options = options;
        this.creator = creator;
        this.optionVotes = new HashMap<>();
        this.userVotes = new HashMap<>();

        for (String option : options) {
            optionVotes.put(option, 0);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getOptionVotes() {
        return optionVotes;
    }

    public String getCreator() {
        return creator;
    }

    public boolean addVote(String option, String username) {
        if (!optionVotes.containsKey(option)) {
            return false;
        }
        if (userVotes.containsKey(username)) {
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
