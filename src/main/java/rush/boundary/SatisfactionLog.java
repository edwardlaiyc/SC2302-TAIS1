package rush.boundary;

import java.io.PrintWriter;

import rush.control.GameListener;
import rush.entity.Customer;
import rush.entity.Restaurant;

// writes every customer's satisfaction at the end of each turn as CSV, for the report's charts
public class SatisfactionLog implements GameListener {

    private final PrintWriter file;

    public SatisfactionLog(PrintWriter file) {
        this.file = file;
        file.println("turn,customer,state,satisfaction");
    }

    @Override
    public void turnEnded(int turn, int totalTurns, Restaurant restaurant) {
        for (Customer customer : restaurant.getCustomers()) {
            file.println(turn + "," + customer + "," + customer.getState() + "," + customer.getSatisfaction());
        }
        file.flush();
    }
}
