package positions;

public class DCAInstructions extends SellingInstructions {
    private PositionHandler.DCAType type;
    private double DCAAmount;

    public DCAInstructions(PositionHandler.DCAType type, double buyingAmount) {
        this.type = type;
        this.DCAAmount = buyingAmount;
    }

    public PositionHandler.DCAType getDCAType() {
        return type;
    }

    public void setType(PositionHandler.DCAType type) {
        this.type = type;
    }

    public  double getDCAAmount() {
        return DCAAmount;
    }

    public void setDCAAmount(double DCAAmount) {
        this.DCAAmount = DCAAmount;
    }
}
