package Strategies.MACDOverRSIStrategies.Long;

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
import java.util.Date;

public class MACDOverRSILongExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
		if (isTrailing) {
			if (!currentCandleBiggerThanPrev(realTimeData) && negativeThreeHistograms(realTimeData)) { //cancel trailing
				return new SellingInstructions(PositionHandler.ClosePositionTypes.STAY_IN_POSITION, BigDecimal.valueOf(Config.DOUBLE_ZERO), Config.ZERO,Config.TRUE);
			}
			else{
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,
						MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE,Config.FALSE); //ask wolloch
			}
		} else {
			if (upwardsPyramid(realTimeData) && negativeThreeHistograms(realTimeData)) {
				TelegramMessenger.sendToTelegram("trailing position with long exit 4" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_WITH_TRAILING,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,
						MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE,Config.FALSE); //ask wolloch
			}
		}
		return null;
	}
}
