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

public class MACDOverRSILongExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private Trailer trailer;

	public MACDOverRSILongExitStrategy4(Trailer trailer){
		this.trailer = trailer;
	}

	@Override
	public SellingInstructions run(RealTimeData realTimeData) {
		if (isTrailing) {
			BigDecimal currentPrice = realTimeData.getCurrentPrice();
			trailer.updateTrailer(currentPrice);
			if (!currentCandleBiggerThanPrev(realTimeData) && negativeThreeHistograms(realTimeData)) {
				isTrailing = false;
				return null;
			}
			if (trailer.needToSell(currentPrice)){
				TelegramMessenger.sendToTelegram("trailing position with long exit 4" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (upwardsPyramid(realTimeData) && negativeThreeHistograms(realTimeData)) {
				trailer.setHighestPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
			}
		}
		return null;
	}
}
