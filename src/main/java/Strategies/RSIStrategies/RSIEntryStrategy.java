package Strategies.RSIStrategies;

import Data.*;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.ExitStrategy;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class RSIEntryStrategy implements EntryStrategy {
    private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;
    private int time_passed_from_position_2 = 0;
    private ArrayList<ExitStrategy> exitStrategies;
    double rsiValueToCheckForPosition3 = -1;
    private boolean once = true ;


    public RSIEntryStrategy(){
        exitStrategies = new ArrayList<>();
        exitStrategies.add(new RSIExitStrategy1());
        exitStrategies.add(new RSIExitStrategy2());
        exitStrategies.add(new RSIExitStrategy3());
        exitStrategies.add(new RSIExitStrategy4());
    }
    /**
     *
     * @param realTimeData - also singleton - the real time data from the binance api. list of candles basically.
     * @return PositionEntry if purchased else return null.
     */
    public PositionHandler run(RealTimeData realTimeData,String symbol) {
        //TODO: check
        if (once){
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.changeInitialLeverage(Config.SYMBOL,Config.LEVERAGE);
            syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
            System.out.println("canceled orders ");
            once = false;
            String buyingQty = getBuyingQtyAsString(realTimeData, symbol);
            try{
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                    buyingQty,realTimeData.getCurrentPrice().toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                String takeProfitPrice = getTakeProfitPriceAsString(realTimeData, symbol);
                Order takeProfitOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                        buyingQty,takeProfitPrice,null,null, takeProfitPrice,null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                System.out.println("take profit");
                String stopLossPrice = getStopLossPriceAsString(realTimeData, symbol);
                Order stopLossOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.STOP, TimeInForce.GTC,
                        buyingQty,stopLossPrice,null,null, stopLossPrice,null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                System.out.println("stop loss");
                System.out.println(buyOrder);
                return new PositionHandler(buyOrder, null, null, null, null, Config.LEVERAGE, exitStrategies);
                //return new PositionHandler(buyOrder, takeProfitOrder.getClientOrderId(), takeProfitOrder.getOrderId(), stopLossOrder.getClientOrderId(), stopLossOrder.getOrderId(), Config.LEVERAGE, exitStrategies);
            }catch (Exception e){
                System.out.println("exception in RSI: " + e);
            }
        }



//        if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
//            if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_1)) {
//                positionInStrategy = PositionInStrategy.POSITION_TWO;
//            }
//            return null;
//        } else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
//            if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_2)) {
//                rsiValueToCheckForPosition3 = realTimeData.calculateCurrentClosedRSIValue();
//                positionInStrategy = PositionInStrategy.POSITION_THREE;
//            }
//            return null;
//        } else if (positionInStrategy == PositionInStrategy.POSITION_THREE) {
//            if (time_passed_from_position_2 >= 2) {
//                time_passed_from_position_2 = 0;
//                rsiValueToCheckForPosition3 = -1;
//                positionInStrategy = PositionInStrategy.POSITION_TWO;
//                return null;
//            }
//            if(! realTimeData.currentRSIValueEquals(RealTimeData.RSIType.CLOSE, rsiValueToCheckForPosition3)) {
//                time_passed_from_position_2 ++;
//            }
//            if (realTimeData.above(RealTimeData.RSIType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_3)) {
//                time_passed_from_position_2 = 0;
//                positionInStrategy = PositionInStrategy.POSITION_ONE;
//                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                String buyingQty = getBuyingQtyAsString(realTimeData, symbol);
//                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
//                        buyingQty,realTimeData.getCurrentPrice().toString(),null,null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
//                System.out.println(buyOrder);
//                return new PositionHandler(buyOrder, stopLossOrder.getClientOrderId(), Config.LEVERAGE, exitStrategies);
//            }
//        }
        return null;
    }
    private String getBuyingQtyAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal buyingQty = Config.BUYING_AMOUNT_REQUESTED.multiply(BigDecimal.valueOf(Config.LEVERAGE.doubleValue())).divide(realTimeData.getCurrentPrice(), MathContext.DECIMAL32);
        return BinanceInfo.formatQty(buyingQty, symbol);
    }

    private String getTakeProfitPriceAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal takeProfitPrice = realTimeData.getCurrentPrice().add((realTimeData.getCurrentPrice().multiply(BigDecimal.valueOf(RSIConstants.TAKE_PROFIT_PERCENTAGE))));
        return BinanceInfo.formatPrice(takeProfitPrice, symbol);
    }

    private String getStopLossPriceAsString(RealTimeData realTimeData, String symbol) {
        BigDecimal stopLossPrice = realTimeData.getCurrentPrice().subtract(realTimeData.getCurrentPrice().multiply(BigDecimal.valueOf(RSIConstants.STOP_LOSS_PERCENTAGE)));
        return BinanceInfo.formatPrice(stopLossPrice, symbol);
    }

}
