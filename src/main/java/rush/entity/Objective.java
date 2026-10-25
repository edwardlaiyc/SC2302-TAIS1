package rush.entity;

public class Objective {

    private final int minPaid;
    private final Money minRevenue;
    private final int minAverageSatisfaction;

    public Objective(int minPaid, Money minRevenue, int minAverageSatisfaction) {
        this.minPaid = minPaid;
        this.minRevenue = minRevenue;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public static Objective standard() {
        return new Objective(4, Money.ofDollars(45), 60);
    }

    public boolean paidMet(GameStats stats) {
        return stats.getPaidCount() >= minPaid;
    }

    public boolean revenueMet(GameStats stats) {
        return stats.getRevenue().isAtLeast(minRevenue);
    }

    // compare totals instead of the average so 59.75 can't round up to 60
    public boolean satisfactionMet(GameStats stats) {
        return stats.getPaidCount() > 0
                && stats.getPaidSatisfactionTotal() >= minAverageSatisfaction * stats.getPaidCount();
    }

    public GameOutcome judge(GameStats stats) {
        if (paidMet(stats) && revenueMet(stats) && satisfactionMet(stats)) {
            return GameOutcome.VICTORY;
        }
        return GameOutcome.DEFEAT;
    }

    public int getMinPaid() {
        return minPaid;
    }

    public Money getMinRevenue() {
        return minRevenue;
    }

    public int getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }
}
