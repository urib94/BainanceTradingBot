package Strategies.RSIStrategies;
import Data.AccountBalance;
import Positions.PositionAction;
import Data.Config;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class RSIExitStrategy3 implements ExitStrategy {
	private double rsiValueTwoBefore = Double.MAX_VALUE;
	private double rsiValueBefore = Double.MAX_VALUE;

	public PositionAction run(RealTimeData realTimeData) {
		AccountBalance accountBalance = AccountBalance.getAccountBalance();
		BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfCandles(Config.RSI_CANDLE_NUM);
		int last_bar_index = baseBarSeries.getEndIndex();
		ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
		RSIIndicator rsi = new RSIIndicator(closePriceIndicator, Config.RSI_CANDLE_NUM);
		Num rsiNumValue = rsi.getValue(last_bar_index);
		double rsiDoubleValue = rsiNumValue.doubleValue();
		if (rsiValueBefore != Double.MAX_VALUE) {
			if (lostMoreThan15Percent(rsiValueBefore,rsiDoubleValue)) {
				//TODO: create PositionAction
				return null;
			}
			if (rsiValueTwoBefore != Double.MAX_VALUE) {
				if (lostMoreThan15Percent(rsiValueTwoBefore,rsiDoubleValue)) {
					//TODO: create PositionAction
					return null;
				}
			}
		}
		updateValues(rsiDoubleValue);
		return null;
	}

	private boolean lostMoreThan15Percent(double oldVal, double newVal) {
		return newVal <= oldVal*0.85;
	}

	private void updateValues(double newValue) {
		double temp = rsiValueBefore;
		rsiValueBefore = newValue;
		rsiValueTwoBefore = temp;
	}
}
