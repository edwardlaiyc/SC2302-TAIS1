package rush.entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Staff {

    private final String title;
    private final List<TaskType> duties;
    private int lastTurnWorked;

    protected Staff(String title, TaskType... duties) {
        this.title = title;
        this.duties = List.of(duties);
    }

    public final void perform(Task task, Restaurant restaurant, int turn) throws InvalidTaskException {
        if (lastTurnWorked == turn) {
            throw new InvalidTaskException("The " + title + " has already worked this turn.");
        }
        if (!canDo(task.getType())) {
            throw new InvalidTaskException("The " + title + " cannot " + task.getType().getActivity() + ".");
        }
        task.perform(restaurant, turn);
        lastTurnWorked = turn; // only set after success, so a refused task costs nothing
    }

    public boolean canDo(TaskType type) {
        return type == TaskType.WAIT || duties.contains(type);
    }

    public List<Task> getAllowedTasks(Restaurant restaurant, int turn) {
        List<Task> allowed = new ArrayList<>();
        for (Task task : possibleTasks(restaurant)) {
            if (task.isAllowed(restaurant, turn)) {
                allowed.add(task);
            }
        }
        allowed.add(new WaitTask());
        return allowed;
    }

    protected abstract List<Task> possibleTasks(Restaurant restaurant);

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
