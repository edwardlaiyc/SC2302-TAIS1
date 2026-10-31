package rush.entity;

// two existing dishes sold together: units add up, price is 90% of the pair
public class ComboMeal extends MenuItem {

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName(),
                first.getPrice().plus(second.getPrice()).percent(90),
                first.getPreparationUnits() + second.getPreparationUnits());
        this.first = first;
        this.second = second;
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }
}
