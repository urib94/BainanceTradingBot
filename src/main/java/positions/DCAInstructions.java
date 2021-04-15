package positions;

import strategies.DCAStrategy;

public class DCAInstructions extends SellingInstructions {
    private DCAStrategy.DCAType type;
    private double DCAAmount;

    public DCAInstructions(DCAStrategy.DCAType type, double buyingAmount) {
        this.type = type;
        this.DCAAmount = buyingAmount;
    }

    public DCAStrategy.DCAType  getDCAType() {
        return type;
    }

    public void setType(DCAStrategy.DCAType type) {
        this.type = type;
    }

    public  double getDCAAmount() {
        return DCAAmount;
    }

    public void setDCAAmount(double DCAAmount) {
        this.DCAAmount = DCAAmount;
    }
    public enum DCAType {
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
