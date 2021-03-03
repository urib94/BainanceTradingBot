package Positions;

import java.math.BigDecimal;

public class SellingInstructions {
    private PositionHandler.ClosePositionTypes type;
    private BigDecimal sellingQtyPercentage;
    private double trailingPercentage;

    public SellingInstructions(PositionHandler.ClosePositionTypes type, BigDecimal sellingQtyPercentage, double trailingPercentage) {
        this.type = type;
        this.sellingQtyPercentage = sellingQtyPercentage;
        this.trailingPercentage = trailingPercentage;
    }

    public PositionHandler.ClosePositionTypes getType() {
        return type;
    }

    public void setType(PositionHandler.ClosePositionTypes type) {
        this.type = type;
    }

    public BigDecimal getSellingQtyPercentage() {
        return sellingQtyPercentage;
    }

    public void setSellingQtyPercentage(BigDecimal sellingQtyPercentage) {
        this.sellingQtyPercentage = sellingQtyPercentage;
    }

    public double getTrailingPercentage() {
        return trailingPercentage;
    }

    public void setTrailingPercentage(double trailingPercentage) {
        this.trailingPercentage = trailingPercentage;
    }
}
