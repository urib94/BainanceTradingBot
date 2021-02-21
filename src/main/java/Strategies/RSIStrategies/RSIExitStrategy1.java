package Strategies.RSIStrategies;

import Data.AccountBalance;
import Positions.PositionAction;
import Data.PrivateConfig;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	public PositionAction run(RealTimeData realTimeData) {
		AccountBalance accountBalance = AccountBalance.getAccountBalance();
		BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfClosedCandles(PrivateConfig.RSI_CANDLE_NUM);
		int last_bar_index = baseBarSeries.getEndIndex();
		ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
		RSIIndicator rsi = new RSIIndicator(closePriceIndicator, PrivateConfig.RSI_CANDLE_NUM);
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			Rule exitRule1 = new CrossedUpIndicatorRule(rsi, PrivateConfig.RSI_EXIT_OPTION_1_OVER_THRESHOLD1);
			if (exitRule1.isSatisfied(last_bar_index)) {
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			Rule exitRule2 = new CrossedDownIndicatorRule(rsi,PrivateConfig.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1);
			if (exitRule2.isSatisfied(last_bar_index)) {
				//TODO: return PositionAction of selling 50% of position
				positionInStrategy = PositionInStrategy.POSITION_THREE;
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			Rule exitRule3 = new CrossedDownIndicatorRule(rsi,PrivateConfig.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2);
			if (exitRule3.isSatisfied(last_bar_index)) {
				//TODO: return PositionAction of selling the rest of position
				positionInStrategy = PositionInStrategy.POSITION_ONE;
			}
		}
		return null;
	}

}
