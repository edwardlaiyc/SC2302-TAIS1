package rush.entity;

public class Order {

    private final Customer customer;
    private final MenuItem item;
    private final Money price;
    private final int takenTurn;

    private OrderStatus status = OrderStatus.PREPARING;
    private int unitsDone;
    private int readyTurn;

    // price is passed in so it stays locked even if the menu changes later
    Order(Customer customer, MenuItem item, Money price, int takenTurn) {
        this.customer = customer;
        this.item = item;
        this.price = price;
        this.takenTurn = takenTurn;
    }

    void checkCookable() throws InvalidTaskException {
        if (status != OrderStatus.PREPARING) {
            throw new InvalidTaskException(this + " is already " + status + ".");
        }
    }

    void cookOneUnit(int turn) {
        unitsDone++;
        if (unitsDone == item.getPreparationUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
        }
    }

    // the Waiter acts before the Chef, so a dish finished this turn waits until next turn
    void checkServable(int turn) throws InvalidTaskException {
        if (status == OrderStatus.PREPARING) {
            throw new InvalidTaskException(this + " is not ready yet.");
        }
        if (status != OrderStatus.READY) {
            throw new InvalidTaskException(this + " is already " + status + ".");
        }
        if (readyTurn == turn) {
            throw new InvalidTaskException(this + " became READY this turn. Serve it next turn.");
        }
    }

    void markServed() {
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        status = OrderStatus.PAID;
    }

    void cancelIfNotServed() {
        if (status == OrderStatus.PREPARING || status == OrderStatus.READY) {
            status = OrderStatus.CANCELLED;
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public Money getPrice() {
        return price;
    }

    public int getTakenTurn() {
        return takenTurn;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public int getReadyTurn() {
        return readyTurn;
    }

    public int getUnitsNeeded() {
        return item.getPreparationUnits();
    }

    @Override
    public String toString() {
        return customer + "'s " + item;
    }
}
