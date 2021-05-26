package strategies.MACrosses;

import TradingTools.Trailers.ExitTrailer;
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
        if (positionHandler == null){
            double currentPrice = realTimeData.getCurrentPrice();
            if(priceIsAboveSMA(realTimeData)){// long only
                if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP) || crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.UP)){
                    return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG);
                }

            }
            else if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN) || crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.DOWN)){
                    return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT);
            }
        }
        return null;
    }

    private boolean crossedSma(DataHolder realTimeData, DataHolder.IndicatorType indicatorType, DataHolder.CrossType crossType){
        int closeIndex = realTimeData.getLastCloseIndex();
        double smaCurrValue = realTimeData.getSmaValueAtIndex(closeIndex);
        double smaPrevValue = realTimeData.getSmaValueAtIndex(closeIndex - 1);
        double prev = 0, curr = 0;
        switch(indicatorType){
            case RSI:
                curr = realTimeData.getRSIValueAtIndex(closeIndex);
                prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
                break;
            case MFI:
                curr = realTimeData.getMFIValue(closeIndex);
                prev = realTimeData.getMFIValue(closeIndex - 1);
                break;
            default:
                break;
        }
        switch(crossType){
            case UP:
                return prev <= smaPrevValue && curr > smaCurrValue;
            case DOWN:
                return prev >= smaPrevValue && curr < smaCurrValue;
        }
        return false;
    }


    private boolean priceIsAboveSMA(DataHolder realTimeData) {
        double smaValue = realTimeData.getSmaValueAtIndex(realTimeData.getLastCloseIndex());
        return realTimeData.above(DataHolder.IndicatorType.SMA, DataHolder.CandleType.CLOSE, smaValue);
    }

    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide) {
        updateBuyingAmount(symbol);
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
        SkippingExitTrailer skippingExitTrailer = new SkippingExitTrailer(MACrossesConstants.SKIPPING_TRAILING_PERCENTAGE, positionSide);
        ExitTrailer exitTrailer = new ExitTrailer(currentPrice, MACrossesConstants.TRAILING_PERCENTAGE, positionSide);
        exitStrategies.add(new MACrossesExitStrategy1(positionSide, skippingExitTrailer));
        exitStrategies.add(new MACrossesExitStrategy2(positionSide, exitTrailer));
        if (positionSide == PositionSide.LONG) {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                positionHandler = new PositionHandler(buyOrder, exitStrategies);
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
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                positionHandler = new PositionHandler(buyOrder, exitStrategies);
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
