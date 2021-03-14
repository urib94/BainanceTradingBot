package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

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
        boolean candleChanged = prevClose == realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()) &&
                prevHigh == realTimeData.getHighPriceAtIndex(realTimeData.getLastCloseIndex()) &&
                prevLow == realTimeData.getLowPriceAtIndex(realTimeData.getLastCloseIndex());
        if (candleChanged){
            if (once){
                once = false;
                if (realTimeData.candleType(DataHolder.CandleType.BULLISH)){
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
                }
            }
        }
        return null;
    }
}
