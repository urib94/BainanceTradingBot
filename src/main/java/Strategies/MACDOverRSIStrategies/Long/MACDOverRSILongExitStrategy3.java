package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class MACDOverRSILongExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing && currentCandleBiggerThanPrev(realTimeData)) {
			return new SellingInstructions(PositionHandler.ClosePositionTypes.STAY_IN_POSITION, BigDecimal.valueOf(Config.DOUBLE_ZERO), Config.ZERO,Config.TRUE);
		} else if (realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > Config.ZERO && absoluteDecliningPyramid(realTimeData)){
			TelegramMessenger.sendToTelegram("exiting position with long exit 3" + "time: " + ZonedDateTime.now());
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING,
					MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,
					MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE,Config.FALSE);
		}
		return null;
	}
}
