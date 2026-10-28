package rush.control;

import java.util.List;

import rush.entity.Cashier;
import rush.entity.Chef;
import rush.entity.Menu;
import rush.entity.Objective;
import rush.entity.Restaurant;
import rush.entity.Staff;
import rush.entity.Waiter;

public class GameConfig {

    private final int turns;
    private final int tables;
    private final Menu menu;
    private final ArrivalSchedule arrivals;
    private final Objective objective;

    public GameConfig(int turns, int tables, Menu menu, ArrivalSchedule arrivals, Objective objective) {
        this.turns = turns;
        this.tables = tables;
        this.menu = menu;
        this.arrivals = arrivals;
        this.objective = objective;
    }

    // the base setting from the brief
    public static GameConfig standard() {
        return new GameConfig(18, 2, Menu.standard(), ArrivalSchedule.standard(), Objective.standard());
    }

    public Restaurant createRestaurant() {
        return new Restaurant(menu, tables);
    }

    // listed in the order they act each turn
    public List<Staff> createStaff() {
        return List.of(new Waiter(), new Chef());
    }

    public Cashier createCashier() {
        return new Cashier();
    }

    public int getTurns() {
        return turns;
    }

    public ArrivalSchedule getArrivals() {
        return arrivals;
    }

    public Objective getObjective() {
        return objective;
    }
}
