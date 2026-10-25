package rush.entity;

import java.util.List;

public class Menu {

    private final List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        this.items = List.copyOf(items);
    }

    public static Menu standard() {
        return new Menu(List.of(
                new MenuItem("Salad", Money.ofDollars(9), 1),
                new MenuItem("Burger", Money.ofDollars(15), 1),
                new MenuItem("Pasta", Money.ofDollars(18), 2)));
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public MenuItem get(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }
}
