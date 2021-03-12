package strategies.macdOverRSIStrategies.Short;

import data.DataHolder;
import data.RealTimeData;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;

import java.math.BigDecimal;
import java.util.Date;

public class MACDOverRSIShortExitStrategy5 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private final Trailer trailer;

    public MACDOverRSIShortExitStrategy5(Trailer trailer){
        this.trailer = trailer;
    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        BigDecimal currentPrice = realTimeData.getCurrentPrice();
        if (! isTrailing){
            trailer.setAbsoluteMaxPrice(currentPrice);
            isTrailing = true;
        }
        else{
            trailer.updateTrailer(currentPrice);
            if (trailer.needToSell(currentPrice)){
                TelegramMessenger.sendToTelegram("trailing position with short exit 5: " + new Date(System.currentTimeMillis()));
                return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
}

