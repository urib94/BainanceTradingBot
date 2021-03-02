package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;

public class MACDOverRSILongExitStrategy2 implements ExitStrategy {

	@Override
	public BigDecimal run(RealTimeData realTimeData) {
		if (realTimeData.urisRulesForEntry()) {
			return MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE; //TODO: check with Uri
		}
		return null;
	}
}
