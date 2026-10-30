package rush.entity;

public class ServeTask extends Task {

    private final Customer customer;

    public ServeTask(Customer customer) {
        this.customer = customer;
    }

    @Override
    public TaskType getType() {
        return TaskType.SERVE;
    }

    @Override
    public void check(Restaurant restaurant, int turn) throws InvalidTaskException {
        if (customer.getState() == CustomerState.LEFT) {
            throw new InvalidTaskException(customer + " has left.");
        }
        if (customer.getOrder() == null) {
            throw new InvalidTaskException(customer + " has not ordered yet.");
        }
        customer.getOrder().checkServable(turn);
    }

    @Override
    protected void apply(Restaurant restaurant, int turn) {
        customer.getOrder().markServed();
        customer.startEating(turn);
        restaurant.addServed();
    }

    @Override
    public String getCommand() {
        return "serve " + customer;
    }

    @Override
    public String getResult() {
        String reaction = customer.getReaction();
        return customer + " gets " + customer.getOrder().getItem() + " and starts eating"
                + (reaction.isEmpty() ? "" : " (" + reaction + ")");
    }
}
