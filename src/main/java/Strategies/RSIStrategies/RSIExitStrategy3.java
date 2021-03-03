package Strategies.RSIStrategies;
import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.ExitStrategy;

import java.math.BigDecimal;


public class RSIExitStrategy3 implements ExitStrategy {
	private double rsiValueTwoBefore = -1.0;
	private double rsiValueBefore;
	private boolean firstTime = true;

	/**
	 * Checks if the current open RSI Indicator value is below 15 than the previous closed one or the previous previous closed one.
	 * @param realTimeData
	 * @return the percentage of quantity to sell, null otherwise.
	 */
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (firstTime) {
			rsiValueBefore = realTimeData.getRsiCloseValue(); // last closed candle rsi value
			firstTime = false;
		} // not the first time. already ran.
		double rsiValue = realTimeData.getRsiOpenValue();
		if (rsiValueBefore != realTimeData.getRsiCloseValue()) {
			updateValues(realTimeData.getRsiCloseValue());
		}
		if (lostValueOf15(rsiValueBefore,rsiValue)) {
			System.out.println("Exiting with RSI exit strategy 3. Returning 100(1)");
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL, RSIConstants.RSI_EXIT_OPTION_3_SELLING_PERCENTAGE, Config.ZERO);

		}
		if (rsiValueTwoBefore != -1.0 && lostValueOf15(rsiValueTwoBefore,rsiValue)) {
			System.out.println("Exiting with RSI exit strategy 3. Returning 100(2)");
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL, RSIConstants.RSI_EXIT_OPTION_3_SELLING_PERCENTAGE, Config.ZERO);
		}
		return null;
	}

	private boolean lostValueOf15(double oldVal, double newVal) {
		return oldVal - newVal >= 15;}

	private void updateValues(double newValue) {
		double temp = rsiValueBefore;
		rsiValueBefore = newValue;
		rsiValueTwoBefore = temp;
	}
}
