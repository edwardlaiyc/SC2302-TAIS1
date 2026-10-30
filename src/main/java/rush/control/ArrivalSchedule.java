package rush.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import rush.entity.CriticCustomer;
import rush.entity.Customer;
import rush.entity.RegularCustomer;
import rush.entity.VIPCustomer;

public class ArrivalSchedule {

    private final List<Integer> turns = new ArrayList<>();
    private final List<BiFunction<Integer, Integer, Customer>> customerTypes = new ArrayList<>();

    // e.g. add(4, VIPCustomer::new) means a VIP arrives at the start of turn 4
    public ArrivalSchedule add(int turn, BiFunction<Integer, Integer, Customer> customerType) {
        turns.add(turn);
        customerTypes.add(customerType);
        return this;
    }

    public static ArrivalSchedule allRegular() {
        return new ArrivalSchedule()
                .add(1, RegularCustomer::new)
                .add(4, RegularCustomer::new)
                .add(7, RegularCustomer::new)
                .add(10, RegularCustomer::new)
                .add(13, RegularCustomer::new)
                .add(16, RegularCustomer::new);
    }

    // same six turns, with the 2nd and 4th arrivals replaced by a VIP and a Critic
    public static ArrivalSchedule withVipAndCritic() {
        return new ArrivalSchedule()
                .add(1, RegularCustomer::new)
                .add(4, VIPCustomer::new)
                .add(7, RegularCustomer::new)
                .add(10, CriticCustomer::new)
                .add(13, RegularCustomer::new)
                .add(16, RegularCustomer::new);
    }

    public List<Customer> arrivalsOn(int turn) {
        List<Customer> arrivals = new ArrayList<>();
        for (int i = 0; i < turns.size(); i++) {
            if (turns.get(i) == turn) {
                arrivals.add(customerTypes.get(i).apply(i + 1, turn));
            }
        }
        return arrivals;
    }
}
