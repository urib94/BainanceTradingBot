package Strategies.RSIStrategies;

import Data.RealTimeData;
import Strategies.ExitStrategy;

import java.math.BigDecimal;

public class RSIExitStrategy4 implements ExitStrategy {
	public BigDecimal run(RealTimeData realTimeData) {
		if (realTimeData.crossed(RealTimeData.CrossType.DOWN, RealTimeData.RSIType.OPEN, RSIConstants.RSI_EXIT_OPTION_4_UNDER_THRESHOLD)) {
			System.out.println("Exiting with RSI exit strategy 4!");
			return RSIConstants.RSI_EXIT_OPTION_4_SELLING_PERCENTAGE;
		}
		return null;
	}
}
