package rush.control;

import java.util.ArrayList;
import java.util.List;

import rush.entity.Cashier;
import rush.entity.Customer;
import rush.entity.GameOutcome;
import rush.entity.InvalidTaskException;
import rush.entity.Restaurant;
import rush.entity.Staff;
import rush.entity.Task;

public class GameController {

    private final GameConfig config;
    private final ManagerInput input;
    private final List<GameListener> listeners = new ArrayList<>();
    private final Restaurant restaurant;
    private final List<Staff> staff;
    private final Cashier cashier;
    private int turn;

    public GameController(GameConfig config, ManagerInput input) {
        this.config = config;
        this.input = input;
        this.restaurant = config.createRestaurant();
        this.staff = config.createStaff();
        this.cashier = config.createCashier();
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public GameOutcome play() {
        for (turn = 1; turn <= config.getTurns(); turn++) {
            playTurn();
        }
        turn = config.getTurns();
        GameOutcome outcome = config.getObjective().judge(restaurant.getStats());
        for (GameListener l : listeners) {
            l.gameEnded(outcome, restaurant, config.getObjective());
        }
        return outcome;
    }

    private void playTurn() {
        for (GameListener l : listeners) {
            l.turnStarted(turn, config.getTurns(), restaurant, staff);
        }

        // 1. arrivals
        for (Customer customer : config.getArrivals().arrivalsOn(turn)) {
            restaurant.admit(customer);
            for (GameListener l : listeners) {
                l.customerArrived(customer);
            }
        }

        // 2-3. staff act in list order, so the Waiter always goes before the Chef
        for (Staff member : staff) {
            giveTask(member);
        }

        // 4. pay first, then finish eating, so nobody pays on the turn they become ready
        Customer paid = cashier.collectPayment(restaurant, turn);
        if (paid != null) {
            for (GameListener l : listeners) {
                l.customerPaid(paid);
            }
        }
        for (Customer customer : restaurant.finishEating(turn)) {
            for (GameListener l : listeners) {
                l.customerFinishedEating(customer);
            }
        }

        // 5. waiting customers lose satisfaction, some may walk out
        for (Customer customer : restaurant.loseWaitingSatisfaction()) {
            for (GameListener l : listeners) {
                l.customerLeft(customer);
            }
        }

        for (GameListener l : listeners) {
            l.turnEnded(turn, config.getTurns(), restaurant);
        }
    }

    // keep asking until the choice is allowed; a refused choice doesn't use up the turn
    private void giveTask(Staff member) {
        while (true) {
            try {
                Task task = input.chooseTask(member, member.getAllowedTasks(restaurant, turn), restaurant);
                member.perform(task, restaurant, turn);
                for (GameListener l : listeners) {
                    l.taskDone(member, task);
                }
                return;
            } catch (InvalidTaskException e) {
                for (GameListener l : listeners) {
                    l.taskRefused(member, e.getMessage());
                }
            }
        }
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public int getTurn() {
        return turn;
    }
}
