package TradingTools.Trailers;

import com.binance.client.model.enums.PositionSide;

public class SkippingExitTrailer extends TrailingExit {
    private double prevOpenPrice;

    private double exitPrice;

    private PositionSide side;

    double trailingPercentage;



    public SkippingExitTrailer(double openPrice, double trailingPercentage, PositionSide side){
        prevOpenPrice = openPrice;
        this.side = side;
        this.trailingPercentage = trailingPercentage;

        if(side == PositionSide.LONG){
            calculateLongTrailingExitPrices(prevOpenPrice, trailingPercentage);
        }
        else{
            calculateShortTrailingExitPrices(prevOpenPrice, trailingPercentage);
        }
    }

    public void updateTrailer(double lastOpenPrice){
        if(side == PositionSide.LONG) {
            if (lastOpenPrice > prevOpenPrice) {
                prevOpenPrice = lastOpenPrice;
                calculateLongTrailingExitPrices(prevOpenPrice, trailingPercentage);
            }
        }
        else{
            if (lastOpenPrice < prevOpenPrice) {
                prevOpenPrice = lastOpenPrice;
                calculateShortTrailingExitPrices(prevOpenPrice, trailingPercentage);
            }
        }
    }

    public boolean needToSell(double currentPrice){
            if (side == PositionSide.LONG) {
                return currentPrice <= exitPrice;
            } else return currentPrice >= exitPrice;


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
