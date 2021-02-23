package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import java.math.BigDecimal;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	public BigDecimal run(RealTimeData realTimeData) {
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE,Config.RSI_EXIT_OPTION_1_OVER_THRESHOLD1)
					&& !(realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE,Config.RSI_EXIT_OPTION_2_OVER_THRESHOLD1))) {
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE,Config.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1)) {
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				return Config.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE;
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE,Config.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2)) {
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				return Config.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE;
			}
		}
		return null;
	}

}
