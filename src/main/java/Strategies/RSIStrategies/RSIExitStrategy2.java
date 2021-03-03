package Strategies.RSIStrategies;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.PositionInStrategy;

import java.math.BigDecimal;

public class RSIExitStrategy2 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	/**
	 * Checks if the current close of RSIIndicator value is above 73, and then below 70 and then below 60.
	 * @param realTimeData
	 * @return the percentage of quantity to sell, null otherwise.
	 */
	public SellingInstructions run(RealTimeData realTimeData) {
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			if (realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_OVER_THRESHOLD1)) {
				System.out.println(this.getClass().getSimpleName() + "Switching to Position 2 ");
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			if (! realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_UNDER_THRESHOLD1)) {
				System.out.println(this.getClass().getSimpleName() + "Switching to Position 3. Returning 40% ");
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL, RSIConstants.RSI_EXIT_OPTION_2_SELLING_PERCENTAGE1, Config.ZERO);

			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			if (! realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_UNDER_THRESHOLD2)) {
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				System.out.println(this.getClass().getSimpleName() + "Switching to Position 1. Returning 100% ");
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL, RSIConstants.RSI_EXIT_OPTION_2_SELLING_PERCENTAGE2, Config.ZERO);
			}
		}
		return null;
	}
}
