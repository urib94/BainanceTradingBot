package Strategies;
import Data.RealTimeData;
import Positions.PositionHandler;

public interface EntryStrategy{
    /**
     *
     * @return Position entry if purchased coins else null.
     */
    PositionHandler run(RealTimeData realTimeData);
}
