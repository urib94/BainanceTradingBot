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
            if (priceIsAboveSMA(realTimeData) && (longFastTriger(realTimeData) || longSlowTriger(realTimeData) || rSIMACrosse(realTimeData , DataHolder.CrossType.UP))) {
                return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.LONG);
            } else if ( !priceIsAboveSMA(realTimeData) && (shortFastTriger(realTimeData) || shortSlowTriger(realTimeData) || rSIMACrosse(realTimeData, DataHolder.CrossType.DOWN))) {
                return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.SHORT);
            }
        }
        return null;
    }

    private boolean shortFastTriger(DataHolder realTimeData) {
        if (outOfCloseBoliingers(realTimeData)) {
            if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
                return !volumeSpike(realTimeData);
            }
        }
        return false;
    }

    private boolean shortSlowTriger(DataHolder realTimeData){
        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
            System.out.println("slow short triger");
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean longFastTriger(DataHolder realTimeData) {
        if (outOfCloseBoliingers(realTimeData)) {
            if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT )) {
                return !volumeSpike(realTimeData);
            }
        }
        return false;
    }



    private boolean longSlowTriger(DataHolder realTimeData) {
        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
            System.out.println("slow long triger");
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean rSIMACrosse(DataHolder realTimeData, DataHolder.CrossType crossType){
        int index = realTimeData.getLastCloseIndex();
        double prevFastMa = realTimeData.getFastSmaOverRSIValue(index - 1);
        double currFastMa = realTimeData.getFastSmaOverRSIValue(index);
        double prevSlowMa = realTimeData.getSmaOverRSIValue(index - 1);
        double currSlowMa = realTimeData.getSmaOverRSIValue(index);
        switch (crossType){

            case UP:
                return prevFastMa <= prevSlowMa && currFastMa > currSlowMa;

            case DOWN:
                return prevFastMa >= prevSlowMa && currFastMa < currSlowMa;
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
                if(candleCount == MACrossesConstants.SMA_OVER_RSI_BAR_COUNT) {
                    smaCurrValue = realTimeData.getSmaOverRSIValue(closeIndex);
                    smaPrevValue = realTimeData.getSmaOverRSIValue(closeIndex - 1);
                } else {
                    if (candleCount == MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT) {
                        smaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
                        smaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
                    }
                }
                break;
            case MFI:
                curr = realTimeData.getMFIValue(closeIndex);
                prev = realTimeData.getMFIValue(closeIndex - 1);
                smaCurrValue = realTimeData.getSmaOverMFIValue(closeIndex);
                smaPrevValue = realTimeData.getSmaOverMFIValue(closeIndex - 1);
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
        double smaValue = realTimeData.getSmaValueAtIndex(index);
        double smaPrevValue = realTimeData.getSmaValueAtIndex(index - 1);
        double closeValue = realTimeData.getClosePriceAtIndex(index);
        double closePrevValue = realTimeData.getClosePriceAtIndex(index - 1);
        System.out.println("curr sma = " + smaValue + "price = " +closeValue);
        return closeValue > smaValue || closePrevValue > smaPrevValue;
    }

    private PositionHandler buyAndCreatePositionHandler(double currentPrice, String symbol, PositionSide positionSide) {
        updateBuyingAmount(symbol);
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
        SkippingExitTrailer slowSkippingExitTrailer = new SkippingExitTrailer(MACrossesConstants.SLOW_SKIPPING_TRAILING_PERCENTAGE, positionSide);
        SkippingExitTrailer fastSkippingExitTrailer = new SkippingExitTrailer(MACrossesConstants.FAST_SKIPPING_TRAILING_PERCENTAGE, positionSide);
//        ExitTrailer exitTrailer = new ExitTrailer(currentPrice, MACrossesConstants.TRAILING_PERCENTAGE, positionSide);
        exitStrategies.add(new MACrossesExitStrategy1(positionSide, slowSkippingExitTrailer, fastSkippingExitTrailer));
//        exitStrategies.add(new MACrossesExitStrategy2(positionSide, exitTrailer));
//        exitStrategies.add(new MACrossesExitStrategy3(positionSide));
        exitStrategies.add(new MACrossesExitStrategy5(positionSide));
        if (positionSide == PositionSide.LONG) {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
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
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
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
        requestedBuyingAmount = (balance * MACrossesConstants.AVAILABLE_BALANCE_PRECENTAGE) / 100;

    }

    private boolean rsiAboveSMA(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currRsi = realTimeData.getRSIValueAtIndex(index);
        double rSISmaValue = realTimeData.getSmaOverRSIValue(index);
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
