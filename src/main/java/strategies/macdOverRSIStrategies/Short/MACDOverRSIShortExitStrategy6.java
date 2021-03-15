package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

import java.util.Date;

public class MACDOverRSIShortExitStrategy6 extends MACDOverRSIBaseExitStrategy {
    private double prevClose;
    private double prevHigh;
    private double prevLow;
    private boolean once = true;

    public MACDOverRSIShortExitStrategy6(DataHolder realTimeData) {
        this.prevClose = realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
        this.prevHigh = realTimeData.getHighPriceAtIndex(realTimeData.getLastCloseIndex());
        this.prevLow = realTimeData.getLowPriceAtIndex(realTimeData.getLastCloseIndex());
    }


    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        boolean candleChanged = prevClose == realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()-1) &&
                prevHigh == realTimeData.getHighPriceAtIndex(realTimeData.getLastCloseIndex()-1) &&
                prevLow == realTimeData.getLowPriceAtIndex(realTimeData.getLastCloseIndex()-1);
        if (candleChanged){
            if (once){
                once = false;
                if (realTimeData.candleType(DataHolder.CandleType.BULLISH)){
                    TelegramMessenger.sendToTelegram("selling position with short exit 6" + "time: " + new Date(System.currentTimeMillis()));
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
                }
            }
        }
        return null;
    }
}
