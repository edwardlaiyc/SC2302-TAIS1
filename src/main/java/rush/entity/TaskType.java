package rush.entity;

public enum TaskType {
    SEAT("seat customers"),
    TAKE_ORDER("take orders"),
    SERVE("serve dishes"),
    COOK("cook"),
    WAIT("wait");

    private final String activity;

    TaskType(String activity) {
        this.activity = activity;
    }

    public String getActivity() {
        return activity;
    }
}
