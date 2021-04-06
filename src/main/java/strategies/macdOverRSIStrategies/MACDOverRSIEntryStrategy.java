package strategies.macdOverRSIStrategies;

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
import strategies.macdOverRSIStrategies.Long.*;
import strategies.macdOverRSIStrategies.Short.*;
import TradingTools.Trailers.TrailingExit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class MACDOverRSIEntryStrategy implements EntryStrategy {


    private double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
    private double requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;
    private final AccountBalance accountBalance;
    private volatile boolean bought = false;
    private double positivePeek = 0;
    private double negativePeek = 0;

    public MACDOverRSIEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("macd");
    }

    @Override
    public synchronized PositionHandler run(DataHolder realTimeData, String symbol) {
        updatePeeks(realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1));
        boolean notInPosition = accountBalance.getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == Config.ZERO;
        if (notInPosition){
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
            if (noOpenOrders){
                double currentPrice = realTimeData.getCurrentPrice();
                boolean currentPriceAboveSma = true;//realTimeData.getSmaValueAtIndex(realTimeData.getLastIndex()) < currentPrice;
                boolean rule1;
                if (negativePeek < -10.0) {
                    rule1 = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, Config.ZERO);
                    if (rule1){
                        if (bought)return null;
                        return buyAndCreatePositionHandler(realTimeData, currentPrice,symbol, PositionSide.LONG);
                    }
                    else {
                        boolean histIsNegative = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) < 0;
                        if (decliningPyramid(realTimeData, DecliningType.POSITIVE) && histIsNegative){
                            if (bought)return null;
                            return buyAndCreatePositionHandler(realTimeData, currentPrice,symbol, PositionSide.LONG);
                        }
                    }
                }
                //else{
                if (positivePeek > 10.0){
                    rule1 = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, Config.ZERO);
                    if (rule1){
                        if (bought)return null;
                        return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT);
                    }
                    else{
                        boolean histIsPositive = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > 0;
                        if (decliningPyramid(realTimeData, DecliningType.NEGATIVE) && histIsPositive){
                            if (bought) return null;
                            return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT);
                        }
                    }
                }
                bought = false;
            }
        }
        return null;
    }


    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide) {//TODO: maybe change market later.
        bought = true;
        if (positionSide == PositionSide.LONG) {
            TelegramMessenger.sendToTelegram("buying long: " + new Date(System.currentTimeMillis()));
            try{
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,leverage);
                String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol,leverage,requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                        buyingQty, String.valueOf(currentPrice),null,null, null,null,null, null, WorkingType.MARK_PRICE, null, NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("buying long: buyOrder: "+ buyOrder + new Date(System.currentTimeMillis()));
                ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                exitStrategies.add(new MACDOverRSILongExitStrategy1(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.LONG)));
                exitStrategies.add(new MACDOverRSILongExitStrategy2(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.LONG)));
                exitStrategies.add(new MACDOverRSILongExitStrategy3(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.LONG)));
                exitStrategies.add(new MACDOverRSILongExitStrategy4(new TrailingExit(currentPrice, MACDOverRSIConstants.CONSTANT_TRAILING_PERCENTAGE, PositionSide.LONG)));
                exitStrategies.add(new MACDOverRSILongExitStrategy5(new TrailingExit(currentPrice, MACDOverRSIConstants.PROFIT_TRAILING_PERCENTAGE, PositionSide.LONG)));
                //exitStrategies.add(new MACDOverRSILongExitStrategy6(realTimeData));
                return new PositionHandler(buyOrder ,exitStrategies);
            }catch (Exception e){ e.printStackTrace();}
        }
        else{
            try{
                TelegramMessenger.sendToTelegram("buying short: " + new Date(System.currentTimeMillis()));
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,leverage);
                String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol,leverage,requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
                        buyingQty, String.valueOf(currentPrice),null,null, null,null,null,null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("buying short: buyOrder: "+ buyOrder + new Date(System.currentTimeMillis()));
                ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                exitStrategies.add(new MACDOverRSIShortExitStrategy1(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.SHORT)));
                exitStrategies.add(new MACDOverRSIShortExitStrategy2(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.SHORT)));
                exitStrategies.add(new MACDOverRSIShortExitStrategy3(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.SHORT)));
                exitStrategies.add(new MACDOverRSIShortExitStrategy4(new TrailingExit(currentPrice, MACDOverRSIConstants.CONSTANT_TRAILING_PERCENTAGE, PositionSide.SHORT)));
                exitStrategies.add(new MACDOverRSIShortExitStrategy5(new TrailingExit(currentPrice, MACDOverRSIConstants.PROFIT_TRAILING_PERCENTAGE, PositionSide.SHORT)));
                //exitStrategies.add(new MACDOverRSIShortExitStrategy6(realTimeData));
                return new PositionHandler(buyOrder ,exitStrategies);
            }catch (Exception e){e.printStackTrace();}
        }

        return null;
    }

    @Override
    public void setTakeProfitPercentage(double takeProfitPercentage) {
        this.takeProfitPercentage =takeProfitPercentage;
    }

    @Override
    public void setStopLossPercentage(double stopLossPercentage) {
        this.stopLossPercentage = stopLossPercentage;
    }

    @Override
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    public boolean decliningPyramid(DataHolder realTimeData, DecliningType type) {
        boolean rule1;
        boolean rule2;
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiCloseValue();
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -1);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -2);

        if (type == DecliningType.POSITIVE){
            rule1 = currentMacdOverRsiValue > prevMacdOverRsiValue;
            rule2 = prevMacdOverRsiValue > prevPrevMacdOverRsiValue;
        }
        else{
            rule1 = currentMacdOverRsiValue < prevMacdOverRsiValue;
            rule2 = prevMacdOverRsiValue < prevPrevMacdOverRsiValue;
        }
        return rule1 && rule2;
    }

    private void updatePeeks(double macdOverRsiValueAtIndex) {
        if (macdOverRsiValueAtIndex < 0){
            positivePeek = 0;
            if (macdOverRsiValueAtIndex < negativePeek) negativePeek = macdOverRsiValueAtIndex;
        }
        else{
            negativePeek = 0;
            if (macdOverRsiValueAtIndex > positivePeek) positivePeek = macdOverRsiValueAtIndex;
        }
    }

    public enum DecliningType{
        NEGATIVE,
        POSITIVE;
    }

}
