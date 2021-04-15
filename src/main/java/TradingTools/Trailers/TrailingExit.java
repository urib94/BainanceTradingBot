package TradingTools.Trailers;

import com.binance.client.model.enums.PositionSide;

public class TrailingExit {

    private double absoluteMaxPrice;

    private double exitPrice;

    private PositionSide side;

    double trailingPercentage;

    public TrailingExit(double currentPrice, double trailingPercentage, PositionSide side){
        absoluteMaxPrice = currentPrice;
        this.side = side;
        this.trailingPercentage = trailingPercentage;
        if(side == PositionSide.LONG){
            exitPrice = calculateLongTrailingExitPrices(absoluteMaxPrice, trailingPercentage);
        }
        else{
            exitPrice = calculateShortTrailingExitPrices(absoluteMaxPrice, trailingPercentage);
        }
    }

    public TrailingExit() {
    }

    public void updateTrailer(double currentPrice){
        if(side == PositionSide.LONG) {
            if (currentPrice > absoluteMaxPrice) {
                absoluteMaxPrice = currentPrice;
                exitPrice = calculateLongTrailingExitPrices(absoluteMaxPrice, trailingPercentage);
            }
        }
        else{
            if (currentPrice < absoluteMaxPrice) {
                absoluteMaxPrice = currentPrice;
                exitPrice = calculateShortTrailingExitPrices(absoluteMaxPrice, trailingPercentage);
            }
        }
    }

    public boolean needToSell(double currentPrice){
        if (side == PositionSide.LONG) return currentPrice <= exitPrice;
        else return currentPrice >= exitPrice;
    }

    private double calculateShortTrailingExitPrices(double highestPrice, double trailingPercentage) {
        return highestPrice + highestPrice * (trailingPercentage/100);
    }

    private double calculateLongTrailingExitPrices(double highestPrice, double trailingPercentage) {
            return highestPrice - (highestPrice * (trailingPercentage/100));
    }

    public double getAbsoluteMaxPrice() {
        return absoluteMaxPrice;
    }

    public void setAbsoluteMaxPrice(double absoluteMaxPrice) {
        this.absoluteMaxPrice = absoluteMaxPrice;
    }

    public double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(double exitPrice) {
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
