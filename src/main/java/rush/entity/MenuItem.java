package rush.entity;

public class MenuItem {

    private final String name;
    private final Money price;
    private final int preparationUnits;

    public MenuItem(String name, Money price, int preparationUnits) {
        this.name = name;
        this.price = price;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    @Override
    public String toString() {
        return name;
    }
}
