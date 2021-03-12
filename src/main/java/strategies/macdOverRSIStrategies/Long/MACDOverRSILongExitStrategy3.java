package strategies.macdOverRSIStrategies.Long;

import data.DataHolder;
import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;

import java.math.BigDecimal;
import java.util.Date;

public class MACDOverRSILongExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final Trailer trailer;

	public MACDOverRSILongExitStrategy3(Trailer trailer){
		this.trailer = trailer;
	}

	@Override
	public SellingInstructions run(DataHolder realTimeData) {
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailer.updateTrailer(realTimeData.getHighPriceAtIndex(realTimeData.getLastIndex()));
			if (changedDirectionAndNegativeThreeHistogram(realTimeData)){
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			if (trailer.needToSell(currentPrice)){
				TelegramMessenger.sendToTelegram("trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (stayInTrackAndThreeNegativeHistograms(realTimeData)) {
				TelegramMessenger.sendToTelegram("start trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				trailer.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
			}

		}
		return null;
	}
}
