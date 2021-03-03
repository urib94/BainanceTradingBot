package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSILongExitStrategy2 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN, RealTimeData.CandleType.OPEN, Config.ZERO)) {
			//todo: consider uris ask.
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,Config.ZERO);
		}
		return null;
	}
}
