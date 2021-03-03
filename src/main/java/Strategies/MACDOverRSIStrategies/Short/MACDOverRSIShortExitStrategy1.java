package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;

import java.math.BigDecimal;

public class MACDOverRSIShortExitStrategy1 implements ExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastIndex())).compareTo(realTimeData.getCurrentPrice()) < Config.ZERO) {
			//todo: do stuff
		}
		return null;
	}
}
