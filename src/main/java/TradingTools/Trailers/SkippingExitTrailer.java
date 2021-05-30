package TradingTools.Trailers;

import com.binance.client.model.enums.PositionSide;
import singletonHelpers.TelegramMessenger;

public class SkippingExitTrailer extends ExitTrailer {
    private Long timeToExit =null;

    private double prevOpenPrice = 0;

    private double exitPrice;

    private PositionSide side;

    double trailingPercentage;

    private boolean sendMessage = true;


    public SkippingExitTrailer(double trailingPercentage, PositionSide side){
        this.side = side;
        this.trailingPercentage = trailingPercentage;
    }

    public void updateTrailer(double currentOpenPrice){
        if(side == PositionSide.LONG) {
            if (currentOpenPrice > prevOpenPrice || prevOpenPrice == 0) {
                prevOpenPrice = currentOpenPrice;
                calculateLongTrailingExitPrices(prevOpenPrice, trailingPercentage);
                sendMessage = true;
            }
        }
        else{
            if (currentOpenPrice < prevOpenPrice || prevOpenPrice == 0) {
                prevOpenPrice = currentOpenPrice;
                calculateShortTrailingExitPrices(prevOpenPrice, trailingPercentage);
                sendMessage = true;
            }
        }
    }

    public boolean needToSell(double currentPrice){
        if (sendMessage){
            TelegramMessenger.sendToTelegram("postion side: " + side + "exit Price = " + exitPrice + " curr price = " + currentPrice);
            sendMessage = false;
        }
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
            System.out.println("start trailing time : " + timeToExit);
        }
        Long timeProtection = 2500L;
        return (timeToExit + timeProtection) <= System.currentTimeMillis();
    }

    private void calculateShortTrailingExitPrices(double curOpenPrice, double trailingPercentage) {
        exitPrice = curOpenPrice + curOpenPrice * (trailingPercentage/100);

    }

    private void calculateLongTrailingExitPrices(double curOpenPrice, double trailingPercentage) {
        exitPrice = curOpenPrice - (curOpenPrice * (trailingPercentage/100));
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
