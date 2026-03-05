package hcmute.edu.vn.ticktickandroid.Task;

public class Task {
    private String title;
    private String group;
    private boolean completed;

    public Task(String title, String group) {
        this.title = title;
        this.group = group;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }
    public String getGroup() {
        return group;
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
