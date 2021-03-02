package Strategies.RSIStrategies;

import Data.*;
import SingletonHelpers.BinanceInfo;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.ExitStrategy;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import SingletonHelpers.RequestClient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

public class RSIEntryStrategy implements EntryStrategy {
    double takeProfitPercentage = RSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = RSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = RSIConstants.DEFAULT_LEVERAGE;
    private  BigDecimal requestedBuyingAmount = RSIConstants.DEFAULT_BUYING_AMOUNT;
    private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;
    private int time_passed_from_position_2 = 0;
    double rsiValueToCheckForPosition3 = -1;

    public RSIEntryStrategy(double stopLossPercentage, int leverage, BigDecimal requestedBuyingAmount) {
        this.stopLossPercentage = stopLossPercentage;
        this.leverage = leverage;
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    public RSIEntryStrategy(){//TODO:complete default values constructor

    }

    public synchronized PositionHandler run(RealTimeData realTimeData,String symbol) {
        if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
            if (realTimeData.crossed(RealTimeData.IndicatorType.RSI,RealTimeData.CrossType.DOWN, RealTimeData.CandleType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_1)) {
                positionInStrategy = PositionInStrategy.POSITION_TWO;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
            if (realTimeData.crossed(RealTimeData.IndicatorType.RSI,RealTimeData.CrossType.UP, RealTimeData.CandleType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_2)) {
                rsiValueToCheckForPosition3 = realTimeData.getRsiCloseValue();
                positionInStrategy = PositionInStrategy.POSITION_THREE;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_THREE) {
            if (time_passed_from_position_2 >= 2) {
                time_passed_from_position_2 = 0;
                rsiValueToCheckForPosition3 = -1;
                positionInStrategy = PositionInStrategy.POSITION_TWO;
                return null;
            }
            if(rsiValueToCheckForPosition3 != realTimeData.getRsiCloseValue()) {
                time_passed_from_position_2 ++;
            }
            if (realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_3)) { //TODO:fix
                time_passed_from_position_2 = 0;
                positionInStrategy = PositionInStrategy.POSITION_ONE;
                rsiValueToCheckForPosition3 = -1;
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,leverage);
                String buyingQty = getBuyingQtyAsString(realTimeData, symbol);
                try{
                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                            buyingQty,null,null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);//TODO: check if buying with market price is ok.
                    String takeProfitPrice = getTakeProfitPriceAsString(realTimeData, symbol);
                    Order takeProfitOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                            buyingQty,takeProfitPrice,null,null, takeProfitPrice,null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    String stopLossPrice = getStopLossPriceAsString(realTimeData, symbol);
                    Order stopLossOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.STOP, TimeInForce.GTC,
                            buyingQty,stopLossPrice,null,null, stopLossPrice,null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    System.out.println("\n\n++++++++++++++++++++Buying+++++++++++++++++++");
                    System.out.println("Buy order: " + buyOrder);
                    ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                    exitStrategies.add(new RSIExitStrategy1());
                    exitStrategies.add(new RSIExitStrategy2());
                    exitStrategies.add(new RSIExitStrategy3());
                    exitStrategies.add(new RSIExitStrategy4());
                    return new PositionHandler(buyOrder, exitStrategies);
                }catch (Exception e){System.out.println("exception in RSI: " + e);}
            }
        }
        return null;
    }

    public void setTakeProfitPercentage(double takeProfitPercentage) {
        this.takeProfitPercentage = takeProfitPercentage;
    }

    public void setStopLossPercentage(double stopLossPercentage) {
        this.stopLossPercentage = stopLossPercentage;
    }

    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    public void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }
    private String getBuyingQtyAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal buyingQty = requestedBuyingAmount.multiply(BigDecimal.valueOf(leverage)).divide(realTimeData.getCurrentPrice(), MathContext.DECIMAL32);
        return BinanceInfo.formatQty(buyingQty, symbol);
    }

    private String getTakeProfitPriceAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal takeProfitPrice = realTimeData.getCurrentPrice().add((realTimeData.getCurrentPrice().multiply(BigDecimal.valueOf(takeProfitPercentage))));
        return BinanceInfo.formatPrice(takeProfitPrice, symbol);
    }

    private String getStopLossPriceAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal stopLossPrice = realTimeData.getCurrentPrice().subtract(realTimeData.getCurrentPrice().multiply(BigDecimal.valueOf(stopLossPercentage)));
        return BinanceInfo.formatPrice(stopLossPrice, symbol);
    }

}
