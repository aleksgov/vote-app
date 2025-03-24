package server;

import java.util.List;

public class Vote {
    private String title;
    private String description;
    private List<String> options;

    public Vote(String title, String description, List<String> options) {
        this.title = title;
        this.description = description;
        this.options = options;
    }

    @Override
    public String toString() {
        return title + " (" + description + "): " + options;
    }

}
