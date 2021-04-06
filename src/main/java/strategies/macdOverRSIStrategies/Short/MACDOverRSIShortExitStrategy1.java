package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import TradingTools.Trailers.TrailingExit;

import java.util.Date;

public class MACDOverRSIShortExitStrategy1 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final TrailingExit trailingExit;

	public MACDOverRSIShortExitStrategy1(TrailingExit trailingExit){
		this.trailingExit = trailingExit;
	}

	@Override
	public SellingInstructions run(DataHolder realTimeData) {
//		boolean openCrossed = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.UP, DataHolder.CandleType.OPEN, MACDOverRSIConstants.SHORT_EXIT2_OPEN_THRESHOLD);
//		if (openCrossed) {
//			TelegramMessenger.sendToTelegram("exiting position with short exit 1" + "time: " + new Date(System.currentTimeMillis()));
//			return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
//		}
//		return null;
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailingExit.updateTrailer(currentPrice);
			boolean openCrossedDown = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.DOWN, DataHolder.CandleType.OPEN,MACDOverRSIConstants.LONG_EXIT2_OPEN_THRESHOLD);
			if (openCrossedDown){
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			boolean histCrossedTen = realTimeData.getMacdOverRsiCloseValue() > 10;
			if (trailingExit.needToSell(currentPrice) || histCrossedTen){
				TelegramMessenger.sendToTelegram("selling position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
			boolean openCrossedUp = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.UP, DataHolder.CandleType.OPEN, MACDOverRSIConstants.SHORT_EXIT2_OPEN_THRESHOLD);
			if (openCrossedUp) {
				trailingExit.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
				TelegramMessenger.sendToTelegram("start trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
			}
		}
		return null;
	}
}
