package Strategies.RSIStrategies;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import java.math.BigDecimal;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	public BigDecimal run(RealTimeData realTimeData) {
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			if (realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_OVER_THRESHOLD1)
					&& !(realTimeData.crossed(RealTimeData.CrossType.UP, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_OVER_THRESHOLD1))) {
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1)) {
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				return RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE1;
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2)) {
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				System.out.println("Exiting with RSI exit strategy 1");
				return RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE2;
			}
		}
		return null;
	}
}
