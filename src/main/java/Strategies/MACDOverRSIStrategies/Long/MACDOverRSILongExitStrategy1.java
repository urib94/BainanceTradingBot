package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Utils.Trailer;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;

public class MACDOverRSILongExitStrategy1 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData) {
		boolean currentPriceBelowSMA = BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastCloseIndex())).compareTo(realTimeData.getCurrentPrice()) > Config.ZERO;
		if (currentPriceBelowSMA) {
			TelegramMessenger.sendToTelegram("exiting position with long exit 1: " + new Date(System.currentTimeMillis()));
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
		}
		return null;
	}
}
