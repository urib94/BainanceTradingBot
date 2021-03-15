package strategies.macdOverRSIStrategies.Short;

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

public class MACDOverRSIShortExitStrategy2 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final Trailer trailer;

	public MACDOverRSIShortExitStrategy2(Trailer trailer){
		this.trailer = trailer;
	}

	@Override
	public SellingInstructions run(DataHolder realTimeData) {
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailer.updateTrailer(realTimeData.getLowPriceAtIndex(realTimeData.getLastIndex()));
			if(stayInTrackAndThreeNegativeHistograms(realTimeData)) {
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with short exit 2" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			boolean currentPriceAboveLowerBollinger = currentPrice > realTimeData.getLowerBollingerAtIndex(realTimeData.getLastIndex());
			boolean prevAboveLowerBollinger = realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()) > realTimeData.getLowerBollingerAtIndex(realTimeData.getLastCloseIndex());
			boolean isBullish = realTimeData.getClosePriceAtIndex(realTimeData.getLastIndex()) > realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
			if (isBullish && trailer.needToSell(currentPrice) && prevAboveLowerBollinger && currentPriceAboveLowerBollinger){
				TelegramMessenger.sendToTelegram("selling position with short exit 2" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (changedDirectionAndNegativeThreeHistogram(realTimeData)) {
				 isTrailing = true;
				 trailer.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				 TelegramMessenger.sendToTelegram("trailing position with short exit 2" + "time: " + new Date(System.currentTimeMillis()));
			}
		}
		return null;
	}
}
