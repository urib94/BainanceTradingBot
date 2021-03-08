package strategies.macdOverRSIStrategies.Short;

import data.Config;
import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

import java.util.Date;

public class MACDOverRSIShortExitStrategy2 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData) {
		boolean closedCrossedZero = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP, RealTimeData.CandleType.CLOSE, Config.ZERO);
		boolean openCrossed03 = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP, RealTimeData.CandleType.OPEN, MACDOverRSIConstants.SHORT_EXIT2_OPEN_THRESHOLD);
		if (closedCrossedZero && openCrossed03) {
			TelegramMessenger.sendToTelegram("exiting position with long exit 2" + "time: " + new Date(System.currentTimeMillis()));
			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
		}
		return null;
	}
}
