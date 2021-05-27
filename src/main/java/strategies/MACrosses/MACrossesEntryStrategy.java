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
import singletonHelpers.BinanceInfo;
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
    String symbol = MACrossesConstants.SYMBOL;



    public MACrossesEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("MFI & RSI SMA Crosses");
    }


    @Override
    public PositionHandler run(DataHolder realTimeData, String symbol) {
        if (positionHandler == null){
            double currentPrice = realTimeData.getCurrentPrice();
            if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG);
            } else {
                if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)){
                    return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT);
                }
            }
        }
        return null;
    }


    private boolean crossedSma(DataHolder realTimeData, DataHolder.IndicatorType indicatorType, DataHolder.CrossType crossType, int cundleCount){
        int closeIndex = realTimeData.getLastCloseIndex();
        double prev = 0, curr = 0;
        double smaCurrValue = 0, smaPrevValue = 0;
        switch(indicatorType){
            case RSI:
                curr = realTimeData.getRSIValueAtIndex(closeIndex);
                prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
                if(cundleCount == MACrossesConstants.SMA_OVER_RSI_BAR_COUNT) {
                    smaCurrValue = realTimeData.getSmaOverRSIValue(closeIndex);
                    smaPrevValue = realTimeData.getSmaOverRSIValue(closeIndex - 1);
                } else {
                    if (cundleCount == MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT) {
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
                return prev <= smaPrevValue && curr > smaCurrValue;
            case DOWN:
                return prev >= smaPrevValue && curr < smaCurrValue;
        }
        return false;
    }


    public static boolean priceIsAboveSMA(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double smaValue = realTimeData.getSmaValueAtIndex(index);
        double smaPrevValue = realTimeData.getSmaValueAtIndex(index - 1);
        double closeValue = realTimeData.getClosePriceAtIndex(index);
        double closePrevValue = realTimeData.getClosePriceAtIndex(index - 1);
        return closeValue > smaValue || closePrevValue > smaPrevValue;
    }

    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide) {
        updateBuyingAmount(symbol);
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
        SkippingExitTrailer skippingExitTrailer = new SkippingExitTrailer(MACrossesConstants.SKIPPING_TRAILING_PERCENTAGE, positionSide);
        ExitTrailer exitTrailer = new ExitTrailer(currentPrice, MACrossesConstants.TRAILING_PERCENTAGE, positionSide);
        exitStrategies.add(new MACrossesExitStrategy1(positionSide, skippingExitTrailer));
        exitStrategies.add(new MACrossesExitStrategy2(positionSide, exitTrailer));
        exitStrategies.add(new MACrossesExitStrategy3(positionSide));
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
                TelegramMessenger.sendToTelegram("bought short:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
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

    private void postStopLoss(double qty, double averagePrice, PositionSide positionSide, String symbol) {
        do
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, symbol));
        String stopLossPrice = BinanceInfo.formatPrice(, symbol);
        switch (positionSide) {
            case LONG:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, "true", null, stopLossPrice, null,
                            null, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case SHORT_DCA_LIMIT:

            case SHORT_DCA_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, "true", null, stopLossPrice, null,
                            null, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
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
