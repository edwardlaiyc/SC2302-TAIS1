package rush.entity;

public class SeatTask extends Task {

    private final Customer customer;
    private final Table table;

    public SeatTask(Customer customer, Table table) {
        this.customer = customer;
        this.table = table;
    }

    @Override
    public TaskType getType() {
        return TaskType.SEAT;
    }

    @Override
    public void check(Restaurant restaurant, int turn) throws InvalidTaskException {
        if (!restaurant.getQueue().contains(customer)) {
            throw new InvalidTaskException(customer + " is not waiting in the queue.");
        }
        if (!table.isFree()) {
            throw new InvalidTaskException(table + " is taken by " + table.getCustomer() + ".");
        }
    }

    @Override
    protected void apply(Restaurant restaurant, int turn) {
        restaurant.seat(customer, table);
    }

    @Override
    public String getCommand() {
        return "seat " + customer + " " + table;
    }

    @Override
    public String getResult() {
        return customer + " sits at " + table;
    }
}
