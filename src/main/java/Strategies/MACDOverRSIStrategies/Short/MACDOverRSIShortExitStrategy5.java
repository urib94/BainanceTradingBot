package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Utils.Trailer;

import java.math.BigDecimal;

public class MACDOverRSIShortExitStrategy5 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private Trailer trailer;

    public MACDOverRSIShortExitStrategy5(Trailer trailer){
        this.trailer = trailer;
    }

    @Override
    public SellingInstructions run(RealTimeData realTimeData) {
        BigDecimal currentPrice = realTimeData.getCurrentPrice();
        if (! isTrailing){
            trailer.setHighestPrice(currentPrice);
            isTrailing = true;
        }
        else{
            trailer.updateTrailer(currentPrice);
            if (trailer.needToSell(currentPrice)){
                return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
}

