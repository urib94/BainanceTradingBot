package Strategies;

import Data.AccountBalance;
import Data.PositionEntry;
import Data.PrivateConfig;
import Data.RealTimeData;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class RSIEntryStrategy implements EntryStrategy {


    /**
     *
     * @param realTimeData - also singleton - the realtimedata from the binance api. list of candles basically.
     * @return PositionEntry if purchased else return null.
     */
    @Override
    public PositionEntry run(RealTimeData realTimeData) {
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        BaseBarSeries baseBarSeries = realTimeData.getRealTimeData();
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
        RSIIndicator rsi_full = new RSIIndicator(closePriceIndicator, PrivateConfig.RSI_CANDLE_NUM);
        RSIIndicator rsi_semi= new RSIIndicator(closePriceIndicator, PrivateConfig.RSI_CANDLE_NUM - PrivateConfig.AMOUNT_OF_CANDLES_TO_IDENTIFY_TREND_RSI);
        Rule entryRule1 = new UnderIndicatorRule(rsi_full, PrivateConfig.RSI_ENTRY_TRHESHOLD_1);
        Rule entryRule2 = new OverIndicatorRule(rsi_full, PrivateConfig.RSI_ENTRY_TRHESHOLD_2);
        Rule entryRule3Helper1 = new OverIndicatorRule(rsi_semi, PrivateConfig.RSI_ENTRY_TRHESHOLD_3);
        Rule entryRule3Helper2 = new OverIndicatorRule(rsi_semi, PrivateConfig.RSI_ENTRY_TRHESHOLD_2);
        int last_bar_index = baseBarSeries.getEndIndex();
        if (entryRule1.isSatisfied(last_bar_index)) {
            // @TODO: implement
            return null;
        } else if (entryRule2.isSatisfied(last_bar_index)) {
            // @TODO: implement
            return null;
        } else if (entryRule3Helper1.isSatisfied(last_bar_index) && entryRule3Helper2.isSatisfied(last_bar_index)) {
            return null;
        }
        return null;
    }
}
