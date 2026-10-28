package rush.entity;

import java.util.ArrayList;
import java.util.List;

public class Waiter extends Staff {

    public Waiter() {
        super("Waiter", TaskType.SEAT, TaskType.TAKE_ORDER, TaskType.SERVE);
    }

    @Override
    protected List<Task> possibleTasks(Restaurant restaurant) {
        List<Task> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            tasks.add(new ServeTask(customer));
            tasks.add(new TakeOrderTask(customer));
        }
        for (Customer customer : restaurant.getQueue().getCustomers()) {
            for (Table table : restaurant.getTables()) {
                tasks.add(new SeatTask(customer, table));
            }
        }
        return tasks;
    }
}
