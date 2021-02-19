package Strategies;

import Data.AccountBalance;
import Data.PrivateConfig;
import Data.RealTimeData;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class RSIExitStrategy implements ExitStrategy {
	@Override
	public void run(AccountBalance accountBalance, RealTimeData realTimeData) {
		BaseBarSeries baseBarSeries = realTimeData.getRealTimeData();
		ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
		RSIIndicator rsi = new RSIIndicator(closePriceIndicator, PrivateConfig.RSI_CANDLE_NUM);


	}
	private void option1(RSIIndicator rsi, AccountBalance accountBalance, BaseBarSeries baseBarSeries) {
		int last_bar_index = baseBarSeries.getEndIndex();
		Rule exitRule1 = new OverIndicatorRule(rsi,PrivateConfig.RSI_EXIT_OPTION_1_OVER_THRESHOLD_1);
		Rule exitRule2 = new UnderIndicatorRule(rsi,PrivateConfig.RSI_EXIT_OPTION_1_UNDER_THRESHOLD_1);
		Rule exitRule3 = new UnderIndicatorRule(rsi,PrivateConfig.RSI_EXIT_OPTION_1_UNDER_THRESHOLD_2);
		if (exitRule1.isSatisfied(last_bar_index) && exitRule2.isSatisfied(last_bar_index)) {
			//do something
		}
		if (exitRule3.isSatisfied(last_bar_index)) {
			//so something else
		}
	}
	private  void option2(RSIIndicator rsi, AccountBalance accountBalance, BaseBarSeries baseBarSeries) {
		//TODO: implement
	}
}
