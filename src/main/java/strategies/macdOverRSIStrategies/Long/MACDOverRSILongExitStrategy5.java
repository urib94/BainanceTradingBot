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

public class MACDOverRSILongExitStrategy5 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private final TrailingExit trailingExit;

    public MACDOverRSILongExitStrategy5(TrailingExit trailingExit){
        this.trailingExit = trailingExit;
    }

    @Override
    public Instructions run(DataHolder realTimeData) {
        if (isTrailing) {
            double currentPrice = realTimeData.getCurrentPrice();
            trailingExit.updateTrailer(currentPrice);
            boolean currentPriceCrossedUpperBollingerUp = realTimeData.crossed(DataHolder.IndicatorType.CLOSE_PRICE, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, realTimeData.getUpperBollingerAtIndex(realTimeData.getLastCloseIndex()));
            if (currentPriceCrossedUpperBollingerUp){
                isTrailing = false;
                trailingExit.setTrailingPercentage(MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE);
                TelegramMessenger.sendToTelegram("stop trailing position with long exit 5" + "time: " + new Date(System.currentTimeMillis()));
                return null;
            }
            if (changedDirection(realTimeData, DataHolder.CandleType.CLOSE)){
                trailingExit.setTrailingPercentage(MACDOverRSIConstants.EXTREME_LOW_TRAILING_PERCENTAGE);
            }
            boolean isBearish = realTimeData.getClosePriceAtIndex(realTimeData.getLastIndex()) < realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
            if (isBearish && trailingExit.needToSell(currentPrice) && currentPrice < realTimeData.getUpperBollingerAtIndex(realTimeData.getLastIndex())){
                TelegramMessenger.sendToTelegram("selling position with long exit 5" + "time: " + new Date(System.currentTimeMillis()));
                return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
                        MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        } else {
            boolean currentPriceCrossedUpperBollingerDown = realTimeData.crossed(DataHolder.IndicatorType.CLOSE_PRICE, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, realTimeData.getUpperBollingerAtIndex(realTimeData.getLastCloseIndex()));

            if (currentPriceCrossedUpperBollingerDown && realTimeData.candleType(DataHolder.CandleType.BEARISH)) {
                TelegramMessenger.sendToTelegram("start trailing position with long exit 5" + "time: " + new Date(System.currentTimeMillis()));
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
