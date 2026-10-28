package rush.entity;

public class CookTask extends Task {

    private final Customer customer;

    public CookTask(Customer customer) {
        this.customer = customer;
    }

    @Override
    public TaskType getType() {
        return TaskType.COOK;
    }

    @Override
    public void check(Restaurant restaurant, int turn) throws InvalidTaskException {
        if (customer.getOrder() == null) {
            throw new InvalidTaskException(customer + " has no order in the kitchen.");
        }
        customer.getOrder().checkCookable();
    }

    @Override
    protected void apply(Restaurant restaurant, int turn) {
        customer.getOrder().cookOneUnit(turn);
    }

    @Override
    public String getCommand() {
        return "cook " + customer;
    }

    @Override
    public String getResult() {
        Order order = customer.getOrder();
        String progress = order.getUnitsDone() + "/" + order.getUnitsNeeded();
        return order + " " + progress + (order.getStatus() == OrderStatus.READY ? " READY" : "");
    }
}
