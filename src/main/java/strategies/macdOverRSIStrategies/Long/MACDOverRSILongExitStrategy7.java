package strategies.macdOverRSIStrategies.Long;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSILongExitStrategy7 extends MACDOverRSIBaseExitStrategy {

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        boolean currentPriceBelowSar = realTimeData.getCurrentPrice() < realTimeData.getSarValueAtIndex(realTimeData.getLastIndex());
        if (currentPriceBelowSar){
            return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
        }
        return null;
    }
}
