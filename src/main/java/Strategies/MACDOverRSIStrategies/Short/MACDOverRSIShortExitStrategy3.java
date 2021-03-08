package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Utils.Trailer;
import com.binance.client.api.model.enums.PositionSide;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;

public class MACDOverRSIShortExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private Trailer trailer;

	public MACDOverRSIShortExitStrategy3(Trailer trailer){
		this.trailer = trailer;
	}

	@Override
	public SellingInstructions run(RealTimeData realTimeData) {
		if (isTrailing) {
			BigDecimal currentPrice = realTimeData.getCurrentPrice();
			trailer.updateTrailer(currentPrice);
			if(currentCandleBiggerThanPrev(realTimeData) && negativeThreeHistograms(realTimeData)) {
				isTrailing = false;
				return null;
			}
			if (trailer.needToSell(currentPrice)){
				TelegramMessenger.sendToTelegram("trailing position with short exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (downwardsPyramid(realTimeData) && negativeThreeHistograms(realTimeData)) {
				 isTrailing = true;
				 trailer.setHighestPrice(realTimeData.getCurrentPrice());
			}
		}
		return null;
	}
}
