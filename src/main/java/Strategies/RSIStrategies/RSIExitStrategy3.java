package Strategies.RSIStrategies;
import Data.Config;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

public class RSIExitStrategy3 implements ExitStrategy {
	private double rsiValueTwoBefore = Double.MAX_VALUE;
	private double rsiValueBefore = Double.MAX_VALUE;

	public BigDecimal run(RealTimeData realTimeData) {
		RSIIndicator rsiIndicator = realTimeData.getRsiOpenIndicator();
		int lastBarIndex = realTimeData.getRealTimeData().getEndIndex();
		Num rsiNumValue = rsiIndicator.getValue(lastBarIndex);
		double rsiDoubleValue = rsiNumValue.doubleValue();
		if (rsiValueBefore != Double.MAX_VALUE) {
			if (lostValueOf15(rsiValueBefore,rsiDoubleValue)) {
				return Config.RSI_EXIT_OPTION_3_SELLING_PERCENTAGE;
			}
			if (rsiValueTwoBefore != Double.MAX_VALUE) {
				if (lostValueOf15(rsiValueTwoBefore,rsiDoubleValue)) {
					return Config.RSI_EXIT_OPTION_3_SELLING_PERCENTAGE;
				}
			}
		}
		updateValues(rsiDoubleValue);
		return null;
	}

	private boolean lostValueOf15(double oldVal, double newVal) {
		return oldVal - newVal >= 15;
	}

	private void updateValues(double newValue) {
		double temp = rsiValueBefore;
		rsiValueBefore = newValue;
		rsiValueTwoBefore = temp;
	}
}
