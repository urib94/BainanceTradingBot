package Strategies.RSIStrategies;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

public class RSIExitStrategy3 implements ExitStrategy {
	private double rsiValueTwoBefore;
	private double rsiValueBefore;
	private boolean firstTime = true;
	public BigDecimal run(RealTimeData realTimeData) {
		if (firstTime) {
			rsiValueBefore = realTimeData.calculateCurrentClosedRSIValue(); // last closed candle
			rsiValueTwoBefore = -1; // second to last closed candle
			firstTime = false;
		} // not the first time. already ran.
		double rsiValue = realTimeData.calculateCurrentOpenRSIValue();
		if (rsiValueBefore == realTimeData.calculateCurrentClosedRSIValue()) {
			updateValues(rsiValue);
		}
		if (lostValueOf15(rsiValueBefore,rsiValue)) {
			System.out.println("Exiting with RSI exit strategy 3. Returning 100");
			return RSIConstants.RSI_EXIT_OPTION_3_SELLING_PERCENTAGE;
		}
		if (lostValueOf15(rsiValueTwoBefore,rsiValue)) {
			System.out.println("Exiting with RSI exit strategy 3. Returning 100");
			return RSIConstants.RSI_EXIT_OPTION_3_SELLING_PERCENTAGE;
		}
		updateValues(rsiValue);
		return null;
	}

	private boolean lostValueOf15(double oldVal, double newVal) {return oldVal - newVal >= 15;}

	private void updateValues(double newValue) {
		double temp = rsiValueBefore;
		rsiValueBefore = newValue;
		rsiValueTwoBefore = temp;
	}
}
