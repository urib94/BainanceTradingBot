package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;

public class MACDOverRSILongExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && currentCandleBiggerThanPrev(realTimeData)) {
			//do something
		} else if (urisRuleOfExit(realTimeData)){
				//trailing with 0.25
		}
		return null;
	}
}
