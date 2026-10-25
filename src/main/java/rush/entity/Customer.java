package rush.entity;

public class Customer {

    public static final int MAX_SATISFACTION = 100;
    private static final int SATISFACTION_LOSS = 8;

    private final int id;
    private final int arrivalTurn;

    private CustomerState state = CustomerState.WAITING;
    private int satisfaction = MAX_SATISFACTION;
    private Table table;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    public Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    // customer n picks dish (n - 1) mod menu size: Salad, Burger, Pasta, Salad, ...
    public MenuItem chooseItem(Menu menu) {
        return menu.get((id - 1) % menu.size());
    }

    void sitAt(Table table) {
        this.table = table;
        state = CustomerState.SEATED;
    }

    Order placeOrder(Menu menu, int turn) {
        MenuItem item = chooseItem(menu);
        order = new Order(this, item, item.getPrice(), turn);
        state = CustomerState.ORDERED;
        return order;
    }

    void startEating(int turn) {
        state = CustomerState.EATING;
        servedTurn = turn;
    }

    // a customer served on an earlier turn has now eaten for one full turn
    boolean finishEating(int turn) {
        if (state == CustomerState.EATING && servedTurn < turn) {
            state = CustomerState.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    public boolean canPayOn(int turn) {
        return state == CustomerState.READY_TO_PAY && readyToPayTurn < turn;
    }

    Money pay() {
        state = CustomerState.PAID;
        order.markPaid();
        leaveTable();
        return order.getPrice();
    }

    public boolean isWaitingForService() {
        return state == CustomerState.WAITING
                || state == CustomerState.SEATED
                || state == CustomerState.ORDERED;
    }

    boolean loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - SATISFACTION_LOSS);
        return satisfaction == 0;
    }

    void leave() {
        state = CustomerState.LEFT;
        if (order != null) {
            order.cancelIfNotServed();
        }
        leaveTable();
    }

    private void leaveTable() {
        if (table != null) {
            table.clear();
            table = null;
        }
    }

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public CustomerState getState() {
        return state;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    @Override
    public String toString() {
        return "C" + id;
    }
}
