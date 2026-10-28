package rush.entity;

import java.util.ArrayList;
import java.util.List;

public class Chef extends Staff {

    public Chef() {
        super("Chef", TaskType.COOK);
    }

    @Override
    protected List<Task> possibleTasks(Restaurant restaurant) {
        List<Task> tasks = new ArrayList<>();
        for (Order order : restaurant.getKitchenOrders()) {
            tasks.add(new CookTask(order.getCustomer()));
        }
        return tasks;
    }
}
