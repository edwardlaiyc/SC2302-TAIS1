package rush.entity;

public class CriticCustomer extends Customer {

    private static final int COLD_FOOD_PENALTY = 10;

    private boolean gotColdFood;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLoss() {
        return 12;
    }

    // always orders the dish that takes longest to cook; on a tie, the dearer one
    @Override
    public MenuItem chooseItem(Menu menu) {
        MenuItem best = menu.get(0);
        for (MenuItem item : menu.getItems()) {
            int units = item.getPreparationUnits();
            if (units > best.getPreparationUnits()
                    || (units == best.getPreparationUnits() && item.getPrice().compareTo(best.getPrice()) > 0)) {
                best = item;
            }
        }
        return best;
    }

    // a dish could always be served the turn after it's ready; any later and it's gone cold
    @Override
    void startEating(int turn) {
        super.startEating(turn);
        if (turn > getOrder().getReadyTurn() + 1) {
            gotColdFood = true;
            reduceSatisfaction(COLD_FOOD_PENALTY);
        }
    }

    @Override
    public String getReaction() {
        return gotColdFood ? "the food is cold, -" + COLD_FOOD_PENALTY : "";
    }
}
