package Strategies.RSIStrategies;

import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;

import java.math.BigDecimal;

public class RSIExitStrategy4 implements ExitStrategy {
	public BigDecimal run(RealTimeData realTimeData) {
		AccountBalance accountBalance = AccountBalance.getAccountBalance();
		RSIIndicator rsiIndicator = realTimeData.getRSIOpenIndicator();
		int lastBarIndex = realTimeData.getRealTimeData().getEndIndex();
		Rule exitRule = new CrossedDownIndicatorRule(rsiIndicator, Config.RSI_EXIT_OPTION_4_UNDER_THRESHOLD);
		if (exitRule.isSatisfied(lastBarIndex)) {
			return Config.RSI_EXIT_OPTION_4_SELLING_PERCENTAGE;
		}
		return null;
	}
}
