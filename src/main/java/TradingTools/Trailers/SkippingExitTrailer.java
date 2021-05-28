package TradingTools.Trailers;

import com.binance.client.model.enums.PositionSide;

public class SkippingExitTrailer extends ExitTrailer {
    private Long timeToExit;

    private double prevOpenPrice = 0;

    private double exitPrice;

    private PositionSide side;

    double trailingPercentage;
    private final Long timeProtection = 10000L;


    public SkippingExitTrailer(double trailingPercentage, PositionSide side){
        this.side = side;
        this.trailingPercentage = trailingPercentage;

        if(side == PositionSide.LONG){
            calculateLongTrailingExitPrices(prevOpenPrice, trailingPercentage);
        }
        else{
            calculateShortTrailingExitPrices(prevOpenPrice, trailingPercentage);
        }
    }

    public void updateTrailer(double currentOpenPrice){
        if(side == PositionSide.LONG) {
            if (currentOpenPrice < prevOpenPrice || prevOpenPrice == 0) {
                prevOpenPrice = currentOpenPrice;
                calculateLongTrailingExitPrices(prevOpenPrice, trailingPercentage);
            }
        }
        else{
            if (currentOpenPrice > prevOpenPrice || prevOpenPrice == 0) {
                prevOpenPrice = currentOpenPrice;
                calculateShortTrailingExitPrices(prevOpenPrice, trailingPercentage);
            }
        }
    }

    public boolean needToSell(double currentPrice){
            if (side == PositionSide.LONG) {
                if(currentPrice <= exitPrice ){
                    return TimeProtect();
                } else {
                    timeToExit = null;
                    return false;
                }
            }
            else if (currentPrice >= exitPrice){
                return TimeProtect();
            } else {
                timeToExit = null;
                return false;
            }

    }

    private boolean TimeProtect() {
        if(timeToExit == null){
            timeToExit = System.currentTimeMillis();
        }
        return (timeToExit + timeProtection) <= System.currentTimeMillis();
    }

    private void calculateShortTrailingExitPrices(double curOpenPrice, double trailingPercentage) {
        if (curOpenPrice<prevOpenPrice){
            prevOpenPrice= curOpenPrice;
            exitPrice=curOpenPrice+ curOpenPrice * (trailingPercentage/100);
        }
    }

    private void calculateLongTrailingExitPrices(double curOpenPrice, double trailingPercentage) {
        if (curOpenPrice>prevOpenPrice){
            prevOpenPrice= curOpenPrice;
            exitPrice=curOpenPrice- (curOpenPrice * (trailingPercentage/100));
        }
    }

    public double getPrevOpenPrice() {
        return prevOpenPrice;
    }

    public void setPrevOpenPrice(double prevOpenPrice) {
        this.prevOpenPrice = prevOpenPrice;
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
