package Strategies.MACDOverRSIStrategies.Short;

import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;

public class MACDOverRSIShortExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && currentCandleBiggerThanPrev(realTimeData)) {
			//cancel training
		} else if (urisRuleOfExit(realTimeData)) {
			//do something
		}
		return null;
	}
}
