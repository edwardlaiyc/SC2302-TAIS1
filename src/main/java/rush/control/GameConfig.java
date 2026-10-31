package rush.control;

import java.util.List;

import rush.entity.Cashier;
import rush.entity.Chef;
import rush.entity.Host;
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
    private final List<Staff> staff;
    private final boolean happyHour;
    private final Objective objective;

    public GameConfig(int turns, int tables, Menu menu, ArrivalSchedule arrivals, List<Staff> staff,
                      boolean happyHour, Objective objective) {
        this.turns = turns;
        this.tables = tables;
        this.menu = menu;
        this.arrivals = arrivals;
        this.staff = staff;
        this.happyHour = happyHour;
        this.objective = objective;
    }

    // base setting from the brief with Regular customers only, exactly as in stage 1
    public static GameConfig stage1() {
        return new GameConfig(18, 2, Menu.standard(), ArrivalSchedule.allRegular(),
                List.of(new Waiter(), new Chef()), false, Objective.standard());
    }

    public static GameConfig stage2() {
        return new GameConfig(18, 2, Menu.standard(), ArrivalSchedule.withVipAndCritic(),
                List.of(new Waiter(), new Chef()), false, Objective.standard());
    }

    // staff act in list order, so the Host seats before the Waiter does anything
    public static GameConfig stage3() {
        return new GameConfig(18, 2, Menu.withCombos(), ArrivalSchedule.withVipAndCritic(),
                List.of(new Host(), new Waiter(), new Chef()), true, Objective.standard());
    }

    public Restaurant createRestaurant() {
        return new Restaurant(menu, tables, happyHour);
    }

    public List<Staff> getStaff() {
        return staff;
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
