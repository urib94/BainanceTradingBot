package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import TradingTools.Trailers.TrailingExit;

import java.util.Date;

public class MACDOverRSIShortExitStrategy5 extends MACDOverRSIBaseExitStrategy {

        private boolean isTrailing = false;
        private final TrailingExit trailingExit;

        public MACDOverRSIShortExitStrategy5(TrailingExit trailingExit) { this.trailingExit = trailingExit;}

        @Override
        public SellingInstructions run (DataHolder realTimeData){
            if (isTrailing) {
                double currentPrice = realTimeData.getCurrentPrice();
                trailingExit.updateTrailer(currentPrice);
                boolean currentPriceCrossedLowerBollingerDown = realTimeData.crossed(DataHolder.IndicatorType.CLOSE_PRICE, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, realTimeData.getLowerBollingerAtIndex(realTimeData.getLastCloseIndex()));
                if (currentPriceCrossedLowerBollingerDown) {
                    isTrailing = false;
                    trailingExit.setTrailingPercentage(MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE);
                    TelegramMessenger.sendToTelegram("stop trailing position with short exit 5" + "time: " + new Date(System.currentTimeMillis()));
                    return null;
                }
                if (changedDirection(realTimeData, DataHolder.CandleType.CLOSE)){
                    trailingExit.setTrailingPercentage(MACDOverRSIConstants.EXTREME_LOW_TRAILING_PERCENTAGE);
                }
                boolean isBullish = realTimeData.getClosePriceAtIndex(realTimeData.getLastIndex()) > realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
                if (isBullish && trailingExit.needToSell(currentPrice) && currentPrice > realTimeData.getLowerBollingerAtIndex(realTimeData.getLastIndex())) {
                    TelegramMessenger.sendToTelegram("trailing position with long exit 5" + "time: " + new Date(System.currentTimeMillis()));
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
                            MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
                }
            } else {
                boolean currentPriceCrossedLowerBollingerUp = realTimeData.crossed(DataHolder.IndicatorType.CLOSE_PRICE, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, realTimeData.getLowerBollingerAtIndex(realTimeData.getLastCloseIndex()));

                if (currentPriceCrossedLowerBollingerUp && realTimeData.candleType(DataHolder.CandleType.BULLISH)) {
                    TelegramMessenger.sendToTelegram("start trailing position with short exit 5" + "time: " + new Date(System.currentTimeMillis()));
                    trailingExit.setAbsoluteMaxPrice(realTimeData.getCurrentPrice());
                    isTrailing = true;
                }
            }
            return null;
        }
}

