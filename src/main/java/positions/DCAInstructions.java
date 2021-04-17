package positions;

import strategies.DCAStrategy;

public class DCAInstructions extends SellingInstructions {
    private DCAStrategy.DCAType type;

    public DCAInstructions(DCAStrategy.DCAType type, double buyingAmount) {
        this.type = type;
    }

    public DCAStrategy.DCAType  getDCAType() {
        return type;
    }

    public void setType(DCAStrategy.DCAType type) {
        this.type = type;
    }

}
