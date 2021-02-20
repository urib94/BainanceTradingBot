package Strategies.RSIStrategies;

import Data.AccountBalance;
import Data.PrivateConfig;
import Data.RealTimeData;
import Strategies.EntryStrategy;
import Strategies.PositionEntry;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

public class RSIEntryStrategy implements EntryStrategy {
    private int position_in_strategy = 1;
    private int time_passed_from_position_2 = 0;

    /**
     *
     * @param realTimeData - also singleton - the realtimedata from the binance api. list of candles basically.
     * @return PositionEntry if purchased else return null.
     */
    public PositionEntry run(RealTimeData realTimeData) {
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfClosedCandles(PrivateConfig.RSI_CANDLE_NUM);
        int last_bar_index = baseBarSeries.getEndIndex();
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
        RSIIndicator rsi = new RSIIndicator(closePriceIndicator, PrivateConfig.RSI_CANDLE_NUM);

        if (position_in_strategy == 1) {
            Rule entryRule1 = new CrossedDownIndicatorRule(rsi, PrivateConfig.RSI_ENTRY_TRHESHOLD_1);
            if (entryRule1.isSatisfied(last_bar_index)) {
                position_in_strategy++;
            }
            return null;
        } else if (position_in_strategy == 2) {
            Rule entryRule2 = new CrossedUpIndicatorRule(rsi, PrivateConfig.RSI_ENTRY_TRHESHOLD_2);
            if (entryRule2.isSatisfied(last_bar_index)) {
                position_in_strategy++;
            }
            return null;
        } else if (position_in_strategy == 3) {
            Rule entryRule3 = new CrossedUpIndicatorRule(rsi, PrivateConfig.RSI_ENTRY_TRHESHOLD_3);
            if (time_passed_from_position_2 >= 2) {
                time_passed_from_position_2 = 2;
                position_in_strategy = 2;
                return null;
            }
            time_passed_from_position_2++;
            if (entryRule3.isSatisfied(last_bar_index)) {
                time_passed_from_position_2 = 0;
                position_in_strategy = 1;
                return null; //TODO: PositionEntry!
            }
        }
        return null;
    }
}
