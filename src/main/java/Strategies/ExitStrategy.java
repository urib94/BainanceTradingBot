package Strategies;

import Data.AccountBalance;
import Data.RealTimeData;

public interface ExitStrategy {
    void run(AccountBalance accountBalance, RealTimeData realTimeData);

}
