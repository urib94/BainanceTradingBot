package TradingTools.Trailers;

import com.binance.client.model.enums.PositionSide;

public class SkippingEntryTrailer {
    private double prevOpenPrice;

    private double entryPrice;

    private PositionSide side;

    double trailingPercentage;
    private boolean dismiss = false;


    public SkippingEntryTrailer(double openPrice, double trailingPercentage, PositionSide side){
        prevOpenPrice = openPrice;
        this.side = side;
        this.trailingPercentage = trailingPercentage;

        if(side == PositionSide.LONG){
            entryPrice = calculateLongTrailingEntryPrice(prevOpenPrice, trailingPercentage);
        }
        else{
            entryPrice = calculateShortTrailingEntryPrice(prevOpenPrice, trailingPercentage);
        }
    }

    public void updateTrailer(double currentOpenPrice){
        dismiss = false;
        if(side == PositionSide.LONG) {
            if (currentOpenPrice < prevOpenPrice) {
                prevOpenPrice = currentOpenPrice;
                entryPrice = calculateLongTrailingEntryPrice(prevOpenPrice, trailingPercentage);
            }
        }
        else{
            if (currentOpenPrice > prevOpenPrice) {
                prevOpenPrice = currentOpenPrice;
                entryPrice = calculateShortTrailingEntryPrice(prevOpenPrice, trailingPercentage);
            }
        }
    }

    public boolean needToEnter(double currentPrice){
        if (side == PositionSide.LONG) {
                return currentPrice >= entryPrice && !dismiss;
            }else
                return currentPrice <= entryPrice && !dismiss;
    }



    private double calculateLongTrailingEntryPrice(double curOpenPrice, double trailingPercentage) {
        return curOpenPrice + curOpenPrice * (trailingPercentage/100);
    }

    private double calculateShortTrailingEntryPrice(double curOpenPrice, double trailingPercentage) {
        return curOpenPrice - (curOpenPrice * (trailingPercentage/100));
    }

    public double getPrevOpenPrice() {
        return prevOpenPrice;
    }

    public void setPrevOpenPrice(double prevOpenPrice) {
        this.prevOpenPrice = prevOpenPrice;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
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

    public void dismiss() {
        dismiss = true;
    }
}


