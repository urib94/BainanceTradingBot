package Strategies;

import Positions.PositionAction;
import Data.RealTimeData;

public interface ExitStrategy {
    PositionAction run(RealTimeData realTimeData);

}
