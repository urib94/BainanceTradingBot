package Strategies;

import Data.AccountBalance;
import Data.RealTimeData;

public interface ExitStrategy {
    PositionAction run(AccountBalance accountBalance, RealTimeData realTimeData);
}
