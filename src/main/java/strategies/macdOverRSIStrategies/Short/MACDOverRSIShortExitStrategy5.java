package strategies.macdOverRSIStrategies.Short;

import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;

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

