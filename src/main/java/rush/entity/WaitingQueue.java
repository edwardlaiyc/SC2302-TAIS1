package rush.entity;

import java.util.ArrayList;
import java.util.List;

public class WaitingQueue {

    private final List<Customer> customers = new ArrayList<>();

    void join(Customer customer) {
        customers.add(customer);
    }

    void remove(Customer customer) {
        customers.remove(customer);
    }

    void removeLeftCustomers() {
        customers.removeIf(c -> c.getState() == CustomerState.LEFT);
    }

    public boolean contains(Customer customer) {
        return customers.contains(customer);
    }

    public List<Customer> getCustomers() {
        return List.copyOf(customers);
    }
}
