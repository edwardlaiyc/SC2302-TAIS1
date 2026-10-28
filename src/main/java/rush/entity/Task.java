package rush.entity;

// one job the manager can hand to a staff member; each subclass checks its own rule
public abstract class Task {

    public abstract TaskType getType();

    public abstract void check(Restaurant restaurant, int turn) throws InvalidTaskException;

    protected abstract void apply(Restaurant restaurant, int turn);

    // the task as the player types it, e.g. "seat C3 T1"
    public abstract String getCommand();

    public abstract String getResult();

    final void perform(Restaurant restaurant, int turn) throws InvalidTaskException {
        check(restaurant, turn);
        apply(restaurant, turn);
    }

    public final boolean isAllowed(Restaurant restaurant, int turn) {
        try {
            check(restaurant, turn);
            return true;
        } catch (InvalidTaskException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return getCommand();
    }
}
