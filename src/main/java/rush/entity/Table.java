package rush.entity;

public class Table {

    private final int number;
    private Customer customer;

    public Table(int number) {
        this.number = number;
    }

    void seat(Customer customer) {
        this.customer = customer;
    }

    void clear() {
        customer = null;
    }

    public boolean isFree() {
        return customer == null;
    }

    public int getNumber() {
        return number;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return "T" + number;
    }
}
