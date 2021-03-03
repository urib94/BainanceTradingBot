package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;

public class MACDOverRSILongExitStrategy3 implements ExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (!isTrailing) {

		}
		return null;
	}
}
