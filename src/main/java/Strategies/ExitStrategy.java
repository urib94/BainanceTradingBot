package Strategies;

import Data.AccountBalance;
import Data.PositionAction;
import Data.RealTimeData;
import org.ta4j.core.num.Num;

public interface ExitStrategy {
    PositionAction run(RealTimeData realTimeData);

}
