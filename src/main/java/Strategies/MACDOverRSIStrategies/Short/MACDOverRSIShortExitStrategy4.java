package Strategies.MACDOverRSIStrategies.Short;

import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;

public class MACDOverRSIShortExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && !currentCandleBiggerThanPrev(realTimeData)) {
			//do something
		} else if(realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > 0 && currentCandleBiggerThanPrev(realTimeData)) {
			//do something else
		}
		return null;
	}
}
