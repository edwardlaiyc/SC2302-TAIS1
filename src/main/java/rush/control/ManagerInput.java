package rush.control;

import java.util.List;

import rush.entity.InvalidTaskException;
import rush.entity.Restaurant;
import rush.entity.Staff;
import rush.entity.Task;

// where the manager's choices come from; the controller doesn't care if it's a keyboard or a file
public interface ManagerInput {

    Task chooseTask(Staff staff, List<Task> allowed, Restaurant restaurant) throws InvalidTaskException;

    boolean wantsHappyHour();
}
