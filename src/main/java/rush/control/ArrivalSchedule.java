package rush.control;

import java.util.ArrayList;
import java.util.List;

import rush.entity.Customer;

public class ArrivalSchedule {

    private final List<Integer> arrivalTurns;

    public ArrivalSchedule(List<Integer> arrivalTurns) {
        this.arrivalTurns = List.copyOf(arrivalTurns);
    }

    public static ArrivalSchedule standard() {
        return new ArrivalSchedule(List.of(1, 4, 7, 10, 13, 16));
    }

    public List<Customer> arrivalsOn(int turn) {
        List<Customer> arrivals = new ArrayList<>();
        for (int i = 0; i < arrivalTurns.size(); i++) {
            if (arrivalTurns.get(i) == turn) {
                arrivals.add(new Customer(i + 1, turn));
            }
        }
        return arrivals;
    }
}
