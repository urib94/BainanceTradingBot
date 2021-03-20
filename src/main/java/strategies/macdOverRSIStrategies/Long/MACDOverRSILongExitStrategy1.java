package strategies.macdOverRSIStrategies.Long;

import data.Config;
import data.DataHolder;
import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;

import java.util.Date;

public class MACDOverRSILongExitStrategy1 extends MACDOverRSIBaseExitStrategy {

	private boolean isTrailing = false;
	private final Trailer trailer;

	public MACDOverRSILongExitStrategy1(Trailer trailer){
		this.trailer = trailer;
	}

	@Override
	public SellingInstructions run(DataHolder realTimeData) {
//		boolean openCrossed = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.DOWN, DataHolder.CandleType.OPEN, MACDOverRSIConstants.LONG_EXIT2_OPEN_THRESHOLD);
//		if (openCrossed) {
//			TelegramMessenger.sendToTelegram("exiting position with long exit 1: " + new Date(System.currentTimeMillis()));
//			return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
//		}
//		return null;
		if (isTrailing) {
			double currentPrice = realTimeData.getCurrentPrice();
			trailer.updateTrailer(currentPrice);
			boolean openCrossedUp = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.UP, DataHolder.CandleType.OPEN,MACDOverRSIConstants.SHORT_EXIT2_OPEN_THRESHOLD);
			if (openCrossedUp){
				isTrailing = false;
				TelegramMessenger.sendToTelegram("stop trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
				return null;
			}
			boolean histCrossedTen = realTimeData.getMacdOverRsiCloseValue() < -10;
			if (trailer.needToSell(currentPrice) || histCrossedTen){
				TelegramMessenger.sendToTelegram("selling position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
						MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
			}
		} else {
				boolean openCrossedDown = realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_RSI, DataHolder.CrossType.DOWN, DataHolder.CandleType.OPEN, MACDOverRSIConstants.LONG_EXIT2_OPEN_THRESHOLD);
				if (openCrossedDown) {
				trailer.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
				isTrailing = true;
				TelegramMessenger.sendToTelegram("start trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
			}
		}
		return null;
	}
}
