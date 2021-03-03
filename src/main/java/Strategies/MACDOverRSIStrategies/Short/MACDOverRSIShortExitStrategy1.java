package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;

public class MACDOverRSIShortExitStrategy1 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastIndex())).compareTo(realTimeData.getCurrentPrice()) < Config.ZERO) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,Config.ZERO);
		}
		return null;
	}
}
