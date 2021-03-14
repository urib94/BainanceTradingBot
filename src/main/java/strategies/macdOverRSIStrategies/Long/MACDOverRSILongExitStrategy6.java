package strategies.macdOverRSIStrategies.Long;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSILongExitStrategy6 extends MACDOverRSIBaseExitStrategy {
    private double prevClose;
    private double prevHigh;
    private double prevLow;
    private boolean once = true;

    public MACDOverRSILongExitStrategy6(DataHolder realTimeData) {
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
                if (realTimeData.candleType(DataHolder.CandleType.BEARISH)){
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
                }
            }
        }
        return null;
    }
}
