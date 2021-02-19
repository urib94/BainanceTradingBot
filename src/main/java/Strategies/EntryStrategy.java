package Strategies;
import Data.AccountBalance;
import Data.PositionEntry;
import Data.RealTimeData;

public interface EntryStrategy{
    /**
     *
     * @return Position entry if purchased coins else null.
     */
    PositionEntry run(RealTimeData realTimeData);
}
