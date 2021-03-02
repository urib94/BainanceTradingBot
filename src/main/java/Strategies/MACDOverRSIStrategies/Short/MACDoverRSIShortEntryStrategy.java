package Strategies.MACDOverRSIStrategies.Short;

import Data.RealTimeData;
import Positions.PositionHandler;
import Strategies.EntryStrategy;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.Long.MACDOverRSILongExitStrategy1;
import Strategies.MACDOverRSIStrategies.Long.MACDOverRSILongExitStrategy2;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MACDoverRSIShortEntryStrategy implements EntryStrategy {

	double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
	private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
	private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
	private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;
	@Override
	public PositionHandler run(RealTimeData realTimeData, String symbol) {
		if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN, RealTimeData.CandleType.CLOSE,0)) {
			//TODO: buy stuff
			ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
			exitStrategies.add(new MACDOverRSIShortExitStrategy1());
			exitStrategies.add(new MACDOverRSIShortExitStrategy2());
			return null;
		}
		if (realTimeData.getMacdOverRsiSignalLineValueAtIndex(realTimeData.getLastCloseIndex()) < 0) {
			if (realTimeData.urisRulesForEntry()) {
				//TODO: buy stuff
				ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
				exitStrategies.add(new MACDOverRSIShortExitStrategy1());
				exitStrategies.add(new MACDOverRSIShortExitStrategy2());
				return null;
			}
		}
		return null;
	}






	@Override
	public void setTakeProfitPercentage(double takeProfitPercentage) {
		this.takeProfitPercentage =takeProfitPercentage;
	}

	@Override
	public void setStopLossPercentage(double stopLossPercentage) {
		this.stopLossPercentage = stopLossPercentage;
	}

	@Override
	public void setLeverage(int leverage) {
		this.leverage = leverage;
	}

	@Override
	public void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount) {
		this.requestedBuyingAmount = requestedBuyingAmount;
	}
}
