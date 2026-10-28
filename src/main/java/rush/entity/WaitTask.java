package rush.entity;

public class WaitTask extends Task {

    @Override
    public TaskType getType() {
        return TaskType.WAIT;
    }

    @Override
    public void check(Restaurant restaurant, int turn) {
    }

    @Override
    protected void apply(Restaurant restaurant, int turn) {
    }

    @Override
    public String getCommand() {
        return "wait";
    }

    @Override
    public String getResult() {
        return "waits";
    }
}
