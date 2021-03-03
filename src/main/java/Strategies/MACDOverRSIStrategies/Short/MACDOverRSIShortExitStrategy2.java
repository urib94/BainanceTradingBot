package Strategies.MACDOverRSIStrategies.Short;

import Data.RealTimeData;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;

public class MACDOverRSIShortExitStrategy2 implements ExitStrategy {

	@Override
	public BigDecimal run(RealTimeData realTimeData) {
		if (realTimeData.urisRulesOfEntry()) {
			return MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE; //TODO: check with Uri
		}
		return null;
	}
}
