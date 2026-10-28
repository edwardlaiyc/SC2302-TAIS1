package rush.entity;

// not a Staff subclass: the manager never gives the Cashier a task
public class Cashier {

    public Customer collectPayment(Restaurant restaurant, int turn) {
        Customer next = null;
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.canPayOn(turn) && (next == null || customer.getReadyToPayTurn() < next.getReadyToPayTurn())) {
                next = customer;
            }
        }
        if (next != null) {
            restaurant.takePayment(next);
        }
        return next;
    }
}
