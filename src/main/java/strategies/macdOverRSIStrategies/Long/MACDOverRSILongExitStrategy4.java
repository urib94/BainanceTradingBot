package strategies.macdOverRSIStrategies.Long;

import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;

import java.math.BigDecimal;
import java.util.Date;

public class MACDOverRSILongExitStrategy4 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final Trailer trailer;

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
				trailer.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
			}
		}
		return null;
	}
}
