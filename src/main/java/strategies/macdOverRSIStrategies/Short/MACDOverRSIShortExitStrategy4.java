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

public class MACDOverRSIShortExitStrategy4 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private final Trailer trailer;

    public MACDOverRSIShortExitStrategy4(Trailer trailer){
        this.trailer = trailer;
    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        double currentPrice = realTimeData.getCurrentPrice();
        if (! isTrailing){
            trailer.setAbsoluteMaxPrice(realTimeData.getLowPriceAtIndex(realTimeData.getLastIndex()));
            isTrailing = true;
        }
        else{
            trailer.updateTrailer(realTimeData.getLowPriceAtIndex(realTimeData.getLastIndex()));
            boolean currentPriceAboveLowerBollinger = currentPrice > realTimeData.getLowerBollingerAtIndex(realTimeData.getLastIndex());
            if (trailer.needToSell(currentPrice) && currentPriceAboveLowerBollinger){
                TelegramMessenger.sendToTelegram("selling position with short exit 4: " + new Date(System.currentTimeMillis()));
                return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
}

