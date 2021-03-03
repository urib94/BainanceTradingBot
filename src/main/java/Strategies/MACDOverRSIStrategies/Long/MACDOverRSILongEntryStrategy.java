package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import SingletonHelpers.BinanceInfo;
import SingletonHelpers.RequestClient;
import Strategies.EntryStrategy;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

public class MACDOverRSILongEntryStrategy implements EntryStrategy {

    double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
    private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;

    @Override
    public PositionHandler run(RealTimeData realTimeData, String symbol) {
        if (BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(Config.CANDLE_NUM)).compareTo(realTimeData.getCurrentPrice()) < Config.ZERO){
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            if (realTimeData.getMacdOverRsiSignalLineValueAtIndex(realTimeData.getLastCloseIndex()) < 0) {
                if (realTimeData.urisRulesOfEntry()) {
                    syncRequestClient.changeInitialLeverage(symbol,leverage);
                    String buyingQty = getBuyingQtyAsString(realTimeData, symbol);
                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                            buyingQty,null,null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                    exitStrategies.add(new MACDOverRSILongExitStrategy1());
                    exitStrategies.add(new MACDOverRSILongExitStrategy2());
                    return new PositionHandler(buyOrder ,exitStrategies);
                }
            }
            if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP,RealTimeData.CandleType.CLOSE,0)) {
                //TODO: buy stuff
                try{
                    syncRequestClient.changeInitialLeverage(symbol,leverage);
                    String buyingQty = getBuyingQtyAsString(realTimeData, symbol);
                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                            buyingQty,null,null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                    exitStrategies.add(new MACDOverRSILongExitStrategy1());
                    exitStrategies.add(new MACDOverRSILongExitStrategy2());
                    return new PositionHandler(buyOrder ,exitStrategies);
                }catch (Exception ignored){}
            }
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
    public void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    private String getBuyingQtyAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal buyingQty = requestedBuyingAmount.multiply(BigDecimal.valueOf(leverage)).divide(realTimeData.getCurrentPrice(), MathContext.DECIMAL32);
        return BinanceInfo.formatQty(buyingQty, symbol);
    }
}
