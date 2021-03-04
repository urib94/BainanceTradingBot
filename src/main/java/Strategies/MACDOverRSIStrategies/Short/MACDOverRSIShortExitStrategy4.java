package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class MACDOverRSIShortExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && !currentCandleBiggerThanPrev(realTimeData)) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.STAY_IN_POSITION, BigDecimal.valueOf(Config.DOUBLE_ZERO),Config.ZERO,Config.TRUE);
		}
		else if(realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > 0 && currentCandleBiggerThanPrev(realTimeData)) {
			TelegramMessenger.sendToTelegram("exiting position with short exit 4" + "time: " + ZonedDateTime.now());
			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_WITH_TRAILING,MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,
					MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, Config.FALSE);
		}
		return null;
	}
}
