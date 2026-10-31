package rush.entity;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {

    private final Menu menu;
    private final List<Table> tables = new ArrayList<>();
    private final WaitingQueue queue = new WaitingQueue();
    private final List<Customer> customers = new ArrayList<>();
    private final GameStats stats = new GameStats();
    private final HappyHour happyHour;

    public Restaurant(Menu menu, int tableCount, boolean happyHourAvailable) {
        this.menu = menu;
        this.happyHour = new HappyHour(happyHourAvailable);
        for (int number = 1; number <= tableCount; number++) {
            tables.add(new Table(number));
        }
    }

    public void admit(Customer customer) {
        customers.add(customer);
        queue.join(customer);
    }

    void seat(Customer customer, Table table) {
        queue.remove(customer);
        table.seat(customer);
        customer.sitAt(table);
    }

    // the customer picks the dish; the price is locked now, Happy Hour included
    void takeOrder(Customer customer, int turn) {
        MenuItem item = customer.chooseItem(menu);
        Money price = happyHour.applyTo(customer.getBill(item), turn);
        customer.placeOrder(item, price, turn);
    }

    public void startHappyHour(int turn) {
        happyHour.start(turn);
    }

    // during Happy Hour, anyone still waiting for a table or food gets some patience back
    public List<Customer> recoverWaitingCustomers(int turn) {
        List<Customer> recovered = new ArrayList<>();
        if (happyHour.isActive(turn)) {
            for (Customer customer : customers) {
                if (customer.isWaitingForService()) {
                    customer.recover(happyHour.getRecovery());
                    recovered.add(customer);
                }
            }
        }
        return recovered;
    }

    void addServed() {
        stats.addServed();
    }

    public List<Customer> finishEating(int turn) {
        List<Customer> finished = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.finishEating(turn)) {
                finished.add(customer);
            }
        }
        return finished;
    }

    void takePayment(Customer customer) {
        Money bill = customer.pay();
        stats.addPayment(bill, customer.getSatisfaction());
    }

    // returns the customers who walked out
    public List<Customer> loseWaitingSatisfaction() {
        List<Customer> left = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.isWaitingForService() && customer.loseSatisfaction()) {
                customer.leave();
                stats.addDeparture();
                left.add(customer);
            }
        }
        queue.removeLeftCustomers();
        return left;
    }

    public Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        return null;
    }

    public Table findTable(int number) {
        for (Table table : tables) {
            if (table.getNumber() == number) {
                return table;
            }
        }
        return null;
    }

    public List<Order> getKitchenOrders() {
        List<Order> orders = new ArrayList<>();
        for (Customer customer : customers) {
            Order order = customer.getOrder();
            if (order != null && (order.getStatus() == OrderStatus.PREPARING || order.getStatus() == OrderStatus.READY)) {
                orders.add(order);
            }
        }
        return orders;
    }

    public Menu getMenu() {
        return menu;
    }

    public List<Table> getTables() {
        return List.copyOf(tables);
    }

    public WaitingQueue getQueue() {
        return queue;
    }

    public List<Customer> getCustomers() {
        return List.copyOf(customers);
    }

    public HappyHour getHappyHour() {
        return happyHour;
    }

    public GameStats getStats() {
        return stats;
    }
}
