package strategies;
import data.DataHolder;
import positions.PositionHandler;

public interface EntryStrategy{
    /**
     *
     * @return Position entry if purchased coins else null.
     */
    PositionHandler run(DataHolder realTimeData, String symbol);

    void setTakeProfitPercentage(double takeProfitPercentage);
    void setStopLossPercentage(double stopLossPercentage);
    void setLeverage(int leverage);
    void setRequestedBuyingAmount(double requestedBuyingAmount);

    void positionClosed();
}
