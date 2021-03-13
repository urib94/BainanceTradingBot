package strategies.macdOverRSIStrategies.Short;

import data.Config;
import data.DataHolder;
import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

import java.util.Date;

public class MACDOverRSIShortExitStrategy1 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(DataHolder realTimeData) {
		boolean openCrossed = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.UP, DataHolder.CandleType.OPEN, MACDOverRSIConstants.SHORT_EXIT2_OPEN_THRESHOLD);
		if (openCrossed) {
			TelegramMessenger.sendToTelegram("exiting position with short exit 1" + "time: " + new Date(System.currentTimeMillis()));
			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
		}
		return null;
	}
}
