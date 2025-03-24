package server;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Vote {
    private String title;
    private String description;
    private List<String> options;
    private Map<String, Integer> optionVotes;

    public Vote(String title, String description, List<String> options) {
        this.title = title;
        this.description = description;
        this.options = options;
        this.optionVotes = new HashMap<>();

        for (String option : options) {
            optionVotes.put(option, 0);
        }
    }

    // Получение названия голосования
    public String getTitle() {
        return title;
    }

    // Получение описания голосования
    public String getDescription() {
        return description;
    }

    // Получение голосов для каждого варианта
    public Map<String, Integer> getOptionVotes() {
        return optionVotes;
    }

    @Override
    public String toString() {
        return title + " (" + description + "): " + options;
    }
}
