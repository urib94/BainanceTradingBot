package Strategies.RSIStrategies;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import java.math.BigDecimal;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	/**
	 * Returns if the rsi Indicator went above 73 and then crossed down 60, and then to 50.
	 * @param realTimeData
	 * @return the percentage for selling. null if no sell.
	 */
	public BigDecimal run(RealTimeData realTimeData) {
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			if (realTimeData.above(RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_OVER_THRESHOLD1)
					&& !(realTimeData.above(RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_OVER_THRESHOLD1))) {
				System.out.println(this.getClass().getSimpleName() + "Switching to Position 2");
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			if ( ! realTimeData.above(RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1)) {
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				System.out.println(this.getClass().getSimpleName() + " Switching to Position 3. Returning 50% ");
				return RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE1;
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			if (! realTimeData.above(RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2)) {
				System.out.println(this.getClass().getSimpleName() + " Switching to Position 1. Returning 100% ");
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				return RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE2;
			}
		}
		return null;
	}
}
