package rush.entity;

public class GameStats {

    private Money revenue = Money.ZERO;
    private int servedCount;
    private int paidCount;
    private int paidSatisfactionTotal;
    private int departures;

    void addServed() {
        servedCount++;
    }

    // satisfaction is recorded at the moment the customer pays
    void addPayment(Money amount, int satisfaction) {
        revenue = revenue.plus(amount);
        paidCount++;
        paidSatisfactionTotal += satisfaction;
    }

    void addDeparture() {
        departures++;
    }

    public Money getRevenue() {
        return revenue;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getPaidSatisfactionTotal() {
        return paidSatisfactionTotal;
    }

    public int getDepartures() {
        return departures;
    }

    public double getAverageSatisfaction() {
        return paidCount == 0 ? 0 : (double) paidSatisfactionTotal / paidCount;
    }
}
