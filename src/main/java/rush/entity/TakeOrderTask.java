package rush.entity;

public class TakeOrderTask extends Task {

    private final Customer customer;

    public TakeOrderTask(Customer customer) {
        this.customer = customer;
    }

    @Override
    public TaskType getType() {
        return TaskType.TAKE_ORDER;
    }

    @Override
    public void check(Restaurant restaurant, int turn) throws InvalidTaskException {
        switch (customer.getState()) {
            case SEATED -> { }
            case WAITING -> throw new InvalidTaskException(customer + " has not been seated yet.");
            case LEFT -> throw new InvalidTaskException(customer + " has left.");
            default -> throw new InvalidTaskException(customer + " has already ordered.");
        }
    }

    @Override
    protected void apply(Restaurant restaurant, int turn) {
        restaurant.takeOrder(customer, turn);
    }

    @Override
    public String getCommand() {
        return "order " + customer;
    }

    @Override
    public String getResult() {
        Order order = customer.getOrder();
        return customer + " orders " + order.getItem() + " (" + order.getPrice() + ")";
    }
}
