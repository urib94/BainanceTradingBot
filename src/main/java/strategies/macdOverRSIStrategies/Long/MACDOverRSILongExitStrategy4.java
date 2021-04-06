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

public class MACDOverRSILongExitStrategy4 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private final TrailingExit trailingExit;

    public MACDOverRSILongExitStrategy4(TrailingExit trailingExit){
        this.trailingExit = trailingExit;
    }
    @Override
    public Instructions run(DataHolder realTimeData) {
        double currentPrice = realTimeData.getCurrentPrice();
        if (! isTrailing){
            trailingExit.setAbsoluteMaxPrice(realTimeData.getHighPriceAtIndex(realTimeData.getLastIndex()));
            isTrailing = true;
        }
        else{
            trailingExit.updateTrailer(realTimeData.getHighPriceAtIndex(realTimeData.getLastIndex()));
            boolean currentPriceBelowUpperBollinger = currentPrice < realTimeData.getUpperBollingerAtIndex(realTimeData.getLastIndex());
            if (trailingExit.needToSell(currentPrice) && currentPriceBelowUpperBollinger){
                TelegramMessenger.sendToTelegram("selling position with long exit 4: " + new Date(System.currentTimeMillis()));
                return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
    @Override
    public void updateExitStrategy() {

    }
}
