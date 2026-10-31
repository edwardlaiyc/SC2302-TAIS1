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

    // the standard dishes plus every pair of them as a combo
    public static Menu withCombos() {
        MenuItem salad = new MenuItem("Salad", Money.ofDollars(9), 1);
        MenuItem burger = new MenuItem("Burger", Money.ofDollars(15), 1);
        MenuItem pasta = new MenuItem("Pasta", Money.ofDollars(18), 2);
        return new Menu(List.of(salad, burger, pasta,
                new ComboMeal(salad, burger),
                new ComboMeal(salad, pasta),
                new ComboMeal(burger, pasta)));
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
