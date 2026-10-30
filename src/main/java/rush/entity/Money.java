package rush.entity;

// stored as whole cents so prices never pick up rounding errors
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    private final int cents;

    private Money(int cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + cents);
        }
        this.cents = cents;
    }

    public static Money ofCents(int cents) {
        return new Money(cents);
    }

    public static Money ofDollars(int dollars) {
        return new Money(dollars * 100);
    }

    public Money plus(Money other) {
        return new Money(cents + other.cents);
    }

    // rounds half up to the nearest cent
    public Money percent(int percent) {
        return new Money((cents * percent + 50) / 100);
    }

    public boolean isAtLeast(Money other) {
        return cents >= other.cents;
    }

    @Override
    public int compareTo(Money other) {
        return Integer.compare(cents, other.cents);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money)) {
            return false;
        }
        Money that = (Money) other;
        return cents == that.cents;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(cents);
    }

    @Override
    public String toString() {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
