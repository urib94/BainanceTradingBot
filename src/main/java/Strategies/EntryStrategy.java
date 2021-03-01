package Strategies;
import Data.RealTimeData;
import Positions.PositionHandler;

import java.math.BigDecimal;

public interface EntryStrategy{
    /**
     *
     * @return Position entry if purchased coins else null.
     */
    PositionHandler run(RealTimeData realTimeData, String symbol);

    void setTakeProfitPercentage(double takeProfitPercentage);
    void setStopLossPercentage(double stopLossPercentage);
    void setLeverage(int leverage);
    void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount);
}
