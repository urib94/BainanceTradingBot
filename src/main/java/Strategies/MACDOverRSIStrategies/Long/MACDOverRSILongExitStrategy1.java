package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;

public class MACDOverRSILongExitStrategy1 implements ExitStrategy {

	@Override
	public BigDecimal run(RealTimeData realTimeData) {
		if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN,RealTimeData.CandleType.CLOSE,0)) {
			return MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE; //TODO: CHECK WITH URI
		}
		return null;
	}
}
