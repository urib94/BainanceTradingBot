package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Utils.Trailer;

import java.math.BigDecimal;
import java.util.Date;

public class MACDOverRSILongExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private Trailer trailer;

	public MACDOverRSILongExitStrategy3(Trailer trailer){
		this.trailer = trailer;
	}

	@Override
	public SellingInstructions run(RealTimeData realTimeData) {
		if (isTrailing) {
			trailer.updateTrailer(realTimeData.getCurrentPrice());
			if(currentCandleBiggerThanPrev(realTimeData) && positiveThreeHistograms(realTimeData)) {
				isTrailing = false;
				return null;
			}
			if (trailer.needToSell(realTimeData.getCurrentPrice())){
				TelegramMessenger.sendToTelegram("trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (downwardsPyramid(realTimeData) && positiveThreeHistograms(realTimeData)) {
				BigDecimal currentPrice = realTimeData.getCurrentPrice();
				trailer.setHighestPrice(currentPrice);
				isTrailing = true;
			}
		}
		return null;
	}
}
