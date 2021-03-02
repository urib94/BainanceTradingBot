package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.ExitStrategy;

import java.math.BigDecimal;

public class MACDOverRSIExitStrategy implements ExitStrategy {
	@Override
	public BigDecimal run(RealTimeData realTimeData) {
		double currentMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex());
		double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1);
		double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-2);
		if (currentMacdOverRsiValue < MACDOverRSIConstants.MACD_OVER_RSI_EXIT_THRESHOLD && prevMacdOverRsiValue > MACDOverRSIConstants.MACD_OVER_RSI_EXIT_THRESHOLD) {
			return MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE; //TODO: CHECK WITH URI
		} else {
			boolean rule1 = currentMacdOverRsiValue < 0;
			boolean rule2 = Math.abs(currentMacdOverRsiValue) < Math.abs(prevMacdOverRsiValue);
			boolean rule3 = Math.abs(prevMacdOverRsiValue) < Math.abs(prevPrevMacdOverRsiValue);
			if (rule1 && rule2 && rule3) {
				return MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE; //TODO: check with Uri
			}
		}
		return null;
	}
}
