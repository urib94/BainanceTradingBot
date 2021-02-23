package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Data.RequestClient;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.ExitStrategy;
import com.binance.client.api.SyncRequestClient;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

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
            if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE,Config.RSI_ENTRY_THRESHOLD_1)) {
                positionInStrategy = PositionInStrategy.POSITION_TWO;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
            if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE,Config.RSI_ENTRY_THRESHOLD_2)) {
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
            if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE,Config.RSI_ENTRY_THRESHOLD_3)) {
                time_passed_from_position_2 = 0;
                positionInStrategy = PositionInStrategy.POSITION_ONE;
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,Config.LEVERAGE);
                String buyingQty = getBuyingQtyAsString(realTimeData);
                System.out.println("-----BUYING MOTHERFUCKER!!!!!!!------");
//                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.LONG, OrderType.LIMIT, TimeInForce.GTC,
//                       buyingQty,realTimeData.getCurrentPrice().toString(),"false",null, null, WorkingType.MARK_PRICE,NewOrderRespType.RESULT);
//                return new PositionHandler(buyOrder,Config.LEVERAGE, exitStrategies);//TODO: retrieve code
            }
        }
        return null;
    }

    private String getBuyingQtyAsString(RealTimeData realTimeData) {
        BigDecimal buyingQty = realTimeData.getCurrentPrice().multiply(Config.BUYING_AMOUNT_REQUESTED).multiply(new BigDecimal(Config.LEVERAGE));
        return buyingQty.toString();
    }
}
