package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;

public class MACDOverRSILongExitStrategy1 implements ExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastIndex())).compareTo(realTimeData.getCurrentPrice()) > Config.ZERO) {
			//todo: do stuff - exit with market
		}
		return null;
	}
}
