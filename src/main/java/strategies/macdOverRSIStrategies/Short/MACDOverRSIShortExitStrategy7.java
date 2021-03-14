package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSIShortExitStrategy7 extends MACDOverRSIBaseExitStrategy {
    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        boolean currentPriceAboveSar = realTimeData.getCurrentPrice() > realTimeData.getSarValueAtIndex(realTimeData.getLastIndex());
        if (currentPriceAboveSar){
            return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
        }
        return null;
    }
}
