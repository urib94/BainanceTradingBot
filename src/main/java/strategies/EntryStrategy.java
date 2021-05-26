package strategies;
import data.DataHolder;
import data.RealTimeData;
import positions.PositionHandler;

import java.math.BigDecimal;

public interface EntryStrategy{
    /**
     *
     * @return Position entry if purchased coins else null.
     */
    PositionHandler run(DataHolder realTimeData, String symbol);

    void setLeverage(int leverage);
    void setRequestedBuyingAmount(double requestedBuyingAmount);

    void positionClosed();
}
