package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSIShortExitStrategy2 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP,RealTimeData.CandleType.OPEN, Config.ZERO)) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,Config.ZERO);
		}
		return null;
	}
}
