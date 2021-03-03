package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;

public class MACDOverRSILongExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && ! (currentCandleBiggerThanPrev(realTimeData))) {
			//cancel trailing
		} else if (realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) < 0 && currentCandleBiggerThanPrev(realTimeData)) {
			//do something
		}
		return null;
	}
}
