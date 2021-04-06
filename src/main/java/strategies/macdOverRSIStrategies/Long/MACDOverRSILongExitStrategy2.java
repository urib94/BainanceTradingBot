package strategies.macdOverRSIStrategies.Long;

import data.DataHolder;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import TradingTools.Trailers.TrailingExit;

import java.util.Date;

public class MACDOverRSILongExitStrategy2 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final TrailingExit trailingExit;

	public MACDOverRSILongExitStrategy2(TrailingExit trailingExit){
		this.trailingExit = trailingExit;
	}



	@Override
	public Instructions run(DataHolder realTimeData) {
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailingExit.updateTrailer(currentPrice);
			if (stayInTrackAndThreePositiveHistograms(realTimeData)){
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with long exit 2" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			boolean currentPriceBelowUpperBollinger = currentPrice < realTimeData.getUpperBollingerAtIndex(realTimeData.getLastIndex());
			boolean prevBelowUpperBollinger = realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()) < realTimeData.getUpperBollingerAtIndex(realTimeData.getLastCloseIndex());
			if (trailingExit.needToSell(currentPrice) && currentPriceBelowUpperBollinger && prevBelowUpperBollinger){
				TelegramMessenger.sendToTelegram("selling position with long exit 2" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (changedDirectionAndPositiveThreeHistogram(realTimeData)) {
				trailingExit.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
				TelegramMessenger.sendToTelegram("start trailing position with long exit 2" + "time: " + new Date(System.currentTimeMillis()));
			}
		}
		return null;
	}
	@Override
	public void updateExitStrategy() {

	}
}
