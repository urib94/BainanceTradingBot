package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import java.math.BigDecimal;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	public BigDecimal run(RealTimeData realTimeData) {
		RSIIndicator rsiIndicator = realTimeData.getRSICloseValue();
		int lastBarIndex = realTimeData.getRealTimeData().getEndIndex();
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			Rule exitRule1 = (new CrossedUpIndicatorRule(rsiIndicator, Config.RSI_EXIT_OPTION_1_OVER_THRESHOLD1)) //Go over 65
					.and(new CrossedDownIndicatorRule(rsiIndicator, Config.RSI_EXIT_OPTION_2_OVER_THRESHOLD1)); // Don't go over 73.
			if (exitRule1.isSatisfied(lastBarIndex)) {
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			Rule exitRule2 = new CrossedDownIndicatorRule(rsiIndicator, Config.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1);
			if (exitRule2.isSatisfied(lastBarIndex)) {
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				return Config.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE;
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			Rule exitRule3 = new CrossedDownIndicatorRule(rsiIndicator, Config.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2);
			if (exitRule3.isSatisfied(lastBarIndex)) {
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				return Config.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE;
			}
		}
		return null;
	}

}
