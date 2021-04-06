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

public class MACDOverRSIShortExitStrategy4 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private final TrailingExit trailingExit;

    public MACDOverRSIShortExitStrategy4(TrailingExit trailingExit){
        this.trailingExit = trailingExit;
    }

    @Override
    public Instructions run(DataHolder realTimeData) {
        double currentPrice = realTimeData.getCurrentPrice();
        if (! isTrailing){
            trailingExit.setAbsoluteMaxPrice(realTimeData.getLowPriceAtIndex(realTimeData.getLastIndex()));
            isTrailing = true;
        }
        else{
            trailingExit.updateTrailer(realTimeData.getLowPriceAtIndex(realTimeData.getLastIndex()));
            boolean currentPriceAboveLowerBollinger = currentPrice > realTimeData.getLowerBollingerAtIndex(realTimeData.getLastIndex());
            if (trailingExit.needToSell(currentPrice) && currentPriceAboveLowerBollinger){
                TelegramMessenger.sendToTelegram("selling position with short exit 4: " + new Date(System.currentTimeMillis()));
                return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
    @Override
    public void updateExitStrategy() {

    }
}

