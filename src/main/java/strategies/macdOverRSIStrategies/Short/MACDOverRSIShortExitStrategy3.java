package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import TradingTools.Trailers.TrailingExit;

import java.util.Date;

public class MACDOverRSIShortExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final TrailingExit trailingExit;

	public MACDOverRSIShortExitStrategy3(TrailingExit trailingExit){
		this.trailingExit = trailingExit;
	}

	@Override
	public Instructions run(DataHolder realTimeData) {
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailingExit.updateTrailer(realTimeData.getLowPriceAtIndex(realTimeData.getLastIndex()));
			if (changedDirectionAndPositiveThreeHistogram(realTimeData)) {
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with short exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			boolean isBullish = realTimeData.getClosePriceAtIndex(realTimeData.getLastIndex()) > realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
			if (isBullish && trailingExit.needToSell(currentPrice)){
				TelegramMessenger.sendToTelegram("selling position with short exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (stayInTrackAndThreePositiveHistograms(realTimeData)) {
				isTrailing = true;
				trailingExit.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				TelegramMessenger.sendToTelegram("trailing position with short exit 3" + "time: " + new Date(System.currentTimeMillis()));
			}
		}
		return null;
	}
	@Override
	public void updateExitStrategy() {

	}
}
