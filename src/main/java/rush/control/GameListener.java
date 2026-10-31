package rush.control;

import java.util.List;

import rush.entity.Customer;
import rush.entity.GameOutcome;
import rush.entity.Objective;
import rush.entity.Restaurant;
import rush.entity.Staff;
import rush.entity.Task;

public interface GameListener {

    default void turnStarted(int turn, int totalTurns, Restaurant restaurant, List<Staff> staff) { }

    default void customerArrived(Customer customer) { }

    default void happyHourStarted() { }

    default void customersRecovered(List<Customer> customers, int amount) { }

    default void taskDone(Staff staff, Task task) { }

    default void taskRefused(Staff staff, String reason) { }

    default void customerPaid(Customer customer) { }

    default void customerFinishedEating(Customer customer) { }

    default void customerLeft(Customer customer) { }

    default void turnEnded(int turn, int totalTurns, Restaurant restaurant) { }

    default void gameEnded(GameOutcome outcome, Restaurant restaurant, Objective objective) { }
}
