package model;

public class RunnerTime {

    private final String id;
    private final String time;

    public RunnerTime(String id, String time) {
        this.id = id;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }
}
