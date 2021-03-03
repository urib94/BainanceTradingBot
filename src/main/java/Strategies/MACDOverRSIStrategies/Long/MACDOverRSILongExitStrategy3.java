package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSILongExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && currentCandleBiggerThanPrev(realTimeData)) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE, Config.ZERO,true);
		} else if (realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > Config.ZERO && urisRuleOfExit(realTimeData)){
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING,
					MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,
					MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE,false);
		}
		return null;
	}
}
