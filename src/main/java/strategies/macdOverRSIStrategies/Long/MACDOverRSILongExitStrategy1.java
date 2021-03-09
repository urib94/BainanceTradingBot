package strategies.macdOverRSIStrategies.Long;

import data.Config;
import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;
import java.util.Date;

public class MACDOverRSILongExitStrategy1 extends MACDOverRSIBaseExitStrategy {

	@Override
	public SellingInstructions run(RealTimeData realTimeData) {
		boolean currentPriceBelowSMA = BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX)).compareTo(realTimeData.getCurrentPrice()) > Config.ZERO;
		if (currentPriceBelowSMA) {
			TelegramMessenger.sendToTelegram("exiting position with long exit 1: " + new Date(System.currentTimeMillis()));
			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
		}
		return null;
	}
}
