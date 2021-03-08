package Utils;

import com.binance.client.api.model.enums.PositionSide;

import java.math.BigDecimal;

public class Trailer {
    private BigDecimal highestPrice;
    private BigDecimal exitPrice;
    private PositionSide side;
    Double trailingPercentage;

    public Trailer(BigDecimal currentPrice, Double trailingPercentage, PositionSide side){
        highestPrice = currentPrice;
        this.side = side;
        this.trailingPercentage = trailingPercentage;
        if(side == PositionSide.LONG){
            exitPrice = calculateLongTrailingExitPrices(highestPrice, trailingPercentage);
        }
        else exitPrice = calculateShortTrailingExitPrices(highestPrice, trailingPercentage);
    }

    public void updateTrailer(BigDecimal currentPrice){
        if (currentPrice.compareTo(highestPrice) > 0 ){
            highestPrice = currentPrice;
            if(side == PositionSide.LONG){
                exitPrice = calculateLongTrailingExitPrices(highestPrice, trailingPercentage);
            }
            else exitPrice = calculateShortTrailingExitPrices(highestPrice, trailingPercentage);
        }
    }

    public boolean needToSell(BigDecimal currentPrice){
        if (side == PositionSide.LONG) return currentPrice.compareTo(exitPrice) <= 0;
        else return currentPrice.compareTo(exitPrice) >= 0;
    }

    private BigDecimal calculateShortTrailingExitPrices(BigDecimal highestPrice, Double trailingPercentage) {
        return highestPrice.add((highestPrice.multiply(BigDecimal.valueOf(trailingPercentage)).multiply(BigDecimal.valueOf(1.0/100))));
    }

    private BigDecimal calculateLongTrailingExitPrices(BigDecimal highestPrice, Double trailingPercentage) {
            return highestPrice.subtract((highestPrice.multiply(BigDecimal.valueOf(trailingPercentage)).multiply(BigDecimal.valueOf(1.0/100))));
    }

    public BigDecimal getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(BigDecimal highestPrice) {
        this.highestPrice = highestPrice;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(BigDecimal exitPrice) {
        this.exitPrice = exitPrice;
    }

    public PositionSide getSide() {
        return side;
    }

    public void setSide(PositionSide side) {
        this.side = side;
    }

    public Double getTrailingPercentage() {
        return trailingPercentage;
    }

    public void setTrailingPercentage(Double trailingPercentage) {
        this.trailingPercentage = trailingPercentage;
    }
}
