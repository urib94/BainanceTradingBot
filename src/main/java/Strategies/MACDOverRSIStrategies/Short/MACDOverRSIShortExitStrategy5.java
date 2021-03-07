package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;

public class MACDOverRSIShortExitStrategy5 extends MACDOverRSIBaseExitStrategy {

    @Override
    public SellingInstructions run(RealTimeData realTimeData, boolean isTrailing) {
        return new SellingInstructions(PositionHandler.ClosePositionTypes.TRAILING_SHORT_STOP_LOSS,
                MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE,
                MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, Config.FALSE);
    }
}

