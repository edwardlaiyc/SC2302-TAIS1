package rush.entity;

import java.util.ArrayList;
import java.util.List;

public class Host extends Staff {

    public Host() {
        super("Host", TaskType.SEAT);
    }

    @Override
    protected List<Task> possibleTasks(Restaurant restaurant) {
        List<Task> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getQueue().getCustomers()) {
            for (Table table : restaurant.getTables()) {
                tasks.add(new SeatTask(customer, table));
            }
        }
        return tasks;
    }
}
