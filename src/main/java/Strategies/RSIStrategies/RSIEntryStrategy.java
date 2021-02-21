package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Data.RequestClient;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.ExitStrategy;
import com.binance.client.api.SyncRequestClient;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
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
        BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfClosedCandles(Config.RSI_CANDLE_NUM);
        int lastBarIndex = baseBarSeries.getEndIndex();
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
        RSIIndicator rsi = new RSIIndicator(closePriceIndicator, Config.RSI_CANDLE_NUM);
        //TODO: test{
        BaseBarSeries baseBarSeriesCheck = realTimeData.getLastAmountOfCandles(Config.RSI_CANDLE_NUM);
        int lastBarIndexCheck = baseBarSeriesCheck.getEndIndex();
        ClosePriceIndicator closePriceIndicatorCheck = new ClosePriceIndicator(baseBarSeriesCheck);
        RSIIndicator rsiCheck = new RSIIndicator(closePriceIndicatorCheck, Config.RSI_CANDLE_NUM);
        System.out.println(rsiCheck.getValue(lastBarIndexCheck));
        //TODO}



        if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
            Rule entryRule1 = new CrossedDownIndicatorRule(rsi, Config.RSI_ENTRY_THRESHOLD_1);
            if (entryRule1.isSatisfied(lastBarIndex)) {
                positionInStrategy = PositionInStrategy.POSITION_TWO;
                System.out.println("entryRule1 satisfied");
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
            Rule entryRule2 = new CrossedUpIndicatorRule(rsi, Config.RSI_ENTRY_THRESHOLD_2);
            if (entryRule2.isSatisfied(lastBarIndex)) {
                positionInStrategy = PositionInStrategy.POSITION_THREE;
                System.out.println("entryRule2 satisfied");
            }
            return null;
        } else if (positionInStrategy == PositionInStrategy.POSITION_THREE) {
            Rule entryRule3 = new CrossedUpIndicatorRule(rsi, Config.RSI_ENTRY_THRESHOLD_3);
            if (time_passed_from_position_2 >= 2) {
                time_passed_from_position_2 = 2;
                positionInStrategy = PositionInStrategy.POSITION_TWO;
                return null;
            }
            time_passed_from_position_2++;
            if (entryRule3.isSatisfied(lastBarIndex)) {
                time_passed_from_position_2 = 0;
                positionInStrategy = PositionInStrategy.POSITION_ONE;
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol,Config.LEVERAGE);
                String buyingQty = getBuyingQtyAsString(realTimeData);
                System.out.println("buying+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
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
