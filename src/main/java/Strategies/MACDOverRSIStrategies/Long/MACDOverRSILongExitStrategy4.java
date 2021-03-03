package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSILongExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && ! (currentCandleBiggerThanPrev(realTimeData))) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,0,true);
		} else if (realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) < 0 && currentCandleBiggerThanPrev(realTimeData)) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING,MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,0.25,false);
		}
		return null;
	}
}
