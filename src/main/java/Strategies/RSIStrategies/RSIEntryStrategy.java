package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Data.RequestClient;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.ExitStrategy;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;

public class RSIEntryStrategy implements EntryStrategy {
    private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;
    private int time_passed_from_position_2 = 0;
    private ArrayList<ExitStrategy> exitStrategies;


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
        if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
            if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_1)) {
                positionInStrategy = PositionInStrategy.POSITION_TWO;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
            if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_2)) {
                positionInStrategy = PositionInStrategy.POSITION_THREE;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_THREE) {
            if (time_passed_from_position_2 >= 2) {
                time_passed_from_position_2 = 0;
                positionInStrategy = PositionInStrategy.POSITION_TWO;
                return null;
            }
            time_passed_from_position_2++;
            if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_ENTRY_THRESHOLD_3)) {
                time_passed_from_position_2 = 0;
                positionInStrategy = PositionInStrategy.POSITION_ONE;
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,Config.LEVERAGE);
                String buyingQty = getBuyingQtyAsString(realTimeData);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.LONG, OrderType.LIMIT, TimeInForce.GTC,
                       buyingQty,realTimeData.getCurrentPrice().toString(),null,null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                String takeProfitPrice = (realTimeData.getCurrentPrice().add(realTimeData.getCurrentPrice().multiply(new BigDecimal(1/1000)))).toString();
                Order takeProfitOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.LONG, OrderType.TAKE_RPOFIT, TimeInForce.GTC,
                        buyingQty,takeProfitPrice,"true",null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                String stopLossPrice = (realTimeData.getCurrentPrice().subtract(realTimeData.getCurrentPrice().multiply(new BigDecimal(1/200)))).toString();
                Order stopLossOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.LONG, OrderType.STOP, TimeInForce.GTC,
                        buyingQty,stopLossPrice,"true",null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                System.out.println("Bought: " + buyOrder.getClientOrderId());
                return new PositionHandler(buyOrder,Config.LEVERAGE, exitStrategies);
            }
        }
        return null;
    }

    private String getBuyingQtyAsString(RealTimeData realTimeData) {
        BigDecimal buyingQty = realTimeData.getCurrentPrice().multiply(Config.BUYING_AMOUNT_REQUESTED).multiply(new BigDecimal(Config.LEVERAGE));
        return buyingQty.toString();
    }
}
