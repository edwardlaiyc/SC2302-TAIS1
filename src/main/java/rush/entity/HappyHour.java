package rush.entity;

public class HappyHour {

    private static final int LENGTH = 2;
    private static final int PRICE_PERCENT = 80;
    private static final int RECOVERY = 15;

    private final boolean available;
    private int startTurn; // 0 until it has been used

    public HappyHour(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean canStart() {
        return available && startTurn == 0;
    }

    void start(int turn) {
        if (!canStart()) {
            throw new IllegalStateException("Happy Hour can only be used once");
        }
        startTurn = turn;
    }

    public boolean isActive(int turn) {
        return startTurn > 0 && turn >= startTurn && turn < startTurn + LENGTH;
    }

    // counts the current turn, so it shows 2 on the first turn and 1 on the last
    public int getTurnsLeft(int turn) {
        return isActive(turn) ? startTurn + LENGTH - turn : 0;
    }

    public Money applyTo(Money price, int turn) {
        return isActive(turn) ? price.percent(PRICE_PERCENT) : price;
    }

    public int getRecovery() {
        return RECOVERY;
    }
}
