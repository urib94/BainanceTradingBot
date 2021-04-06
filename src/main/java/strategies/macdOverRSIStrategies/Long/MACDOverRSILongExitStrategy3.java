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

public class MACDOverRSILongExitStrategy3 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final TrailingExit trailingExit;

	public MACDOverRSILongExitStrategy3(TrailingExit trailingExit){
		this.trailingExit = trailingExit;
	}

	@Override
	public Instructions run(DataHolder realTimeData) {
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailingExit.updateTrailer(realTimeData.getHighPriceAtIndex(realTimeData.getLastIndex()));
			if (changedDirectionAndNegativeThreeHistogram(realTimeData)){
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			boolean isBearish = realTimeData.getClosePriceAtIndex(realTimeData.getLastIndex()) < realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
			if (trailingExit.needToSell(currentPrice) && isBearish){
				TelegramMessenger.sendToTelegram("trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			if (stayInTrackAndThreeNegativeHistograms(realTimeData)) {
				TelegramMessenger.sendToTelegram("start trailing position with long exit 3" + "time: " + new Date(System.currentTimeMillis()));
				trailingExit.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
			}

		}
		return null;
	}
	@Override
	public void updateExitStrategy() {

	}
}
