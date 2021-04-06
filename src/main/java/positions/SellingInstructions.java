package positions;

import java.math.BigDecimal;

public class SellingInstructions extends Instructions {
    private PositionHandler.ClosePositionTypes type;
    private double sellingQtyPercentage;

    public SellingInstructions(PositionHandler.ClosePositionTypes type, double sellingQtyPercentage) {
        this.type = type;
        this.sellingQtyPercentage = sellingQtyPercentage;
    }

    public SellingInstructions() {
    }

    public PositionHandler.ClosePositionTypes getType() {
        return type;
    }

    public void setType(PositionHandler.ClosePositionTypes type) {
        this.type = type;
    }

    public double getSellingQtyPercentage() {
        return sellingQtyPercentage;
    }

    public void setSellingQtyPercentage(double sellingQtyPercentage) {
        this.sellingQtyPercentage = sellingQtyPercentage;
    }
}
