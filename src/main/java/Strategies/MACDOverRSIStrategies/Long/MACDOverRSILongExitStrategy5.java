package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import Positions.SellingInstructions;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Utils.Trailer;

import java.math.BigDecimal;
import java.util.Date;

public class MACDOverRSILongExitStrategy5 extends MACDOverRSIBaseExitStrategy {

    private boolean isTrailing = false;
    private Trailer trailer;

    public MACDOverRSILongExitStrategy5(Trailer trailer){
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
                TelegramMessenger.sendToTelegram("trailing position with long exit 5: " + new Date(System.currentTimeMillis()));
                return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }

}
