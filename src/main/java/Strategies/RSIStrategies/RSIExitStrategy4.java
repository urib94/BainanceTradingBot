package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;

import java.math.BigDecimal;

public class RSIExitStrategy4 implements ExitStrategy {
	/**
	 * Safety mechanism - sells everything if current open RSI value below 30.
	 * @param realTimeData
	 * @return the percentage of quantity to sell, null otherwise.
	 */
	public SellingInstructions run(RealTimeData realTimeData) {
		System.out.println("rsi open value: " + realTimeData.getRsiOpenValue());
		if (!(realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.OPEN, RSIConstants.RSI_EXIT_OPTION_4_UNDER_THRESHOLD))) {
			System.out.println("Exiting with RSI exit strategy 4!");
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, RSIConstants.RSI_EXIT_OPTION_4_SELLING_PERCENTAGE);
		}
		return null;
	}
}
