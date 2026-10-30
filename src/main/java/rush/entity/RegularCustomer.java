package rush.entity;

public class RegularCustomer extends Customer {

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLoss() {
        return 8;
    }
}
