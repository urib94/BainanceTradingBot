package strategies.MACrosses;

import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.AccountBalance;
import data.Config;
import data.DataHolder;
import positions.PositionHandler;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.EntryStrategy;
import strategies.ExitStrategy;
import utils.Utils;

import java.util.ArrayList;
import java.util.Date;

public class MACrossesEntryStrategy implements EntryStrategy {
    AccountBalance accountBalance;
    private double requestedBuyingAmount = MACrossesConstants.DEFAULT_BUYING_AMOUNT;
    PositionHandler positionHandler;
    private int leverage = MACrossesConstants.LEVERAGE;



    public MACrossesEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("MFI & RSI SMA Crosses");
    }


    @Override
    public PositionHandler run(DataHolder realTimeData, String symbol) {
        double currentPrice = realTimeData.getCurrentPrice();
        if (positionHandler == null) {
            if (isLongArea(realTimeData)){
                if (longFastTriger(realTimeData) || longSlowTriger(realTimeData) /*|| rsiSlowAndFastSmaCrossedUp(realTimeData)*/ || rsiSpikedUp(realTimeData)){
                    return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.LONG);
                }
            }
            else{
                if(shortFastTriger(realTimeData) || shortSlowTriger(realTimeData) /*|| rsiSlowAndFastSmaCrossedDown(realTimeData)*/ || rsiSpikedDown(realTimeData)){
                    return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.SHORT);
                }
            }
        }
        return null;
    }

    private boolean rsiSmaOutOfBannedZone(DataHolder realTimeData, int barCount){
        int index = realTimeData.getLastCloseIndex();
        double rsiValue = realTimeData.getRSIValueAtIndex(index);
        double smaOverRsiValue;
        if (barCount == MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT) smaOverRsiValue = realTimeData.getFastSmaOverRSIValue(index);
        else smaOverRsiValue = realTimeData.getSlowSmaOverRSIValue(index);
        return rsiValue > MACrossesConstants.UPPER_BANNED_ZONE_THRESHOLD || rsiValue < MACrossesConstants.LOWER_BANNED_ZONE_THRESHOLD ||
                smaOverRsiValue > MACrossesConstants.UPPER_BANNED_ZONE_THRESHOLD || smaOverRsiValue < MACrossesConstants.LOWER_BANNED_ZONE_THRESHOLD;
    }

    private boolean isLongArea(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currentPrice = realTimeData.getCurrentPrice();
        double fastSma = realTimeData.getFastSmaValue(index);
        double slowSma = realTimeData.getSlowSmaValueAtIndex(index);
        return fastSma > slowSma ;
    }

    private boolean rsiSpikedUp(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currRsiValue = realTimeData.getRSIValueAtIndex(index);
        double prevRsiValue = realTimeData.getRSIValueAtIndex(index);
        return currRsiValue > prevRsiValue + 10 && currRsiValue < prevRsiValue + 15;
    }
    private boolean rsiSpikedDown(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currRsiValue = realTimeData.getRSIValueAtIndex(index);
        double prevRsiValue = realTimeData.getRSIValueAtIndex(index);
        return currRsiValue < prevRsiValue - 10 && currRsiValue > prevRsiValue - 15;
    }

    private boolean shortFastTriger(DataHolder realTimeData) {
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
            return !volumeSpike(realTimeData);
        }

        return false;
    }

    private boolean shortSlowTriger(DataHolder realTimeData){
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.DOWN, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean longFastTriger(DataHolder realTimeData) {
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT )) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }



    private boolean longSlowTriger(DataHolder realTimeData) {
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.UP, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean rsiSlowAndFastSmaCrossedUp(DataHolder realTimeData){
        if (rsiSmaOutOfBannedZone(realTimeData, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) && rsiSmaOutOfBannedZone(realTimeData, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
            int closeIndex = realTimeData.getLastCloseIndex();
            double fastSmaCurrValue = realTimeData.getFastSmaValue(closeIndex);
            double fastSmaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
            double slowSmaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
            double slowSmaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
            return slowSmaPrevValue >= fastSmaPrevValue && slowSmaCurrValue < fastSmaCurrValue;
        }
        return false;
    }

    private boolean rsiSlowAndFastSmaCrossedDown(DataHolder realTimeData){
        int closeIndex = realTimeData.getLastCloseIndex();
        double fastSmaCurrValue = realTimeData.getFastSmaValue(closeIndex);
        double fastSmaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
        double slowSmaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
        double slowSmaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
        return slowSmaPrevValue <= fastSmaPrevValue && slowSmaCurrValue > fastSmaCurrValue;
    }
    private boolean rsiCrossedSma(DataHolder realTimeData, DataHolder.CrossType crossType, int barCount) {
        if (rsiSmaOutOfBannedZone(realTimeData, barCount)) {
            int closeIndex = realTimeData.getLastCloseIndex();
            double curr, prev, smaCurrValue, smaPrevValue;
            curr = realTimeData.getRSIValueAtIndex(closeIndex);
            prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
            if(barCount == MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) {
                smaCurrValue = realTimeData.getFastSmaValue(closeIndex);
                smaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
            } else {
                smaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
                smaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);

            }
            switch (crossType) {

                case UP:
                    return prev <= smaPrevValue && curr > smaCurrValue;

                case DOWN:
                    return prev >= smaPrevValue && curr < smaCurrValue;
            }
        }
        return false;
    }

    private boolean outOfCloseBoliingers(DataHolder realTimeData) {
        int index =realTimeData.getLastCloseIndex();
        double upperBoliinger = realTimeData.getCloseUpperBollingerAtIndex(index);
        double lowerBoliinger = realTimeData.getCloseLowerBollingerAtIndex(index);
        double currentPrice = realTimeData.getCurrentPrice();
//        System.out.println(" upper = " + upperBoliinger + "\n lower = " + lowerBoliinger + "\ncurrent price = " + currentPrice );
        boolean in = currentPrice > lowerBoliinger && currentPrice < upperBoliinger;
//        System.out.println( "in = "+ in);
        return !in;
    }

    private boolean volumeSpike(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double volume = realTimeData.getVolumeAtIndex(index);
        double smaOverVolume = realTimeData.getSmaOverVolumeValueAtIndex(index);
        return volume > 2.5 * smaOverVolume && volume < 3.5 * smaOverVolume;
    }


    private boolean crossedSma(DataHolder realTimeData, DataHolder.IndicatorType indicatorType, DataHolder.CrossType crossType, int candleCount){
        int closeIndex = realTimeData.getLastCloseIndex();
        double prev = 0, curr = 0;
        double smaCurrValue = 0, smaPrevValue = 0;
        switch(indicatorType){
            case RSI:
                curr = realTimeData.getRSIValueAtIndex(closeIndex);
                prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
                if(candleCount == MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) {
                    smaCurrValue = realTimeData.getFastSmaValue(closeIndex);
                    smaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
                } else {
                    if (candleCount == MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT) {
                        smaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
                        smaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
                    }
                }
                break;

            default:
                break;
        }
        switch(crossType){
            case UP:
                if(smaCurrValue > 40 && smaCurrValue < 60 ){
                    return false;
                }
                return prev <= smaPrevValue && curr > smaCurrValue + MACrossesConstants.ENTRY_THRESHOLD;
            case DOWN:
                if(smaCurrValue > 40 && smaCurrValue < 60 ){
                    return false;
                }
                return prev >= smaPrevValue && curr < smaCurrValue - MACrossesConstants.ENTRY_THRESHOLD;
        }
        return false;
    }


    public static boolean priceIsAboveSMA(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double smaValue = realTimeData.getSlowSmaValueAtIndex(index);
        double smaPrevValue = realTimeData.getSlowSmaValueAtIndex(index - 1);
        double closeValue = realTimeData.getClosePriceAtIndex(index);
        double closePrevValue = realTimeData.getClosePriceAtIndex(index - 1);
        System.out.println("curr sma = " + smaValue + "price = " +closeValue);
        return closeValue > smaValue || closePrevValue > smaPrevValue;
    }

    private PositionHandler buyAndCreatePositionHandler(double currentPrice, String symbol, PositionSide positionSide) {
        updateBuyingAmount(symbol);
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
        SkippingExitTrailer trailer = new SkippingExitTrailer(MACrossesConstants.TRAILING_PERCENTAGE, positionSide);
        exitStrategies.add(new MACrossesExitStrategy1(positionSide, trailer));
        if (positionSide == PositionSide.LONG) {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Average Price: " + buyOrder.getAvgPrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                positionHandler = new PositionHandler(buyOrder, exitStrategies, MACrossesConstants.STOP_LOSS_PERCENTAGE, positionSide);
                return positionHandler;
            } catch (Exception e) {
                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
                e.printStackTrace();
            }
        }
        else {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE,"TRUE" , NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought short:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Average Price: " + buyOrder.getAvgPrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                positionHandler = new PositionHandler(buyOrder, exitStrategies, MACrossesConstants.STOP_LOSS_PERCENTAGE, positionSide);
                return positionHandler;
            } catch (Exception e) {
                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
                e.printStackTrace();
            }
        }
        return null;
    }

    private void updateBuyingAmount(String symbol) {
        String baseSymbol = Config.BASE_COIN;
        double balance = accountBalance.getCoinBalance(baseSymbol).doubleValue();
        requestedBuyingAmount = (balance * MACrossesConstants.AVAILABLE_BALANCE_PERCENTAGE) / 100;

    }

    private boolean rsiAboveSMA(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currRsi = realTimeData.getRSIValueAtIndex(index);
        double rSISmaValue = realTimeData.getFastSmaValue(index);
        return currRsi > rSISmaValue;
    }

    @Override
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    @Override
    public void positionClosed() {
        positionHandler = null;
    }
}
