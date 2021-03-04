package Strategies.RSIStrategies;
import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.PositionInStrategy;

import java.math.BigDecimal;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	/**
	 * Returns if the rsi Indicator went above 73 and then crossed down 60, and then to 50.
	 * @param realTimeData
	 * @return the percentage for selling. null if no sell.
	 */
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			if (realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_OVER_THRESHOLD1)
					&& !(realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_OVER_THRESHOLD1))) {
				System.out.println(this.getClass().getSimpleName() + "Switching to Position 2");
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			if ( ! realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1)) {
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				System.out.println(this.getClass().getSimpleName() + " Switching to Position 3. Returning 50% ");
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE1, Config.ZERO);
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			if (! realTimeData.above(RealTimeData.IndicatorType.RSI,RealTimeData.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2)) {
				System.out.println(this.getClass().getSimpleName() + " Switching to Position 1. Returning 100% ");
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE2, Config.ZERO);
			}
		}
		return null;
	}
}
