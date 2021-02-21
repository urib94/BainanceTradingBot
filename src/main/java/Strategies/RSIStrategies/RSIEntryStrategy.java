package Strategies.RSIStrategies;

import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Data.RequestClient;
import Positions.PositionAction;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.ExitStrategy;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.OrderSide;
import com.binance.client.api.model.trade.Order;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import java.math.BigDecimal;
import java.util.ArrayList;

public class RSIEntryStrategy implements EntryStrategy {
    private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;
    private int time_passed_from_position_2 = 0;
    private ArrayList<ExitStrategy> exitStrategies;

    /**
     *
     * @param realTimeData - also singleton - the realtimedata from the binance api. list of candles basically.
     * @return PositionEntry if purchased else return null.
     */
    public PositionHandler run(RealTimeData realTimeData,String symbol) {
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfClosedCandles(Config.RSI_CANDLE_NUM);
        int last_bar_index = baseBarSeries.getEndIndex();
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
        RSIIndicator rsi = new RSIIndicator(closePriceIndicator, Config.RSI_CANDLE_NUM);

        if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
            Rule entryRule1 = new CrossedDownIndicatorRule(rsi, Config.RSI_ENTRY_TRHESHOLD_1);
            if (entryRule1.isSatisfied(last_bar_index)) {
                positionInStrategy = PositionInStrategy.POSITION_TWO;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
            Rule entryRule2 = new CrossedUpIndicatorRule(rsi, Config.RSI_ENTRY_TRHESHOLD_2);
            if (entryRule2.isSatisfied(last_bar_index)) {
                positionInStrategy = PositionInStrategy.POSITION_THREE;
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_THREE) {
            Rule entryRule3 = new CrossedUpIndicatorRule(rsi, Config.RSI_ENTRY_TRHESHOLD_3);
            if (time_passed_from_position_2 >= 2) {
                time_passed_from_position_2 = 2;
                positionInStrategy = PositionInStrategy.POSITION_TWO;
                return null;
            }
            time_passed_from_position_2++;
            if (entryRule3.isSatisfied(last_bar_index)) {
                time_passed_from_position_2 = 0;
                positionInStrategy = PositionInStrategy.POSITION_ONE;
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,Config.leverage);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.LONG, OrderType.LIMIT, TimeInForce.GTC,
                        sellingQty,positionAction.getPrice().toString(),"true",null, null,null,null);
                return new PositionHandler(buyOrder,Config.leverage, exitStrategies);

            }
        }
        return null;
    }
}
