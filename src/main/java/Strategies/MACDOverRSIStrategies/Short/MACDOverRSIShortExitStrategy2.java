package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.time.ZonedDateTime;
import java.util.Date;

public class MACDOverRSIShortExitStrategy2 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP,RealTimeData.CandleType.CLOSE, Config.ZERO)) {
			TelegramMessenger.sendToTelegram("exiting position with short exit 2" + "time: " + new Date(System.currentTimeMillis()));
			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,Config.ZERO);
		}
		return null;
	}
}
