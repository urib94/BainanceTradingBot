package Strategies;

import Data.AccountBalance;
import Data.PrivateConfig;
import Data.RealTimeData;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

public class RSIExitStrategy implements ExitStrategy {
	//TODO: implement as different ExitStrategy.
	private EXIT_STRATEGY_TYPE exitStrategyType = null;
	private int positionInStrategy = 1;
	@Override
	public PositionAction run(AccountBalance accountBalance, RealTimeData realTimeData) {
		BaseBarSeries baseBarSeries = realTimeData.getRealTimeData();
		ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
		if (exitStrategyType == null) {
			Rule typeOneRule = new CrossedUpIndicatorRule(closePriceIndicator,PrivateConfig.RSI_EXIT_OPTION_1_OVER_THRESHOLD1);
			Rule typeTwoRule = new CrossedUpIndicatorRule(closePriceIndicator,PrivateConfig.RSI_EXIT_OPTION_2_OVER_THRESHOLD1);
		} else if (exitStrategyType == EXIT_STRATEGY_TYPE.TYPE_ONE) {
			//Check with 60
		} else if (exitStrategyType == EXIT_STRATEGY_TYPE.TYPE_TWO) {
			//Check with 70.
		}



		RSIIndicator rsi = new RSIIndicator(closePriceIndicator, PrivateConfig.RSI_CANDLE_NUM);



	}

	private  void option2(RSIIndicator rsi, AccountBalance accountBalance, BaseBarSeries baseBarSeries) {
		//TODO: implement
	}
}
