package rush.entity;

public class VIPCustomer extends Customer {

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLoss() {
        return 5;
    }

    @Override
    public Money getBill(MenuItem item) {
        return item.getPrice().percent(90);
    }
}
