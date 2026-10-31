package rush.boundary;

import java.util.ArrayList;
import java.util.List;

import rush.control.GameListener;
import rush.entity.Customer;
import rush.entity.CustomerState;
import rush.entity.GameOutcome;
import rush.entity.GameStats;
import rush.entity.HappyHour;
import rush.entity.Objective;
import rush.entity.Order;
import rush.entity.Restaurant;
import rush.entity.Staff;
import rush.entity.Table;
import rush.entity.Task;

public class ConsoleView implements GameListener {

    private static final String LINE = "=".repeat(72);

    private final Colors colors;
    private int turnsUsed;

    public ConsoleView(Colors colors) {
        this.colors = colors;
    }

    public void showTitle(Objective objective, int turns) {
        System.out.println(colors.bold("RESTAURANT RUSH"));
        System.out.println("Win after " + turns + " turns with at least " + objective.getMinPaid()
                + " paid customers, " + objective.getMinRevenue() + " revenue and average satisfaction "
                + objective.getMinAverageSatisfaction() + " or more.");
        System.out.println(colors.dim("Type 'help' at any prompt for the commands."));
    }

    @Override
    public void turnStarted(int turn, int totalTurns, Restaurant restaurant, List<Staff> staff) {
        String title = " TURN " + turn + " / " + totalTurns;
        String totals = "Revenue " + restaurant.getStats().getRevenue()
                + "   Avg satisfaction " + average(restaurant.getStats());
        System.out.println();
        System.out.println(LINE);
        System.out.println(colors.bold(title) + " ".repeat(LINE.length() - title.length() - totals.length()) + totals);
        System.out.println(LINE);

        List<String> queue = new ArrayList<>();
        for (Customer customer : restaurant.getQueue().getCustomers()) {
            queue.add(customer + " " + customer.getType() + " " + satisfaction(customer));
        }
        System.out.println(label("Queue") + (queue.isEmpty() ? colors.dim("empty") : String.join("   ", queue)));

        String heading = "Tables";
        for (Table table : restaurant.getTables()) {
            System.out.println(label(heading) + describe(table));
            heading = "";
        }

        HappyHour happyHour = restaurant.getHappyHour();
        if (happyHour.isAvailable()) {
            String status;
            if (happyHour.isActive(turn)) {
                int left = happyHour.getTurnsLeft(turn);
                status = colors.green(left == 1 ? "ACTIVE, last turn" : "ACTIVE, " + left + " turns left");
            } else if (happyHour.canStart()) {
                status = "available";
            } else {
                status = colors.dim("used");
            }
            System.out.println(label("Happy Hour") + status);
        }

        List<String> kitchen = new ArrayList<>();
        for (Order order : restaurant.getKitchenOrders()) {
            kitchen.add(order + " " + order.getUnitsDone() + "/" + order.getUnitsNeeded() + " " + order.getStatus());
        }
        System.out.println(label("Kitchen") + (kitchen.isEmpty() ? colors.dim("empty") : String.join("   ", kitchen)));

        heading = "Tasks";
        for (Staff member : staff) {
            List<String> commands = new ArrayList<>();
            for (Task task : member.getAllowedTasks(restaurant, turn)) {
                commands.add(task.getCommand());
            }
            System.out.println(label(heading) + String.format("%-7s", member.getTitle()) + String.join(" | ", commands));
            heading = "";
        }
        System.out.println(LINE);
    }

    @Override
    public void customerArrived(Customer customer) {
        System.out.println(colors.cyan(" + " + customer + " (" + customer.getType() + ") arrives and joins the queue (100)"));
    }

    @Override
    public void happyHourStarted() {
        System.out.println(colors.green(" * Happy Hour is ACTIVE, 2 turns left: 20% off orders taken this turn and next"));
    }

    @Override
    public void customersRecovered(List<Customer> customers, int amount) {
        List<String> names = new ArrayList<>();
        for (Customer customer : customers) {
            names.add(customer + " " + satisfaction(customer));
        }
        System.out.println(colors.green(" * Happy Hour: +" + amount + " for " + String.join(", ", names)));
    }

    @Override
    public void taskDone(Staff staff, Task task) {
        System.out.println("   " + String.format("%-7s", staff.getTitle()) + task.getResult());
    }

    @Override
    public void taskRefused(Staff staff, String reason) {
        System.out.println(colors.yellow("   Not allowed: " + reason + " Choose again."));
    }

    @Override
    public void customerPaid(Customer customer) {
        System.out.println(colors.green(" $ " + customer + " pays " + customer.getOrder().getPrice()
                + " (satisfaction " + customer.getSatisfaction() + ")"));
    }

    @Override
    public void customerFinishedEating(Customer customer) {
        System.out.println(" . " + customer + " has finished eating and is ready to pay");
    }

    @Override
    public void customerLeft(Customer customer) {
        String cancelled = customer.getOrder() == null ? "" : "; order cancelled";
        System.out.println(colors.red(" ! " + customer + " leaves unhappy without paying" + cancelled));
    }

    @Override
    public void turnEnded(int turn, int totalTurns, Restaurant restaurant) {
        turnsUsed = turn;
        GameStats stats = restaurant.getStats();
        System.out.println(colors.dim(String.format(
                " End of turn %d   served %d   unhappy departures %d   revenue %s   avg satisfaction %s   turns used %d/%d",
                turn, stats.getServedCount(), stats.getDepartures(), stats.getRevenue(), average(stats),
                turn, totalTurns)));
    }

    @Override
    public void gameEnded(GameOutcome outcome, Restaurant restaurant, Objective objective) {
        GameStats stats = restaurant.getStats();
        System.out.println();
        System.out.println(LINE);
        System.out.println(outcome == GameOutcome.VICTORY ? colors.bold(colors.green(" VICTORY"))
                : colors.bold(colors.red(" DEFEAT")));
        System.out.println(LINE);
        System.out.println(target("Paid customers", "" + stats.getPaidCount(), "" + objective.getMinPaid(),
                objective.paidMet(stats)));
        System.out.println(target("Revenue", "" + stats.getRevenue(), "" + objective.getMinRevenue(),
                objective.revenueMet(stats)));
        System.out.println(target("Avg satisfaction", average(stats), "" + objective.getMinAverageSatisfaction(),
                objective.satisfactionMet(stats)));
        System.out.println(" Served " + stats.getServedCount() + "   Unhappy departures " + stats.getDepartures()
                + "   Turns used " + turnsUsed);
        System.out.println(LINE);
    }

    public void showInputEnded(String message, Restaurant restaurant, int turn) {
        GameStats stats = restaurant.getStats();
        System.out.println();
        System.out.println(colors.yellow(" " + message));
        System.out.println(" Stopped in turn " + turn + "   paid " + stats.getPaidCount() + "   revenue "
                + stats.getRevenue() + "   avg satisfaction " + average(stats));
    }

    private String describe(Table table) {
        Customer customer = table.getCustomer();
        if (customer == null) {
            return table + "  " + colors.dim("free");
        }
        Order order = customer.getOrder();
        if (order == null) {
            return table + "  " + customer + " " + customer.getType() + " " + satisfaction(customer) + "   no order yet";
        }
        // before serving, show how the dish is doing; after, show the customer
        String status = customer.getState() == CustomerState.ORDERED ? order.getStatus().name() : customer.getState().name();
        return table + "  " + customer + " " + customer.getType() + " " + satisfaction(customer) + "   "
                + order.getItem() + "  " + status;
    }

    private String satisfaction(Customer customer) {
        int value = customer.getSatisfaction();
        String text = "(" + value + ")";
        if (value < 30) {
            return colors.red(text);
        }
        return value < 60 ? colors.yellow(text) : text;
    }

    private String target(String name, String actual, String target, boolean met) {
        return String.format(" %-18s %10s   target %-8s %s", name, actual, target,
                met ? colors.green("met") : colors.red("missed"));
    }

    private static String label(String text) {
        return String.format(" %-11s", text);
    }

    private static String average(GameStats stats) {
        return stats.getPaidCount() == 0 ? "N/A" : String.format("%.1f", stats.getAverageSatisfaction());
    }
}
